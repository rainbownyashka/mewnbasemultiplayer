# MewnBase Multiplayer & World Loading — Full Technical Documentation

Подробный техдок по мультиплееру, загрузке мира и связанным подсистемам проекта MewnBase.
Документ включает архитектуру, протоколы, последовательности вызовов, типичные проблемы, рекомендации по тестированию и расширению.

---

## Содержание
- Введение
- Архитектура мультиплеера
- Протокол обмена (handshake + UTF-сообщения)
- Сервер: компоненты, жизненный цикл, ClientHandler
- Клиент: жизненный цикл, потоки, обработка сообщений
- In-process сервер (host mode) — особенности
- Система сохранений и загрузки мира (GameLoader)
- Точки интеграции с игровым циклом (GameScreen, World, Hud)
- Частые ошибки и способы их устранения
- Рекомендации по логированию и тестированию
- Точки расширения и API для модов
- Quick Reference — самые важные файлы и методы
- Примеры логов и расследование инцидентов
- План автоматического теста и проверок

---

## Введение
Этот документ описывает рефакторинг/анализ деобфусцированного/декомпилированного исходного кода и как работать с мультиплеерной подсистемой. Он предназначен для разработчиков, которые хотят понять архитектуру и внести изменения или отладить сетевой стек.

Цели:
- Понять, как сервер и клиент взаимодействуют
- Прописать точные последовательности handshake и загрузки
- Указать ключевые точки, где обычно возникают гонки и NPE
- Дать набор практических шагов и тестов

---

## Архитектура мультиплеера

Основные компоненты:
- Сервер (`com.cairn4.moonbase.Server`) — прослушивает TCP-порт, принимает клиентов, создает `ClientHandler` для каждого клиента и ретранслирует сообщения.
- Клиент (`com.cairn4.moonbase.Client`) — подключается к серверу, читает начальные блобы (если есть), затем читает/пишет UTF-сообщения; имеет Reader/Writer потоки.
- `GameScreen` — при включённом мультиплеере создаёт клиент (reflection) и предоставляет интерфейсы для добавления/обновления удалённых игроков (`addPlayer`, `updatePlayerPosition` и т.д.).
- `GameLoader` — управляет сохранениями/загрузкой мира и тесно связан с тем, как клиент применяет incoming blobs.

Коммуникация разделена на бинарный handshake (initial save и world data) и текстовый протокол для игровых событий.

---

## Протокол обмена

1) Binary handshake (optional): сервер при подключении может отправить последовательность:
   - int clientId
   - int gameSaveLen; if >0 then bytes of gameSave
   - int worldDataLen; if >0 then bytes of worldData

   Клиент обязан прочитать эти блоки перед использованием `readUTF()` т.к. данные идут до текстовой части.

2) Текстовый протокол: UTF-строки, обычно с `writeUTF/readUTF`.

Частые payloads:
- `CONNECTED:<id>` / `DISCONNECTED:<id>`
- `SPAWNREMOTE` / `SPAWNREMOTE:<id>`
- `APPEARANCE:<face>|<color>|<nick>` / на сервере пересылается как `APPEARANCE:<clientId>:...`
- `REQUEST_APPEARANCE:<id>`
- `POS:PLAYER:<id>:<x>:<y>:<vx>:<vy>`
- `CHAT:<encNick>:<encText>`
- `TILE_BUILD_START:...`, `TILE_REMOVE:...`, `ITEM_DROP:...` — обновления мира
- `ANIM:...`, `ANIMPLAY:...`, `FLIP:...` — анимации и отражение
- `INVENTORY_UPDATE:` — сериализованные данные инвентаря

Примечание: сервер обычно добавляет префикс `<clientId>:` при ретрансляции сообщений, чтобы клиенты знали источник.

---

## Сервер (Server.java)

Ключевые точки:
- `start()` — запускает accept loop в отдельном потоке; `listening=true` после bind.
- `clients` — потокобезопасная map `ConcurrentHashMap<Integer, ClientHandler>`.
- `ClientHandler`:
  - `sendInitialWorldData()` — может синхронно сохранить snapshot мира в файлы и отправить их клиенту.
  - В `run()` реализует цикл чтения `in.readUTF()` и обработку payload.
  - `sendMessage` синхронизирован по `outLock`.

In-process host:
- `setGameScreen(GameScreen)` прикрепляет экран к серверу и запускает периодический broadcast POS от host (ID=0).
- Сервер тогда может применять входящие `TILE_*`/`ITEM_DROP` локально через `Gdx.app.postRunnable`.

Проблемы и рекомендации:
- Всегда использовать `postRunnable` при работе с `GameScreen`/`World` (render thread requirement).
- Логи: убедиться, что `Gdx.app.log("Server", ...)` появляется при bind/accept и в catch-блоках.

---

## Клиент (Client.java)

Жизненный цикл:
- `new Client(host, port, screen)` создаёт объект. `connect()` запускает connector thread.
- Connector делает `socket.connect(host,port)`, затем читает `clientId`, опциональные блобы (gameSave/worldData) и записывает их в `saves/multiplayer_received/`.
- После чтения начальных блобов запускается `readerThread` (читает UTF) и `senderThread` (отправляет POS ~ 50ms).

Обработка сообщений:
- Reader вызывает `handleMessage(msg)` который маршрутизирует payloadы (SPAWNREMOTE, APPEARANCE, POS, INVENTORY_UPDATE, TILE_*) и применяет эффекты на `GameScreen` через `postRunnable`.

Проблемы:
- Если `GameScreen` уже начал загрузку мира до того как client закончил запись `multiplayer_received`, происходит гонка — `GameLoader` не находя файла возвращает ошибку.

Рекомендации:
- Клиент должен либо:
  - дождаться записи файлов перед созданием GameScreen (synchronous fetch), либо
  - создать GameScreen в режиме ожидания и применить deferred load после получения файлов (non-blocking).

---

## Система сохранений (GameLoader)

Структура:
- saves/<folder>/gameSave.json|gameSave.data
- saves/<folder>/worldData.json
- saves/<folder>/screenshot.png

Основные функции:
- `getSavedGames()` — сканирует папки, читает файлы и возвращает `GameSaveData` список
- `getGameSaveData(folder)` — загружает `gameSave.json` и парсит
- `loadGame(World w)` — основная функция восстановления мира из `GameSaveData`:
  - создает `Mission` и применяет настройки
  - загружает `worldData` через `loadWorldDataFile`
  - вызывает `world.spawnPlayer(...)` и `loadEntities(...)`

Гонки:
- Если `currentSaveFolder == "multiplayer_received"` и файлов нет, `getGameSaveData` вернёт null — это основной источник ошибок при подключении.

Рекомендации:
- Сделать проверку на наличие файлов и отложить `loadGame` до их появления (we implemented варианты и sync fetch).

---

## Точки интеграции: GameScreen, World, Hud

- `GameScreen` создает `World` и, если `MoonBase.isMultiplayer`, создает `Client` через reflection и вызывает `connect()`.
- `World` в конструкторе вызывает `gameLoader.loadGame(this)` если `newGame==false`.
- `Hud` предполагает, что `world.player` уже создан; обращения до этого приводят к NPE.

Важно: порядок создания объектов критичен: сначала файлы сейва должны быть на диске, затем `GameScreen`/`World` могут безопасно вызвать `GameLoader.loadGame`

---

## Частые ошибки и их исправления (summary)

1) NPE в `Hud.activate` — фикс: проверять `player!=null` и `game.getCurrentMission()!=null`.
2) "Game failed to load, returning to main menu" — причина: `GameLoader.getGameSaveData` не нашёл `gameSave.json` (гонка). Рекомендуем:
   - Синхронный fetch before creating GameScreen (blocking), либо
   - Deferred load in World: poll for file existence or subscribe на завершение записи.
3) Нет логов подключения на сервере — причина: клиент не делает connect или firewall. Решение: добавить диагностику в `Client.connect` и в `Server.start`.

---

## Рекомендации по логированию и тестированию

- Добавить подробные Gdx.app.log в `Client.connect`:
  - перед socket.connect (host:port)
  - после соединения
  - при исключениях
- В `ClientHandler.sendInitialWorldData()` логировать размеры и запись файлов
- Автоматический smoke test: старт сервера на случайном порту, старт клиента, assert рукопожатие и запись файлов.

---

## API / точки расширения

- `Server.broadcast` — точка для добавления фильтрации сообщений или модерации.
- `Client.handleMessage` — центральная точка добавления новых payload handler-ов.
- `GameLoader` — место для добавления обратной совместимости save форматов.

---

## Quick Reference — самые полезные файлы/методы

- `com/cairn4/moonbase/Server.java` — `start()`, `broadcast()`, `setGameScreen()`
- `com/cairn4/moonbase/Client.java` — `connect()`, `handleMessage()`, `sendMessage()`
- `com/cairn4/moonbase/GameLoader.java` — `getGameSaveData()`, `loadGame(World)`
- `com/cairn4/moonbase/ui/MultiplayerConfigMenu.java` — entrypoint UI
- `com/cairn4/moonbase/ui/GameScreen.java` — reflection-based client creation, `addPlayer`, `updatePlayerPosition`

---

## Примеры логов и сценарии расследования

1) Клиент видит "can't find save data for folder multiplayer_received":
   - Проверьте, что `Client` записал `gameSave.json`: ищите логи `Client` о `gameSave.json received` или проверяйте файловую систему `saves/multiplayer_received/`.
   - Если нет записи — проверьте сетевое соединение, firewall и серверный `ClientHandler.sendInitialWorldData()`.

2) Сервер не показывает "Client connected":
   - netstat на порту; проверить, что сервер слушает
   - если слушает — проверить блокировки СКПТ/файрвола

---

## План автоматического smoke-test (предлагаю реализовать)

1) Start `Server` on port P (in-process)
2) Create `Client(host=127.0.0.1, port=P, fakeGameScreen)` and call `connect()`
3) Wait for handshake: expect `clientId>0` and initial blob lengths
4) Verify files `saves/multiplayer_received/gameSave.json` and `worldData.json` exist
5) Validate that `Client` receives `CONNECTED`/`APPEARANCE` messages

---

Если нужно, я могу продолжить: вставить диаграммы последовательности (ascii), подробный список всех payloads с примерами форматов, и/или сгенерировать `build.gradle` и smoke-test JUnit harness. Скажите, что предпочитаете — продолжу расширять этот документ или добавлю тестовый скрипт.


