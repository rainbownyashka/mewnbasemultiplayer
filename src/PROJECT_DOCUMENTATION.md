# MewnBase — Полная документация проекта

Этот документ представляет собой развёрнутую документацию по проекту MewnBase (decompiled). Она предназначена для разработчиков, которые будут поддерживать, рефакторить или расширять проект. Включены описания архитектуры, основных модулей, паттернов, точек расширения, а также инструкции по сборке, тестированию и отладке.

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
- `decompiled/com/cairn4/moonbase/` — исходные файлы Java после decompilation. Основная логика приложения.
- `decompiled/com/cairn4/moonbase/ui/` — UI-классы (меню, экраны, HUD)
- `game/` — собранный JAR и запускаемые ресурсы (в distrib)
- `saves/` — папка с сохранениями во время запуска (runtime)
- `MULTIPLAYER_FULL.md` — подробная мультиплеер-документация (сгенерирована ранее)

---

## Архитектура приложения (high-level)
1) DesktopLauncher -> инициализация платформы и `MoonBase`.
2) `MoonBase` инициализирует `Settings`, `AssetManagerSingleton` и загружает ресурсы фронтенда.
3) Показывается `MainMenu` -> оттуда пользователь может начать новую игру, загрузить save или открыть `MultiplayerConfigMenu`.
4) При старте игры: `LoadingScreen` загружает ресурсы, затем `GameScreen` создаётся и инициализирует `World`.
5) `World` строит мир: `ChunkLoader` грузит чанк-данные (`worldData.json`) и `GameLoader` загружает `gameSave`.
6) В мультиплеере: `GameScreen` создаёт `Client` (reflection) и подключается к `Server`.

---

## Основные подсистемы и файлы

1) Инициализация
- `MoonBase.java` — главный класс приложения: хранит глобальные настройки, current mission, флаги мультиплеера (`isMultiplayer`, `multiplayerHost`, ...), управление ресурсами.
- `DesktopLauncher` — entrypoint с `main(...)`.

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
- Формат: `saves/<folder>/gameSave.json` (или `.data` — base64), `saves/<folder>/worldData.json`, `screenshot.png`
- `GameLoader.getSavedGames()` — читает и парсит JSON
- `LoadGameMenu.loadGame(folder)` — UI-триггер загрузки

5) Мультиплеер
- `Server.java` — прослушивает порт, управляет `ClientHandler`
- `ClientHandler` — читает/пишет initial blobs и текстовый протокол
- `Client.java` — подключается, читает initial blobs и запускает reader/sender
- Протокол: бинарный handshake + UTF-сообщения для игровых событий

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
1) `DesktopLauncher.main` — инициализация платформы и создание `MoonBase`.
2) `MoonBase` считывает настройки (`SettingsLoader`), инициализирует `AssetManagerSingleton`.
3) Показывается `Splash` затем `MainMenu`.
4) При выборе Load/Continue:
   - `LoadGameMenu` собирает список `GameSaveData` через `GameLoader.getSavedGames()`.
   - После выбора `LoadGameMenu.loadGame(saveFolder)` устанавливает `MoonBase.currentSaveFolder` и открывает `LoadingScreen`.
   - `LoadingScreen.finished()` создаёт `GameScreen(game, false)`.
5) `GameScreen` конструктор:
   - инициализирует атласы/сцены
   - создаёт `World(this, newGame)`
   - и если `MoonBase.isMultiplayer`, создаёт `Client` (reflection) и вызывает `connect()`.
6) `World` конструктор:
   - на newGame — вызывает `makeEmptyWorld()`; на загрузку — вызывает `gameScreen.gameLoader.loadGame(this)` → полная инициализация мира.

---

## Протоколы и форматы файлов
- `gameSave.json` — JSON с `GameSaveData` классом (playerData, list of entities, tech, day cycle и т.д.)
- `worldData.json` — JSON со структурой чанков
- Транспорт поверх TCP: комбинированный бинарный handshake и text UTF

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
1) Добавить Gradle build (рекомендую) — управлять libGDX и LWJGL зависимостями.
2) Создать smoke-test harness (JUnit/Java) стартующий Server+Client и проверяющий handshake.
3) Внести четкое разделение responsibilities: Network layer (Client/Server) не должен напрямую читать/писать файлы; вынести persistence в отдельный компонент.
4) Добавить контракт/интерфейс для message handlers (чтобы легко добавлять новые payloads).

---

## Quick reference (ключевые файлы)
- `com/cairn4/moonbase/MoonBase.java` — глобальные флаги/параметры
- `com/cairn4/moonbase/Server.java` — сервер
- `com/cairn4/moonbase/Client.java` — клиент
- `com/cairn4/moonbase/GameLoader.java` — загрузчик/сохранение
- `com/cairn4/moonbase/ui/GameScreen.java` — создает world, client
- `com/cairn4/moonbase/ui/MultiplayerConfigMenu.java` — UI для подключения

---

Файл сохранён как `decompiled/PROJECT_DOCUMENTATION.md`. Если хотите, я могу:
- Сконвертировать в PDF/HTML.
- Добавить диаграммы sequence flow (mermaid) прямо в md.
- Сгенерировать build.gradle и smoke-test.


