# --- ФАЙЛ: mewnbase_autoupdate_ai.py (AI ВЕРСИЯ) ---

import os
import sys
import zipfile
import shutil
import time
import io
import difflib
import subprocess
import json
import re
from datetime import datetime

# --- ОСНОВНЫЕ ПУТИ ---
TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(TOOLS_DIR)

BASEGAME_PATH = os.path.join(PROJECT_ROOT, "basegame")
GAME_PATH = os.path.join(BASEGAME_PATH, "game", "desktop-1.0.jar")
SOURCE_PATH = os.path.join(PROJECT_ROOT, "src")
UNARCHIVED_PATH = os.path.join(PROJECT_ROOT, "work")
COMPILE_DIR = os.path.join(PROJECT_ROOT, "out")
autostart_path = os.path.join(BASEGAME_PATH, "MewnBase.exe")
BACKUP_PATH = os.path.join(PROJECT_ROOT, "backups")

# --- ФАЙЛЫ СОСТОЯНИЯ ---
STATUS_FILE = os.path.join(PROJECT_ROOT, "autocompile.status")
ERROR_LOG_FILE = os.path.join(PROJECT_ROOT, "compile_errors.json")
CHANGED_TRACK_FILE = os.path.join(PROJECT_ROOT, "autocompile.changed.json")
UNARCHIVE_MARKER = os.path.join(UNARCHIVED_PATH, ".unarchived_from")

# --- Функции для работы с файлами состояния ---

def update_status(status_code, details=""):
    """Записывает текущий статус в файл."""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    content = f"{status_code}|{timestamp}\n{details}"
    temp_status_file = STATUS_FILE + ".tmp"
    try:
        with open(temp_status_file, 'w', encoding='utf-8') as f:
            f.write(content)
        os.replace(temp_status_file, STATUS_FILE)
    except Exception as e:
        print(f"Не удалось обновить файл статуса: {e}")

def read_error_log():
    """Читает словарь ошибок из JSON-файла."""
    if not os.path.exists(ERROR_LOG_FILE):
        return {}
    try:
        with open(ERROR_LOG_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    except (json.JSONDecodeError, FileNotFoundError):
        return {}

def write_error_log(errors):
    """Записывает словарь ошибок в JSON-файл."""
    try:
        with open(ERROR_LOG_FILE, 'w', encoding='utf-8') as f:
            json.dump(errors, f, indent=4, ensure_ascii=False)
    except Exception as e:
        print(f"Не удалось записать лог ошибок: {e}")

def read_changed_track():
    if not os.path.exists(CHANGED_TRACK_FILE):
        return set()
    try:
        with open(CHANGED_TRACK_FILE, 'r', encoding='utf-8') as f:
            data = json.load(f)
            if isinstance(data, list):
                return set(data)
    except Exception:
        return set()
    return set()

def write_changed_track(changed_set):
    try:
        with open(CHANGED_TRACK_FILE, 'w', encoding='utf-8') as f:
            json.dump(sorted(list(changed_set)), f, indent=2, ensure_ascii=False)
    except Exception as e:
        print(f"Не удалось записать список измененных файлов: {e}")

def ensure_unarchived():
    """Ensure UNARCHIVED_PATH contains an extracted copy of the current game jar."""
    try:
        jar_mtime = os.path.getmtime(GAME_PATH)
    except Exception:
        jar_mtime = None

    need_extract = False
    if not os.path.exists(UNARCHIVED_PATH):
        need_extract = True
    else:
        if not os.path.isdir(UNARCHIVED_PATH):
            need_extract = True
        else:
            if not os.path.exists(UNARCHIVE_MARKER):
                need_extract = True
            else:
                try:
                    with open(UNARCHIVE_MARKER, 'r', encoding='utf-8') as f:
                        marker = f.read().strip()
                    if str(jar_mtime) != marker:
                        need_extract = True
                except Exception:
                    need_extract = True

    if not need_extract:
        return

    print("Распаковка базового JAR в work/ ...")
    if os.path.exists(UNARCHIVED_PATH):
        try:
            shutil.rmtree(UNARCHIVED_PATH)
        except Exception as e:
            print(f"\033[91mНе удалось очистить work/: {e}\033[0m")
            return
    os.makedirs(UNARCHIVED_PATH, exist_ok=True)
    try:
        with zipfile.ZipFile(GAME_PATH, 'r') as zin:
            zin.extractall(UNARCHIVED_PATH)
        if jar_mtime is not None:
            with open(UNARCHIVE_MARKER, 'w', encoding='utf-8') as f:
                f.write(str(jar_mtime))
    except Exception as e:
        print(f"\033[91mНе удалось распаковать JAR: {e}\033[0m")

def kill_mewnbase_processes():
    """Kill MewnBase.exe and any java process using desktop-1.0.jar."""
    try:
        os.system('taskkill /F /IM MewnBase.exe > NUL 2>&1')
    except Exception:
        pass
    try:
        cmd = 'powershell -Command "Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like \'*desktop-1.0.jar*\' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"'
        os.system(cmd)
    except Exception:
        pass
    try:
        # Kill cmd.exe windows running run_*.bat scripts (server/client/log)
        cmd = (
            'powershell -Command "'
            '$patterns = @('
            '\'run_server.bat\',\'run_client.bat\',\'run_client1_uitest.bat\',\'run_client2_uitest.bat\','
            '\'run_both.bat\',\'run_log.bat\',\'run_log_uitest.bat\',\'run_server_uitest.bat\');'
            'Get-CimInstance Win32_Process | Where-Object { $_.Name -ieq \'cmd.exe\' } | '
            'Where-Object { $cl=$_.CommandLine; $cl -and ($patterns | Where-Object { $cl -like (\'*\' + $_ + \'*\') }) } | '
            'ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"'
        )
        os.system(cmd)
    except Exception:
        pass

# <<< НАЧАЛО ИЗМЕНЕННОГО БЛОКА >>>
def update_compile_status(java_file, compiler_stderr, returncode):
    """
    Обновляет лог ошибок для ОДНОГО файла.
    При успехе - удаляет запись. При ошибке - сохраняет ПОЛНЫЙ вывод компилятора.
    """
    errors = read_error_log()
    
    if returncode == 0:
        # Компиляция успешна, удаляем файл из лога, если он там был
        if java_file in errors:
            del errors[java_file]
            print(f"\033[92mСтатус файла '{os.path.basename(java_file)}' изменен на 'OK'.\033[0m")
    else:
        # Компиляция провалена. Записываем весь вывод stderr как есть, без парсинга.
        errors[java_file] = {
            "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            # Сохраняем полный, необработанный вывод stderr для максимальной информативности
            "compiler_output": compiler_stderr.strip()
        }
    
    write_error_log(errors)
# <<< КОНЕЦ ИЗМЕНЕННОГО БЛОКА >>>


# --- Вспомогательные функции ---

def zipdir(path, ziph):
    for root, _, files in os.walk(path):
        for file in files:
            file_path = os.path.join(root, file)
            archive_name = os.path.relpath(file_path, path)
            ziph.write(file_path, archive_name)

def highlight_diff(line):
    if line.startswith('+'): return f"\033[92m{line}\033[0m"
    elif line.startswith('-'): return f"\033[91m{line}\033[0m"
    elif line.startswith('^'): return f"\033[93m{line}\033[0m"
    else: return line

def create_structured_backup(file_to_backup):
    if not os.path.exists(file_to_backup): return
    relative_path = os.path.relpath(file_to_backup, PROJECT_ROOT)
    backup_file_path = os.path.join(BACKUP_PATH, relative_path) + ".bak"
    os.makedirs(os.path.dirname(backup_file_path), exist_ok=True)
    if not os.path.exists(backup_file_path):
        shutil.copy2(file_to_backup, backup_file_path)
        print(f"Создана резервная копия: {backup_file_path}")

# --- Основная логика ---

def compile_and_update(changed_files, deleted_files):
    ensure_unarchived()

    changed_set = read_changed_track()

    if deleted_files:
        print("Удаление старых .class файлов...")
        for java_file in deleted_files:
            if java_file in changed_set:
                changed_set.discard(java_file)
            relative_java_path = os.path.relpath(java_file, SOURCE_PATH)
            class_file_path = os.path.join(UNARCHIVED_PATH, os.path.splitext(relative_java_path)[0] + ".class")
            if os.path.exists(class_file_path):
                try:
                    os.remove(class_file_path)
                    print(f"Удален: {class_file_path}")
                except OSError as e:
                    print(f"\033[91mОшибка при удалении {class_file_path}: {e}\033[0m")
        write_changed_track(changed_set)

    compilation_failed = False
    if changed_files:
        for java_file in changed_files:
            changed_set.add(java_file)
        write_changed_track(changed_set)

    if changed_set:
        print("Создание резервных копий измененных файлов...")
        for java_file in sorted(changed_set):
            create_structured_backup(java_file)
            relative_java_path = os.path.relpath(java_file, SOURCE_PATH)
            class_file_path = os.path.join(UNARCHIVED_PATH, os.path.splitext(relative_java_path)[0] + ".class")
            create_structured_backup(class_file_path)

        update_status("COMPILING", f"Компиляция {len(changed_set)} файл(а/ов)...")

        for java_file in sorted(changed_set):
            print(f"--- Компиляция: {os.path.basename(java_file)} ---")
            compile_command = ['javac', '-cp', UNARCHIVED_PATH, '-sourcepath', SOURCE_PATH, java_file, '-d', COMPILE_DIR]
            result = subprocess.run(compile_command, capture_output=True, text=True, encoding='cp866')
            
            update_compile_status(java_file, result.stderr, result.returncode)

            if result.returncode != 0:
                compilation_failed = True
                print(f"\033[91mОшибка компиляции в файле {os.path.basename(java_file)}!\033[0m")
                # Выводим полный вывод ошибки в консоль
                print(result.stderr)
            else:
                print(f"\033[92mУспешно скомпилирован.\033[0m")

        if compilation_failed:
            error_message = "Один или несколько файлов не скомпилировались. Игра не будет обновлена."
            print(f"\033[91m{error_message}\033[0m")
            update_status("ERROR", error_message)
            return

        if os.path.exists(COMPILE_DIR):
            shutil.copytree(COMPILE_DIR, UNARCHIVED_PATH, dirs_exist_ok=True)
            shutil.rmtree(COMPILE_DIR)
            os.makedirs(COMPILE_DIR)

    update_status("UPDATING", "Упаковка JAR-архива...")
    print("Упаковка в архив...")
    zip_buffer = io.BytesIO()
    with zipfile.ZipFile(zip_buffer, 'w', compression=zipfile.ZIP_STORED) as zout:
        zipdir(UNARCHIVED_PATH, zout)
    
    try:
        kill_mewnbase_processes()
        time.sleep(0.6)
        with open(GAME_PATH, 'wb') as f:
            f.write(zip_buffer.getvalue())
        print('Игра обновлена!')
        update_status("SUCCESS", "Игра успешно обновлена.")
        if autostart_path and os.path.exists(autostart_path):
            os.startfile(autostart_path)
    except Exception as e:
        error_message = f"Произошла ошибка при обновлении JAR-файла: {e}"
        print(f"\033[91m{error_message}\033[0m")
        update_status("ERROR", error_message)

# --- Основной цикл отслеживания ---
def main_loop():
    os.makedirs(BACKUP_PATH, exist_ok=True)
    os.makedirs(COMPILE_DIR, exist_ok=True)
    ensure_unarchived()
    print("Отслеживание изменений в файлах...")
    update_status("IDLE", "Скрипт запущен и отслеживает изменения.")
    last_modified = {}
    for root, _, files in os.walk(SOURCE_PATH):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                last_modified[file_path] = os.path.getmtime(file_path)

    while True:
        changed_files = []
        deleted_files = []
        
        current_files = set(last_modified.keys())
        for file_path in current_files:
            if not os.path.exists(file_path):
                deleted_files.append(file_path)
                del last_modified[file_path]
                print(f"Файл удален: {file_path}")

        for root, _, files in os.walk(SOURCE_PATH):
            for file in files:
                if file.endswith('.java'):
                    file_path = os.path.join(root, file)
                    try:
                        current_modified = os.path.getmtime(file_path)
                        if file_path not in last_modified:
                            print(f"Найден новый файл: {file_path}")
                            last_modified[file_path] = current_modified
                            changed_files.append(file_path)
                        elif current_modified != last_modified[file_path]:
                            print(f"Файл изменен: {file_path}")
                            last_modified[file_path] = current_modified
                            changed_files.append(file_path)
                    except FileNotFoundError:
                        continue

        if changed_files or deleted_files:
            compile_and_update(changed_files, deleted_files)
            if os.path.exists(STATUS_FILE):
                 with open(STATUS_FILE, 'r', encoding='utf-8') as f:
                    if not f.readline().startswith("ERROR"):
                         update_status("IDLE", "Ожидание следующих изменений...")
            
        time.sleep(1)

if __name__ == "__main__":
    if '--check' in sys.argv or '--check_status' in sys.argv:
        try:
            import check_status
            check_status.main()
        except ImportError:
            print("\033[91mОшибка: Не удалось найти файл 'check_status.py'.\033[0m")
            print("Убедитесь, что оба скрипта находятся в одной директории.")
        except Exception as e:
            print(f"\033[91mПроизошла ошибка при выполнении check_status: {e}\033[0m")
    else:
        main_loop()
