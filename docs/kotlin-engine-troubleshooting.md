# Kotlin-движок недоступен: диагностика

## Подтверждено по присланному latest.log

Среда: Minecraft 26.3, Fabric Loader 0.20.1, Brokke 3.0.
Также загружен fabric-language-kotlin 1.14.2+kotlin.2.4.21.

При selfTest системы скриптов, до компиляции TriggerBot, загрузчик не находит:

```text
java.lang.ClassNotFoundException: kotlin.script.dependencies.ScriptContents
java.lang.ClassNotFoundException: kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory
Kotlin-движок не запустился: the Kotlin .kts script engine is not on the classpath
```

Позже тот же сбой показан для script-triggerbot.

Это подтверждает недоступность требуемых классов через загрузчик Brokke, а не синтаксическую ошибку TriggerBot. Из лога нельзя установить, отсутствуют ли библиотеки физически, не включены ли они в classpath, либо недоступны из-за упаковки/изоляции загрузчиков. Наличие Fabric Language Kotlin не доказывает наличие отдельного JSR223-движка скриптов.

## Что делать пользователю

1. Закрыть Minecraft. Сохранить резервную копию профиля и скриптов.
2. Проверить обновления/восстановление файлов через официальный лаунчер Brokke, если такие действия доступны.
3. Проверить запуск официального профиля с рекомендованными разработчиком зависимостями без дополнительных пользовательских модов. Не удалять исходный профиль для проверки.
4. Если ошибка повторилась, передать поддержке текст ниже. Исправление может потребовать обновления сборки или официальных зависимостей.
5. Не устанавливать случайные Kotlin JAR в mods и не смешивать версии библиотек. Не менять Java наугад: этот лог не доказывает, что причиной является её версия.

## Сообщение разработчику

> Minecraft 26.3, Fabric Loader 0.20.1, Brokke 3.0. Fabric Language Kotlin 1.14.2+kotlin.2.4.21 загружен. При ScriptManager.selfTest() движок скриптов падает до компиляции пользовательского кода: ClassNotFoundException kotlin.script.dependencies.ScriptContents (ScriptClasspath.addByName/build) и ClassNotFoundException kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory (ScriptEngineHost.create). Итог: the Kotlin .kts script engine is not on the classpath. Проверьте комплектность/версии scripting-зависимостей и их доступность загрузчику движка. Какая официальная сборка или установка зависимостей исправляет это?

## После восстановления движка

Повторить загрузку script-triggerbot.kts. Возможные ошибки компиляции самого скрипта разбирать отдельно по полному сообщению со строкой. Успешное восстановление движка ещё не подтверждает корректность скрипта в игре.

Полный пользовательский лог намеренно не добавлен в Git: он содержит локальные пути и другие данные, не нужные для публичной диагностики.
