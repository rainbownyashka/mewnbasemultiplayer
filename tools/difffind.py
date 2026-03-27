import os
import shutil
from datetime import datetime
import time

def find_and_copy_modified_java_files(source_dir, dest_dir, cutoff_datetime_str):
    """
    Checks for .java files modified after a specific datetime and copies them
    to a destination directory, preserving the folder structure.
    
    Args:
        source_dir (str): The source directory to check.
        dest_dir (str): The destination directory for modified files.
        cutoff_datetime_str (str): The cutoff datetime in 'DD.MM.YYYY HH:MM' format.
    """
    # Преобразуем строку с датой в объект datetime, а затем в метку времени (timestamp)
    try:
        cutoff_datetime_obj = datetime.strptime(cutoff_datetime_str, "%d.%m.%Y %H:%M")
        cutoff_timestamp = cutoff_datetime_obj.timestamp()
    except ValueError:
        print("Ошибка: Неверный формат даты. Пожалуйста, используйте 'DD.MM.YYYY HH:MM'.")
        return

    print(f"Ищем файлы, изменённые после: {cutoff_datetime_str}")

    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)

    # Проходим по всем файлам и подпапкам в исходной директории
    for dirpath, _, filenames in os.walk(source_dir):
        for filename in filenames:
            if filename.endswith('.java'):
                file_path = os.path.join(dirpath, filename)

                # Получаем время последней модификации файла
                mod_timestamp = os.path.getmtime(file_path)

                # Сравниваем метки времени
                if mod_timestamp > cutoff_timestamp:
                    print(f"Найден изменённый файл: {file_path}")

                    # Создаем относительный путь для сохранения структуры папок
                    relative_path = os.path.relpath(file_path, source_dir)
                    destination_path = os.path.join(dest_dir, relative_path)
                    
                    # Создаем необходимые подпапки в целевой директории
                    os.makedirs(os.path.dirname(destination_path), exist_ok=True)
                    
                    # Копируем файл
                    shutil.copy2(file_path, destination_path)
    
    print("Процесс завершен.")

if __name__ == "__main__":
    # Укажите свои пути и время
    source_folder = r"C:\Users\ASUS\Documents\mewnbase\mewnbase_dc"
    destination_folder = r"C:\Users\ASUS\Documents\mewnbase\diff"
    
    # Время, после которого нужно искать изменения
    modification_cutoff_time = "10.08.2024 18:39"
    
    find_and_copy_modified_java_files(source_folder, destination_folder, modification_cutoff_time)