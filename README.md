# MewnBase Multiplayer

Модификация MewnBase с кооперативным мультиплеером (TCP, порт **7777** по умолчанию).

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
| `basegame/` | Оригинальные файлы игры + пропатченный jar (локально, не в git) |
| `src/` | Исходники Java (ваши правки) |
| `out/` | Скомпилированные `.class` |
| `tools/` | `mewnbase_autoupdate.py`, `patch_manual.ps1` |
| `docs/` | Документация, в т.ч. [PLAY_WITH_FRIENDS.md](docs/PLAY_WITH_FRIENDS.md) |

Запуск разработческой сборки из корня: **`run.bat`** (вызывает автокомпиляцию и патч jar).

## Документация

- **[Игра с друзьями — для новичков](docs/PLAY_WITH_FRIENDS.md)**
- **[Зеркало списка серверов (VPS ~300 MB)](docs/SERVER_BROWSER.md)**
- [Статус разработки](docs/STATUS.md)
- [Задачи](docs/TODO.md)
- [Мультиплеер (техн.)](src/MULTIPLAYER_FULL.md)
- [Релизы](RELEASING.md)

## Ссылки

- Репозиторий: https://github.com/rainbownyashka/mewnbasemultiplayer
