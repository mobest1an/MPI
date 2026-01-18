## Профилирование удалённого (docker) ресурса
Производится с помощью visualvm

Скачать можно вот здесь https://visualvm.github.io/

Запускается командой вида: ` C:\Users\Ivan\Documents\visualvm_22\bin\visualvm.exe --jdkhome "C:\Program Files\Java\jdk-17.0.1" --userdir "C:\Users\Ivan"`

В существующий docker-compose для app добавлена конфигурация портов 9091,
чтобы подключиться необходимо `Add JMX Connection`, конфигурация `localhost:9010`, аунтификация и ssl отключены.

Для проведения профилирования необходимо:
1) Выбрать вкладку `Sampler` в созданной конфигурации
2) Выбрать CPU/MEM
3) Дать нагрузку на приложение
4) Нажать на Stop
5) Нажать на Snapshot

