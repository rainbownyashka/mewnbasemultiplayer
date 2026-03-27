# --- ФАЙЛ: check_status.py (ИСПРАВЛЕННАЯ ВЕРСИЯ) ---

import os
import time
import json

# --- ПУТИ ---
PROJECT_ROOT = r"C:\Users\ASUS\Documents\mewnbase"
STATUS_FILE = os.path.join(PROJECT_ROOT, "autocompile.status")
ERROR_LOG_FILE = os.path.join(PROJECT_ROOT, "compile_errors.json")

# Статусы, при которых нужно ждать
IN_PROGRESS_STATUSES = ["COMPILING", "UPDATING"]

def get_status():
    """Читает статус из файла."""
    try:
        with open(STATUS_FILE, 'r', encoding='utf-8') as f:
            return f.read()
    except FileNotFoundError:
        return "NOT_FOUND|0\nФайл статуса не найден. Запустите основной скрипт."
    except Exception as e:
        return f"READ_ERROR|0\nНе удалось прочитать файл статуса: {e}"

# <<< НАЧАЛО ИЗМЕНЕННОГО БЛОКА >>>
def display_error_summary():
    """Читает и выводит информацию о текущих ошибках компиляции."""
    errors = {}
    if os.path.exists(ERROR_LOG_FILE):
        try:
            with open(ERROR_LOG_FILE, 'r', encoding='utf-8') as f:
                errors = json.load(f)
        except (json.JSONDecodeError, FileNotFoundError):
            pass

    if not errors:
        print("\033[92mВсе отслеживаемые файлы скомпилированы без ошибок.\033[0m")
        return

    print("\033[91mНайдены файлы с ошибками компиляции:\033[0m")
    for file_path, details in errors.items():
        file_name = os.path.basename(file_path)
        timestamp = details.get("timestamp", "N/A")
        print(f"\n  \033[93mФайл: {file_name}\033[0m (Последняя ошибка: {timestamp})")
        
        # Получаем полный вывод компилятора из поля "compiler_output"
        compiler_output = details.get("compiler_output", "Нет данных об ошибке.")
        # Выводим его с отступом для читаемости
        indented_output = "\n".join([f"    {line}" for line in compiler_output.splitlines()])
        print(f"\033[97m{indented_output}\033[0m") # Выводим белым цветом
# <<< КОНЕЦ ИЗМЕНЕННОГО БЛОКА >>>


def main():
    """Основная логика проверки статуса."""
    print("Проверка статуса авто-компилятора...")
    
    status_content = get_status()
    first_line = status_content.split('\n')[0]
    parts = first_line.split('|')
    status_code = parts[0]
    
    if status_code in IN_PROGRESS_STATUSES:
        print(f"Процесс еще выполняется (статус: {status_code}). Попробуйте позже.")
        return

    print("-" * 30)
    
    timestamp = parts[1] if len(parts) > 1 else "N/A"
    details = '\n'.join(status_content.split('\n')[1:])

    if status_code == "SUCCESS":
        print(f"Статус последней операции: \033[92mУСПЕШНО\033[0m (на {timestamp})")
        print(f"Детали: {details}")
    elif status_code == "ERROR":
        print(f"Статус последней операции: \033[91mОШИБКА\033[0m (на {timestamp})")
        print(details)
    elif status_code == "IDLE":
        print(f"Статус последней операции: \033[94mОЖИДАНИЕ\033[0m (на {timestamp})")
        print("Скрипт готов к работе. Нет активных задач.")
    else:
        print(status_content)
        
    print("-" * 30)
    
    print("\nСводка по состоянию файлов:")
    display_error_summary()
    print("-" * 30)


if __name__ == "__main__":
    main()