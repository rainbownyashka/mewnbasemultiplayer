# MewnBase — Полная документация проекта

Этот документ описывает текущую структуру репозитория и runtime-поток запуска MewnBase Multiplayer. Он полезен для поддержки, рефакторинга и проверки того, как исходники в `src/com/cairn4/moonbase` связаны с `basegame/`, `work/` и `tools/`.

Важно:
- часть дерева `src/` состоит из вендорнутых библиотек и декомпилированных зависимостей;
- если что-то конфликтует с кодом, актуальнее смотреть текущие исходники и `docs/STATUS.md`;
- старые пути вида `decompiled/...` в этом документе больше не используются.

---

## Содержание
- Введение
- Структура репозитория и важные директории
- Архитектура приложения
- Основные подсистемы и файлы (с подробностями)
  - Инициализация приложения: `MoonBase`, `DesktopLauncher`
  - UI и экраны: `FrontendScreen`, `GameScreen`, `LoadingScreen`, `Menu`
  - Мир и данные: `World`, `Chunk`, `ChunkLoader`, `GameLoader`
  - Сохранения: `GameLoader`, `LoadGameMenu`, структура папки `saves/`
  - Мультиплеер: `Server`, `Client`, `ClientHandler`, `MultiplayerConfigMenu`
  - Игрок и сущности: `Player`, `Entity` и связанные классы
  - Рендер и ресурсы: `AssetManagerSingleton`, `ShaderHelper`, `TextureAtlas` и пр.
  - Вспомогательные утилиты: `SettingsLoader`, `Input`, `DayCycle`, `Tutorial`
- Жизненный цикл старта: от запуска JAR до открытия `GameScreen`
- Протоколы, форматы файлов и сериализация
- Рекомендации по разработке и CI (build, run, test)
- Руководство по отладке (сценарии, что проверять, логи)
- Планы рефакторинга (высокоуровневые)
- Quick reference — список ключевых файлов и функций

---

## Введение
MewnBase — игра с поддержкой одиночной и мультиплеерной игры. Код декомпилирован, поэтому структура и имена были восстановлены, но могут содержать артефакты. Документ охватывает ключевые элементы и объясняет их взаимодействие.

Цели документации:
- Дать обзор для новых разработчиков
- Описать порядок и формат данных (сейвы, worldData)
- Разъяснить сетевой стек и протокол
- Предложить безопасные паттерны интеграции для дальнейших изменений

---

## Структура репозитория и важные директории
- `src/com/cairn4/moonbase/` — основная игровая логика, мультиплеер, мир, сущности, сохранения.
- `src/com/cairn4/moonbase/ui/` — UI-классы: меню, экраны, HUD, попапы.
- `src/com/cairn4/moonbase/worlddata/` — сериализуемые модели мира, сейвов и сетевых данных.
- `src/com/cairn4/moonbase/entities/` — игровые сущности, транспорт, NPC, AI и pathfinding.
- `basegame/` — локальная копия дистрибутива игры и патченый jar для запуска.
- `work/` — распакованный jar, который пересобирается автоскриптом.
- `out/` — временный каталог для `.class` после `javac`.
- `tools/` — автокомпиляция, ручной патч, служебные скрипты.
- `docs/` — пользовательские и операционные инструкции.
- `repos/MewnbaseModded/` — отдельный patch/loader-проект, не основной runtime.

---

## Архитектура приложения (high-level)
1) `DesktopLauncher` поднимает LWJGL3-конфиг и создаёт `MoonBase`.
2) `MoonBase` инициализирует ассеты, настройки, cursors, audio, RPC и проверяет JVM-параметры автозапуска.
3) Показывается `Splash`, затем `MainMenu`, либо сразу `LoadingScreen`, если включён `INSTANT_RUN`.
4) `LoadingScreen` готовит ресурсы и создаёт `GameScreen`.
5) `GameScreen` создаёт `World`, HUD и, если включён мультиплеер, поднимает `Client`; в серверных сценариях может стартовать `Server`.
6) `World` загружает `GameLoader`, `ChunkLoader` и данные мира из сейвов/чанков.

---

## Основные подсистемы и файлы

1) Инициализация
- `MoonBase.java` — главный класс приложения: глобальные настройки, current mission, флаги мультиплеера (`isMultiplayer`, `multiplayerHost`, ...), управление ресурсами.
- `DesktopLauncher` — entrypoint с `main(...)`, конфиг LWJGL3 и режимы `headless/serverOnly`.

2) UI и экраны
- `FrontendScreen` — главный меню экран
- `LoadingScreen` — экран загрузки ресурсов и подготовка `GameScreen`
- `GameScreen` — основной игровой экран: инициализация world, hud, client/server attach
- `MultiplayerConfigMenu` — UI для ввода IP/port/nick, опции Create Server
- `Hud` — отображение состояния игрока, инфо и уведомлений

3) Мир и загрузка
- `World` — управление чанками, сущностями, dayCycle, terrainGen
- `Chunk` / `ChunkLoader` — загрузка чанков из `worldData` и управление видимостью
- `GameLoader` — чтение/запись save файлов, worldData, функции загрузки и сохранения

4) Сохранения
- Формат: `saves/<folder>/gameSave.json` или `gameSave.data`, `worldData.json` и служебные файлы мира.
- Для мультиплеера используется временная папка `multiplayer_received`, которую клиент заполняет данными от хоста.
- `GameLoader.getSavedGames()` — читает и парсит JSON.
- `LoadGameMenu.loadGame(folder)` — UI-триггер загрузки.

5) Мультиплеер
- `Server.java` — прослушивает порт, управляет `ClientHandler`.
- `ClientHandler` — читает/пишет initial blobs и текстовый протокол.
- `Client.java` — подключается, читает initial blobs и запускает reader/sender.
- `net/ProtocolV2.java` — текущая рамка для части сообщений (`MB2|...`) и READY-handshake.
- Протокол сочетает бинарный handshake и UTF-сообщения для игровых событий.

6) Игрок и сущности
- `Player` — управление входом игрока, инвентарь, взаимодействия
- `Entity` и подклассы — NPC, Creature, ItemPickup, ItemPile
- `Item`, `ItemFactory` — данные об итемах

7) Рендер и ресурсы
- `AssetManagerSingleton` — управление ассетами через libGDX AssetManager
- `ShaderHelper`, `HeatDistortEffect`, `SkeletonRenderer` и др — визуальные эффекты

8) Утилиты
- `SettingsLoader`, `SettingsData` — настройки игры
- `SaveFixer` — утилита для починки старых save форматов

---

## Жизненный цикл старта (пошагово)
1) `DesktopLauncher.main` создаёт конфиг, выбирает `serverOnly/headless` режимы и запускает `Lwjgl3Application`.
2) `MoonBase.create()` грузит настройки, ассеты, курсоры и опциональные debug-сервисы.
3) Если включён `INSTANT_RUN`, игра уходит в `LoadingScreen`, иначе открывает `Splash`.
4) `LoadGameMenu` собирает список `GameSaveData` через `GameLoader.getSavedGames()`.
5) После выбора сейва `LoadingScreen.finished()` создаёт `GameScreen(game, false)`.
6) `GameScreen` создаёт `World(this, newGame)` и, если `MoonBase.isMultiplayer`, подключает `Client`.
7) `World` либо строит пустой мир, либо вызывает `gameLoader.loadGame(this)` и поднимает полное состояние мира.

---

## Протоколы и форматы файлов
- `gameSave.json` / `gameSave.data` — данные сейва (`GameSaveData`: playerData, entities, tech, day cycle и т.д.).
- `worldData.json` — JSON со структурой чанков и состояния мира.
- Для мультиплеера используется TCP-транспорт с handshake, blob-sync и текстовыми событиями; часть событий уже переведена на MB2 framing.

---

## Руководство по отладке
1) Проблема: "can't find save data for folder multiplayer_received"
   - Проверьте, созданы ли файлы `saves/multiplayer_received/gameSave.json` и `worldData.json`.
   - Посмотрите логи клиента (`Client.connect`): были ли записаны блобы?
   - Если сервер запущен in-process — проверьте, что `ClientHandler.sendInitialWorldData()` действительно записывает файлы.

2) NPE в `Hud.activate`
   - Проверьте порядок создания `GameScreen`/`World`/`player`.
   - Убедитесь, что `world.player` не null перед вызовом HUD-методов.

3) Сервер не видит подключений
   - netstat, firewall, порт слушается ли
   - логи сервера на `accept` и `IOException`

---

## Рекомендации по рефакторингу и CI
1) Добавить единый build pipeline, который компилирует в `out/` и пакует jar из `basegame/ + out/`.
2) Создать smoke-test harness (JUnit/Java), который стартует `Server + Client` и проверяет handshake.
3) Разделить network layer и persistence: `Client/Server` не должны напрямую отвечать за хранение файлов.
4) Добавить контракт/интерфейс для message handlers, чтобы проще расширять сетевые payloads.

---

## Quick reference (ключевые файлы)
- `src/com/cairn4/moonbase/MoonBase.java` — глобальные флаги/параметры.
- `src/com/cairn4/moonbase/desktop/DesktopLauncher.java` — entrypoint.
- `src/com/cairn4/moonbase/Server.java` — сервер.
- `src/com/cairn4/moonbase/Client.java` — клиент.
- `src/com/cairn4/moonbase/GameLoader.java` — загрузчик/сохранение.
- `src/com/cairn4/moonbase/ui/GameScreen.java` — создаёт world, HUD и сетевой слой.
- `src/com/cairn4/moonbase/ui/MultiplayerConfigMenu.java` — UI для подключения.

---

Файл находится в `src/PROJECT_DOCUMENTATION.md`. Если хотите, я могу:
- Сконвертировать в PDF/HTML.
- Добавить диаграммы sequence flow (mermaid) прямо в md.
- Сгенерировать build.gradle и smoke-test.


