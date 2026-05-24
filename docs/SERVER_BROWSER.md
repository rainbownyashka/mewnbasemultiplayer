# Зеркало списка серверов (VPS 300 MB)

## Идея

На VPS **не** крутится сама игра и мир — только **зеркало**: маленький HTTP-сервис с табличкой «кто сейчас хостит».

| Где | Что делает | RAM |
|-----|------------|-----|
| **VPS (до 300 MB на процесс)** | `tools/mp_registry_server.py` — список IP:порт | ~20–50 MB |
| **ПК хоста** | MewnBase + Radmin VPN + TCP **7777** | как обычно |
| **ПК гостя** | Кнопка **Browse online list** → Connect | как обычно |

8 GB на VPS — с запасом; лимит **300 MB** на зеркало более чем достаточен.

## Запуск зеркала на VPS

```bash
# Python 3 уже есть на большинстве VPS
cd /opt/mewnbasemultiplayer
python3 tools/mp_registry_server.py --host 0.0.0.0 --port 8080
```

Проверка:

```bash
curl http://127.0.0.1:8080/health
curl http://127.0.0.1:8080/servers
```

Откройте порт **8080** (или свой) в firewall/security group.

### systemd (опционально, лимит 300M)

```ini
[Unit]
Description=MewnBase MP registry mirror
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/mewnbasemultiplayer
ExecStart=/usr/bin/python3 tools/mp_registry_server.py --port 8080
MemoryMax=300M
Restart=always

[Install]
WantedBy=multi-user.target
```

## Настройка у хоста (игра на своём ПК)

1. Radmin VPN — как в [PLAY_WITH_FRIENDS.md](PLAY_WITH_FRIENDS.md).
2. В игре: **MULTIPLAYER → Create Server (7777)**.
3. В ярлыке / `run.bat` добавьте (подставьте свой Radmin IP и URL VPS):

```text
-Dmewnbase.mp.registry=http://ВАШ_VPS:8080
-Dmewnbase.mp.advertiseHost=26.12.34.56
-Dmewnbase.mp.serverName=Мой мир
```

Игра раз в ~45 с шлёт heartbeat на зеркало: имя, Radmin IP, порт, число игроков.

## Настройка у гостя

```text
-Dmewnbase.mp.registry=http://ВАШ_VPS:8080
```

В меню: **MULTIPLAYER → Browse online list** → выбрать сервер → **Connect**.

## Зависание при загрузке мира

Если гость «висит» на загрузке:

1. **Хост** должен полностью зайти в мир до приглашения (сейв записан).
2. Большой сейв — подождать 1–3 мин (таймаут увеличен под размер файла).
3. У хоста есть `gameSave.json` или `gameSave.data` в папке сейва.
4. У гостя удалить `saves/multiplayer_received` и подключиться снова.
5. В логе хоста не должно быть `No gameSave for folder`.

Исправления в коде: отправка `.data`-сейва, запись `worldData_p<id>.json`, явная ошибка вместо вечного ожидания.

## Безопасность

Зеркало публичное — любой может зарегистрировать фейковый IP. Для друзей этого обычно хватает; для публичного хаба позже можно добавить пароль/токен в POST.
