# MewnBase Multiplayer

Модификация MewnBase с кооперативным мультиплеером (TCP, порт **7777** по умолчанию).

Это рабочий репозиторий для текущей сборки и правок. Активный код лежит в `src/com/cairn4/moonbase`, а `basegame/` и `work/` используются как локальные runtime- и упаковочные артефакты.

## Быстрый старт (игрокам)

**[Инструкция: установка и игра с друзьями (RU)](docs/PLAY_WITH_FRIENDS.md)** — установка, LAN, **Radmin VPN**, типичные ошибки.

Кратко:

1. Запустите игру (`MewnBase.exe` или `run.bat` в portable-сборке).
2. **Хост:** меню → **MULTIPLAYER** → **Create Server (7777)** → выберите сохранение.
3. **Друзья:** **MULTIPLAYER** → IP хоста (в интернете — IP из **Radmin VPN**) → порт **7777** → **Connect**.

> Репозиторий **не содержит** файлов оригинальной игры. Нужна лицензионная копия MewnBase или готовый portable из [Releases](https://github.com/rainbownyashka/mewnbasemultiplayer/releases).

## Структура репозитория (разработка)

| Папка | Назначение |
|-------|------------|
| `basegame/` | Локальная копия игры для запуска и патча jar, не хранится в git |
| `src/com/cairn4/moonbase/` | Основные исходники модификации |
| `src/` | Вендорнутые зависимости, рендер, UI и вспомогательные классы |
| `out/` | Временные `.class` после компиляции |
| `work/` | Распакованный jar для перепаковки |
| `tools/` | `mewnbase_autoupdate.py`, `patch_manual.ps1`, служебные скрипты |
| `docs/` | Операционная и техдокументация |
| `old/` | Старые деcompiled-варианты и сравнения, для справки |
| `repos/MewnbaseModded/` | Отдельный репозиторий с loader/patch-инфраструктурой |

Запуск разработческой сборки из корня: **`run.bat`** (вызывает автокомпиляцию и патч jar).

## Документация

- **[Игра с друзьями — для новичков](docs/PLAY_WITH_FRIENDS.md)**
- **[Зеркало списка серверов (VPS ~300 MB)](docs/SERVER_BROWSER.md)**
- [Статус разработки](docs/STATUS.md)
- [Задачи](docs/TODO.md)
- [Мультиплеер (техн.)](src/MULTIPLAYER_FULL.md)
- [Полная документация проекта](src/PROJECT_DOCUMENTATION.md)
- [Релизы](RELEASING.md)

## Ссылки

- Репозиторий: https://github.com/rainbownyashka/mewnbasemultiplayer
