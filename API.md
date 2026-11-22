# Скрипты Brokke.cc на Kotlin

Скрипт - это обычный файл Kotlin с расширением .kts. Клиент компилирует его прямо во время игры: сохранил файл - скрипт перезагрузился, перезапуск не нужен.
Один файл - один скрипт. Он выглядит как отдельный модуль: со своими настройками, биндом и включателем во вкладке Скрипты.

```
name("Привет")
description("Мой первый скрипт")

chat.print("скрипт загружен")

on<ClientTickEvent> {
    // выполняется каждый тик, пока скрипт включён
}
```

### Что можно сделать
Свой модуль: настройки, бинд, включатель во вкладке Скрипты - разделы Основы и Настройки.
Реакции на игру: тики, атаки, клавиши, движение, пакеты, чат, появление сущностей - раздел События.
Чтение мира: игрок, блоки, сущности, инвентарь, сервер, таб - разделы Игрок, Мир, Инвентарь, Сервер.
Действия: удары, установка и ломание блоков, повороты головы, смена слотов, отправка пакетов - разделы Управление, Взаимодействие, Повороты, Пакеты.
Своя графика: HUD, рамки и линии в мире - раздел Рендер.
Свои команды в чате с подсказками - раздел Команды.
Настоящие модули клиента прямо из скрипта: module("Имя", ModuleCategory.COMBAT) { } - раздел Модули из скриптов.
Свои бинды, HUD-элементы и окна с кнопками - разделы Бинды, HUD-элементы, Окна.
Ожидание без блокировки игры: launch { waitTicks(20); waitUntil { ... } } - раздел Корутины.
HTTP-запросы, друзья, точки, навигация, конфиги - разделы HTTP, Интеграция с клиентом.
Графики в HUD и окнах, скриншоты, Discord-вебхуки и Telegram-боты - разделы Графики, Скриншоты, Discord, Telegram.

### Где лежат скрипты
Папка: .minecraft/brokke/scripts. Кнопка Папка во вкладке Скрипты открывает её.
Имя файла - это id скрипта: target-hud.kts показывается как target-hud, пока ты не задашь name(...).
Состояние сохраняется между запусками: включён ли скрипт, бинд и все настройки.

### Вкладка Скрипты
ЛКМ по скрипту - открыть код. ПКМ - настройки. Крестик при наведении - удалить.
Пока скрипт компилируется, у него жёлтая точка. Ошибка делает точку красной, а текст ошибки с номером строки появляется в консоли и во вкладке Настройки.
В редакторе: Ctrl+S - сохранить и перезапустить, Ctrl+Z / Ctrl+Y - отмена и повтор, Ctrl+D - дублировать строку, Tab / Shift+Tab - отступ.

## Основы
Скрипт выполняется сверху вниз один раз при загрузке. В этот момент он объявляет имя, настройки, обработчики событий и команды. Сам код логики живёт внутри обработчиков.

### Описание скрипта
Эти функции можно вызывать только на верхнем уровне скрипта, не внутри обработчиков.
| Функция | Что делает |
|---|---|
| name("Имя") | имя в списке скриптов |
| description("Текст") | описание, видно во вкладке Настройки |
| key(Key.G) | бинд по умолчанию, игрок может поменять |
| bind(Key.G) | то же, что key |
| requireApi(4) | не загружать скрипт, если версия API клиента ниже (сейчас 4) |
| author("Ник") | автор, виден в консоли и MCP |
| version("1.2") | версия скрипта |
| dependsOn("lib", "other") | нужные скрипты: если их нет, в консоли будет предупреждение |

### Жизненный цикл
| Функция | Когда вызывается |
|---|---|
| onEnable { } | при каждом включении скрипта |
| onDisable { } | при каждом выключении |
| onUnload { } | при выгрузке: файл изменён, удалён или игра закрывается |

### Состояние скрипта
| Член | Тип | Описание |
|---|---|---|
| id | String | id скрипта, имя файла без .kts |
| enabled | Boolean | включён ли скрипт, можно присвоить |
| toggle() | Unit | переключить скрипт |
| inGame | Boolean | игрок и мир загружены |
| whenInGame { } | Unit | выполнить блок, только если inGame |

```
name("Таймер включения")
var start = 0L

onEnable {
    start = client.millis()
    notify("Включён")
}

onDisable {
    val sec = (client.millis() - start) / 1000
    chat.print("Скрипт работал $sec сек")
}
```

### Песочница
import писать не нужно и нельзя: всё API уже доступно.
Запрещены: net.minecraft.*, java.io, java.nio.file, java.net, потоки, Thread.sleep, рефлексия, System.exit, ClassLoader.
Обработчик, который работает дольше 250 мс, выключает скрипт. 5 ошибок подряд тоже выключают скрипт.

## События
Подписка: on<Событие> { e -> ... }. Обработчик работает только пока скрипт включён, сам отписывается при выключении.

```
on<AttackEvent> { e ->
    chat.print("удар!")
}

val sub = on<ClientTickEvent> { }
sub.unsubscribe()
```

### Параметры подписки
| Параметр | Тип | Описание |
|---|---|---|
| priority | Int | порядок вызова: Priority.FIRST, EARLY, NORMAL, LATE, LAST |
| ignoreCancelled | Boolean | true - вызывать даже если событие уже отменено |

### Тики
| Событие | Когда |
|---|---|
| ClientTickEvent | каждый тик клиента (20 в секунду), даже в меню |
| PlayerTickEvent | тик игрока, до движения. PrePlayerTickEvent - то же |
| PostPlayerTickEvent | после тика игрока |

### Движение и повороты
| Событие | Поля | Описание |
|---|---|---|
| RotationEvent | yaw, pitch | поворот, который уйдёт на сервер. Можно менять - это серверные повороты |
| SyncEvent | yaw, pitch | то же самое, что RotationEvent |
| PostSyncEvent | - | после отправки позиции на сервер |
| MoveInputEvent | forward, strafe, isJump, isSneak, isSprint | ввод движения за тик. Можно менять: forward = 1f - идти вперёд |
| LookEvent | yaw, pitch, originalYaw, originalPitch | движение мыши. cancel() - заблокировать поворот камеры |

```
on<MoveInputEvent> { e ->
    if (e.forward > 0f) e.isSprint = true
}

on<RotationEvent> { e ->
    val target = world.nearestEntity(player.position(), 6.0, filters.enemyPlayers()) ?: return@on
    val rot = rotations.lookAt(target)
    e.yaw = rot.yaw()
    e.pitch = rot.pitch()
}
```

### Бой и ввод
| Событие | Поля | Описание |
|---|---|---|
| AttackEvent | player, target | ты ударил сущность. Entity(e.target) - обёртка для удобной работы |
| KeyEvent | key, action | нажатие клавиши или кнопки мыши. action: 1 - нажата, 0 - отпущена |
| ChatReceiveEvent | message, component | сообщение в чате. message - текст без цветов |

### Пакеты
| Событие | Методы | Описание |
|---|---|---|
| PacketReceiveEvent | packet(), type(), cancel() | пакет от сервера |
| PacketSendEvent | packet(), type(), cancel() | пакет на сервер |
type() - короткое имя класса пакета, например ClientboundSetHealthPacket.

### Мир и сущности
| Событие | Методы | Описание |
|---|---|---|
| WorldLoadEvent | dimension() | вход в мир или смена измерения |
| EntitySpawnEvent | entity() | сущность появилась рядом |
| EntityRemoveEvent | entity() | сущность исчезла |
| ModuleToggleEvent | name(), enabled(), isModule, isScript | включение или выключение скрипта или модуля клиента |
| DimensionChangeEvent | from(), to() | смена измерения (не первый вход) |
| EntityStatusEvent | entity(), status() | статус сущности от сервера (35 - тотем, 2 - урон и т.д.) |
| BlockUpdateEvent | x(), y(), z(), oldId(), newId(), broken(), placed() | сервер изменил блок |
| ExplosionEvent | center(), radius(), knockback(), distanceToPlayer(), cancel() | взрыв рядом. cancel() убирает и откидывание |

### Игрок
| Событие | Методы | Описание |
|---|---|---|
| HealthChangeEvent | oldHealth(), newHealth(), damage(), healed(), isDamage, isHeal, food(), saturation() | сервер прислал новое здоровье |
| DamageEvent | entity(), type(), attacker(), direct(), isSelf, byPlayer() | любая сущность получила урон. type - id типа урона |
| DeathEvent | message(), position() | ты умер |
| RespawnEvent | dimension(), dimensionChanged() | возрождение или смена мира |
| TotemPopEvent | entity(), name(), pops(), isSelf | сработал тотем. pops() - сколько раз с захода |
| VelocityEvent | x, y, z (можно менять), scale(h, v), original(), cancel() | сервер толкнул игрока (отдача) |
| TeleportEvent | from(), to(), distance() | сервер переместил игрока |
| ItemPickupEvent | collector(), itemId(), amount(), isSelf | кто-то подобрал предмет |

### Чат, экраны, звуки
| Событие | Методы | Описание |
|---|---|---|
| ChatSendEvent | message (можно менять), original(), isCommand, cancel() | ты отправляешь сообщение или /команду (без /) |
| TitleEvent | kind(), text(), isTitle, isSubtitle, isActionBar, replace("текст"), cancel() | тайтл, сабтайтл или текст над хотбаром |
| SoundPlayEvent | id(), category(), position(), volume(), pitch(), isSound(id), contains(часть), distanceToPlayer(), cancel() | любой звук клиента |
| ScreenOpenEvent | kind(), title(), screenClass(), isChat, isContainer, cancel() | открывается экран. kind: CHAT, INVENTORY, CONTAINER, SCRIPT, OTHER |
| ScreenCloseEvent | kind(), title() | экран закрылся |
| ContainerOpenEvent | containerId(), type(), title(), titleContains(часть) | сервер открыл меню (сундук, аукцион) |
| SlotUpdateEvent | containerId(), slot(), item(), playerInventory() | сервер изменил слот |
| BossBarEvent | uuid() | изменился боссбар. Данные - game.server().bossBars() |
| ScoreboardEvent | objective(), owner(), score(), removed() | изменилась строка скорборда |
ChatReceiveEvent тоже можно менять: e.replace("новый текст") заменит сообщение в чате.

### Действия и ввод
| Событие | Методы | Описание |
|---|---|---|
| BlockBreakEvent | block(), x(), y(), z(), id(), cancel() | ты сломал блок |
| BlockInteractEvent | block(), side(), hand(), heldItem(), placing(), cancel() | ПКМ по блоку (установка или использование) |
| EntityInteractEvent | entity(), hand(), cancel() | ПКМ по сущности |
| ItemUseEvent | hand(), itemId(), isItem(id), cancel() | использование предмета в руке |
| MouseScrollEvent | vertical(), horizontal(), up(), down(), inScreen(), cancel() | прокрутка колёсика |
| ServerConnectEvent | address(), singleplayer() | вход на сервер |
| ServerDisconnectEvent | address() | выход с сервера |

### Короткие формы
onHealthChange { e -> }, onDamage, onDeath, onRespawn, onTotemPop, onPlayerJoin { имя -> }, onPlayerLeave { имя -> }, onChat, onChatSend, onTitle, onSound, onScreenOpen, onScreenClose, onContainerOpen, onItemPickup, onBlockBreak, onBlockInteract, onAttack { entity -> }, onServerConnect { адрес -> }, onServerDisconnect { }, onDimensionChange, onVelocity, onExplosion, onTeleport, onScroll, onKeyPress { key -> }, onKeyRelease { key -> }, onModuleToggle.
Отменяемые события: ChatReceiveEvent, ChatSendEvent, TitleEvent, SoundPlayEvent, ScreenOpenEvent, BlockBreakEvent, BlockInteractEvent, EntityInteractEvent, ItemUseEvent, VelocityEvent, ExplosionEvent, MouseScrollEvent, AttackEvent, пакеты.

```
onTotemPop { e -> if (!e.isSelf) chat.print(e.name() + " снёс тотем (" + e.pops() + ")") }
onVelocity { e -> e.scale(0.0, 1.0) }
onChatSend { e -> if (e.message == "кд") { e.cancel(); chat.print("кулдаун: " + player.attackCooldown()) } }
onSound { e -> if (e.isSound("entity.generic.explode")) e.cancel() }
```

### Рендер
| Событие | Методы | Описание |
|---|---|---|
| Render2DEvent | render(), width(), height(), tickDelta() | отрисовка HUD, каждый кадр |
| Render3DEvent | render(), camera(), tickDelta() | отрисовка в мире, каждый кадр |

```
on<ChatReceiveEvent> { e ->
    if (e.message.contains("вас вызвал на дуэль")) chat.sendToServer("/duel accept")
}

on<WorldLoadEvent> { e -> log.info("мир: " + e.dimension()) }
```

## Настройки
Настройки появляются во вкладке Настройки скрипта (ПКМ по скрипту) и сохраняются между запусками.
Удобнее всего читать их через by - тогда переменная всегда содержит текущее значение.

```
val enabledMsg by checkBox("Сообщение", true)
val speed by slider("Скорость", 1.5f, 0f, 5f, 0.1f)
val delay by slider("Задержка", 5, 1, 20)
val range by rangeSlider("Диапазон", 2f, 4f, 0f, 6f)
val text by input("Текст", "привет")
val mode by selectable("Режим", "Обычный", "Быстрый", selected = "Обычный")
val targets by combo("Цели", "Игроки", "Мобы", "Животные", selected = listOf("Игроки"))
val color by colorPicker("Цвет", 0xFF55FFFF)
hotkey("Телепорт", Key.H) { chat.runCommand("/home") }
button("Сбросить счётчик") { storage.put("n", 0) }
```

### Виды настроек
| Функция | Значение | Описание |
|---|---|---|
| checkBox(name, default) | Boolean | переключатель |
| slider(name, default, min, max, step) | Float | дробный ползунок, step по умолчанию 0.01 |
| slider(name, default, min, max) | Float | целый ползунок (передай Int) |
| rangeSlider(name, from, to, min, max) | ClosedFloatingPointRange | два значения: от и до |
| input(name, default) | String | текстовое поле |
| selectable(name, vararg варианты, selected) | String | выбор одного варианта |
| combo(name, vararg варианты, selected) | List<String> | выбор нескольких вариантов |
| colorPicker(name, 0xAARRGGBB) | Int | цвет ARGB |
| hotkey(name, клавиша) { } | - | отдельная клавиша, вызывает блок при нажатии |
| button(name) { } | - | кнопка в настройках |

### Вложенные настройки
Если первым аргументом передать checkBox, настройка будет видна только когда он включён.
```
val extra = checkBox("Доп. опции")
val power by slider(extra, "Сила", 1f, 0f, 2f)
val note by input(extra, "Заметка")
```

### Работа с объектом настройки
Без by функция возвращает объект настройки.
| Метод | Описание |
|---|---|
| value() | текущее значение |
| value(v) | изменить значение из кода |
| onChange { v -> } | вызывается при каждом изменении |
| visibleWhen { условие } | показывать только при условии |
| toggle() | для checkBox - переключить |
| select("вариант"), has("вариант"), options() | для selectable |
| has("вариант"), set("вариант", true) | для combo |
| intValue(), postfix("ms") | для slider |
| from(), to() | для rangeSlider |
| press() | для button и hotkey - нажать |

```
val mode = selectable("Режим", "Тихо", "Громко")
val volume = slider("Громкость", 1f, 0f, 2f).postfix("x")
volume.visibleWhen { mode.has("Громко") }
mode.onChange { m -> chat.print("режим: $m") }
```

## Игрок
player - твой персонаж. Все методы читают живые данные при каждом вызове. Вне мира используй проверку inGame или whenInGame.

### Позиция и движение
| Метод | Тип | Описание |
|---|---|---|
| player.position() | Vec | позиция ног |
| player.x(), y(), z() | Double | координаты |
| player.blockX(), blockY(), blockZ() | Int | координаты блока под ногами |
| player.dimension(), biome(), totemPops() | String / Int | измерение, биом, сколько тотемов снесено с захода |
| player.eyePosition() | Vec | позиция глаз |
| player.previousPosition() | Vec | позиция в прошлом тике |
| player.renderPosition() | Vec | сглаженная позиция для рендера |
| player.velocity() | Vec | скорость в блоках за тик |
| player.yaw(), pitch() | Float | поворот головы |
| player.rotation() | Rotation | yaw и pitch вместе |
| player.box() | AABB | хитбокс |
| player.width(), height() | Float | размер хитбокса |
| player.distanceTo(entity) | Double | расстояние до сущности |
| player.distanceTo(vec) | Double | расстояние до точки |
| player.fallDistanceBlocks() | Float | высота текущего падения |
| player.hasMovementInput() | Boolean | нажаты клавиши движения |

### Состояние
| Метод | Тип | Описание |
|---|---|---|
| player.onGround() | Boolean | стоит на земле |
| player.sneaking(), sprinting() | Boolean | присед, бег |
| player.swimming(), climbing() | Boolean | плывёт, лезет по лестнице |
| player.flying(), gliding() | Boolean | полёт, элитры |
| player.riding() | Boolean | сидит на сущности |
| player.inWater(), inLava() | Boolean | в воде, в лаве |
| player.usingRiptide() | Boolean | рывок трезубцем |
| player.alive(), dead() | Boolean | жив или мёртв |
| player.creative() | Boolean | креатив |
| player.gameMode() | String | survival, creative, adventure, spectator |
| player.airTicks() | Int | запас воздуха |

### Здоровье, еда, опыт
| Метод | Тип | Описание |
|---|---|---|
| player.health(), maxHealth() | Float | здоровье в половинках сердец |
| player.absorption() | Float | жёлтые сердца |
| player.armorPoints() | Int | очки брони 0..20 |
| player.hurtTicks() | Int | сколько тиков осталось анимации урона |
| player.food() | Int | голод 0..20 |
| player.saturation() | Float | насыщение |
| player.xpLevel(), xpProgress() | Int, Float | уровень и прогресс опыта |

### Эффекты
| Метод | Тип | Описание |
|---|---|---|
| player.hasEffect("minecraft:speed") | Boolean | есть ли эффект |
| player.effect(id) | Effect? | эффект или null |
| player.effects() | List<Effect> | все эффекты |
| effect.id(), name() | String | id и название |
| effect.amplifier() | Int | уровень, 0 - первый |
| effect.durationTicks(), infinite() | Int, Boolean | длительность |
| effect.beneficial() | Boolean | полезный ли |

### Бой и предметы
| Метод | Тип | Описание |
|---|---|---|
| player.attackCooldown() | Float | заряд удара 0..1 |
| player.belowMinimumAttackCharge() | Boolean | удар ещё слишком слабый |
| player.entityReachBlocks() | Double | дальность удара |
| player.blockReachBlocks() | Double | дальность до блоков |
| player.usingItem(), usingHand(), itemUseTicks() | | ест, пьёт, натягивает лук |
| player.blocking() | Boolean | блокирует щитом |
| player.held() | String | id предмета в руке |
| player.mainHandItem(), offHandItem() | ItemStack | предметы в руках |
| player.armorItems(), armorItem(slot) | ItemStack | броня |
| player.isNaked() | Boolean | без брони |

### Прочее
| Метод | Тип | Описание |
|---|---|---|
| player.name(), uuid(), id() | | ник, uuid, id сущности |
| player.pingMs() | Int | твой пинг |
| player.respawn() | Unit | возродиться после смерти |

```
on<ClientTickEvent> {
    whenInGame {
        if (player.health() < 8f && !player.hasEffect("minecraft:regeneration")) {
            notify("Мало здоровья!", NotifyKind.WARN)
        }
    }
}
```

## Мир и сущности
world - загруженный мир. Сущность - объект Entity, блок - объект Block.

### Мир
| Метод | Тип | Описание |
|---|---|---|
| world.loaded() | Boolean | мир загружен |
| world.block(x, y, z), block(vec) | Block | блок по координатам |
| world.isFree(box) | Boolean | в области нет блоков |
| world.entities() | List<Entity> | все сущности рядом |
| world.players() | List<Entity> | все игроки, кроме тебя |
| world.entityById(id) | Entity? | сущность по id |
| world.entitiesNear(центр, радиус, фильтр) | List<Entity> | сущности в радиусе, ближние первыми |
| world.nearestEntity(центр, радиус, фильтр) | Entity? | ближайшая сущность |
| world.time() | Long | время суток |
| world.isDay(), isRaining() | Boolean | день, дождь |
| world.dimension() | String | minecraft:overworld, the_nether, the_end |
| world.playSound(id, громкость, высота) | Unit | проиграть звук только себе |

### Сущность (Entity)
| Метод | Тип | Описание |
|---|---|---|
| id(), uuid(), name() | | id, uuid, имя |
| type() | String | тип, например minecraft:zombie |
| position(), eyePosition(), renderPosition() | Vec | позиции |
| velocity() | Vec | скорость |
| yaw(), pitch() | Float | поворот |
| box(), width(), height() | | хитбокс |
| health(), maxHealth() | Float | здоровье (для живых) |
| alive(), onGround(), sneaking(), sprinting() | Boolean | состояние |
| inWater(), onFire() | Boolean | в воде, горит |
| isPlayer(), isLiving(), isSelf() | Boolean | игрок, живое существо, это ты |
| isAlly() | Boolean | в списке друзей клиента |
| distanceTo(vec), squaredDistanceTo(vec) | Double | расстояние |

### Блок (Block)
| Метод | Тип | Описание |
|---|---|---|
| id() | String | id блока, например minecraft:stone |
| isA("diamond_ore") | Boolean | проверка типа, minecraft: можно не писать |
| x(), y(), z(), position() | | координаты |
| air(), liquid() | Boolean | воздух, жидкость |
| replaceable() | Boolean | можно поставить блок поверх |
| blocksMovement() | Boolean | твёрдый |
| hardness() | Float | прочность, -1 - неломаемый |
| box() | AABB | форма блока в мире |

### Фильтры
Готовые фильтры для поиска сущностей - объект filters.
| Фильтр | Кого пропускает |
|---|---|
| filters.attackable() | живых, кроме тебя и друзей |
| filters.enemyPlayers() | живых игроков, кроме друзей |
| filters.players() | всех игроков, кроме тебя |
| filters.living() | всех живых существ |
| filters.allies() | друзей |
| filters.ofType("zombie") | сущности одного типа |
| filters.any() | всех |
Свой фильтр - обычная лямбда: { it.isPlayer() && it.health() < 10f }.

```
val ore = world.block(player.position().add(0.0, -1.0, 0.0))
if (ore.isA("diamond_ore")) notify("Алмазы под ногами!")

val enemies = world.entitiesNear(player.position(), 20.0, filters.enemyPlayers())
chat.print("Врагов рядом: " + enemies.size)
```

## Векторы и коробки
Vec - точка или направление (x, y, z). AABB - прямоугольная область.
| Код | Описание |
|---|---|
| Vec(1.0, 2.0, 3.0) | создать вектор |
| v.x, v.y, v.z | координаты |
| v.add(x, y, z), v.add(other) | сложение |
| v.subtract(other), v.scale(k) | вычитание, умножение |
| v.distanceTo(other), v.length() | расстояние, длина |
| v.normalize() | единичный вектор |
| AABB(x1, y1, z1, x2, y2, z2) | создать область |
| box.inflate(d), box.move(x, y, z) | расширить, сдвинуть |
| box.contains(vec), box.intersects(other) | проверки |
| box.center | центр |

## Управление
control - нажатие клавиш движения и прямое движение.
| Метод | Описание |
|---|---|
| control.jump() | прыгнуть сейчас, если на земле |
| control.jump(true/false) | зажать или отпустить пробел |
| control.sneak(bool), sneaking(bool) | присед: клавиша / сразу состояние |
| control.sprint(bool), sprinting(bool) | бег: клавиша / сразу состояние |
| control.forward(bool), backward(bool), left(bool), right(bool) | клавиши движения |
| control.useKey(bool), attackKey(bool) | ПКМ и ЛКМ |
| control.velocity(x, y, z) | задать скорость напрямую |
| control.look(yaw, pitch) | повернуть камеру |
| control.lookAt(vec) | повернуть камеру на точку |
Зажатые клавиши остаются зажатыми. Отпускай их в onDisable.

```
onEnable { control.forward(true) }
onDisable { control.forward(false) }
```

## Взаимодействие
interaction - удары, предметы, блоки. Руки: "main" - основная, "off" - вторая.
| Метод | Описание |
|---|---|
| interaction.attack(entity) | ударить сущность и взмахнуть рукой |
| interaction.attackCrosshair() | ударить того, на кого смотришь |
| interaction.swing("main") | взмах рукой |
| interaction.useItem("main") | использовать предмет (как ПКМ в воздух) |
| interaction.stopUsingItem() | перестать есть, пить, натягивать лук |
| interaction.placeBlock(x, y, z, сторона, рука) | поставить блок, сторона: UP, DOWN, NORTH, SOUTH, EAST, WEST |
| interaction.breakBlock(x, y, z, сторона) | начать ломать блок |
| interaction.interactEntity(entity, рука) | ПКМ по сущности |

```
on<PlayerTickEvent> {
    val target = world.nearestEntity(player.eyePosition(), 3.0, filters.attackable()) ?: return@on
    if (player.attackCooldown() >= 0.95f) interaction.attack(target)
}
```

## Повороты
rotations считает углы, а raycast проверяет, что видно.
| Метод | Тип | Описание |
|---|---|---|
| rotations.lookAt(vec) | Rotation | углы, чтобы смотреть на точку |
| rotations.lookAt(entity) | Rotation | углы на центр сущности |
| rotations.current() | Rotation | текущий поворот игрока |
| rotations.yawDifference(a, b) | Float | разница yaw с учётом 360 |
| rotations.distance(r1, r2) | Float | насколько повороты отличаются |
| rotation.yaw(), pitch() | Float | углы |
| rotation.direction() | Vec | направление взгляда |
| Rotation(yaw, pitch) | Rotation | создать поворот |
Плавный поворот: сдвигай yaw к цели на небольшой шаг каждый тик в RotationEvent.

### Лучи (raycast)
| Метод | Тип | Описание |
|---|---|---|
| raycast.crosshair(дистанция) | Hit | на что смотрит прицел |
| raycast.crosshair(дистанция, блоки, фильтр) | Hit | с фильтром сущностей |
| raycast.entityAtCrosshair(дистанция) | Entity? | сущность под прицелом |
| raycast.from(rotation, дистанция, блоки) | Hit | луч из глаз под заданным углом |
| raycast.blocks(от, до, форма, жидкости) | Hit | луч только по блокам |
| raycast.canSee(от, до) | Boolean | между точками нет стен |
| raycast.hitOn(entity, от, до) | Vec? | точка попадания в хитбокс |
| hit.isBlock(), isEntity(), isMiss() | Boolean | во что попал |
| hit.position(), distance() | | точка и расстояние |
Для блока: (hit as Hit.OnBlock).blockX(), blockY(), blockZ(), side(). Для сущности: (hit as Hit.OnEntity).entity().

## Инвентарь
inventory - хотбар и инвентарь. Слоты хотбара 0..8.
| Метод | Тип | Описание |
|---|---|---|
| inventory.selectedSlot() | Int | выбранный слот |
| inventory.selectSlot(n) | Unit | выбрать слот хотбара |
| inventory.held() | String | id предмета в руке |
| inventory.item(слот) | String | id предмета в слоте |
| inventory.count(слот) | Int | количество в слоте |
| inventory.find("totem_of_undying") | Int | первый слот с предметом, -1 если нет |
| inventory.findInHotbar(id) | Int | то же, только хотбар |
| inventory.busy() | Boolean | открыт экран и инвентарь занят |
| inventory.stack(слот), heldItem(), offhandItem() | ItemInfo | предмет целиком. Слоты: 0..8 хотбар, 9..35 рюкзак, 36..39 броня (39 - шлем), 40 - левая рука |
| inventory.items(), hotbar(), armor() | List | списки ItemInfo |
| inventory.findAll(id), find { item -> }, findByName(часть), bestSlot { item -> оценка } | | поиск |
| inventory.total(id), has(id) | Int / Boolean | сколько всего предметов |
| inventory.firstEmpty(), firstEmptyHotbar(), emptySlots(), full() | | свободное место |
| inventory.select(id) | Boolean | взять предмет в руку: выбрать в хотбаре или перенести в текущий слот |
| inventory.swap(слот, хотбар), moveToHotbar(id, хотбар) | | поменять местами со слотом хотбара |
| inventory.toOffhand(id), swapOffhand(слот), swapHands() | | левая рука (swapHands - как клавиша F) |
| inventory.move(откуда, куда), quickMove(слот) | | перенести предмет, shift-клик |
| inventory.drop(слот), dropStack(слот), dropHeld(всё), dropAll(id) | | выбросить |
| inventory.equip(слот или id) | Boolean | надеть броню |
| inventory.open(), close(), inventoryOpen(), scroll(направление) | | экран инвентаря, прокрутка хотбара |
| inventory.menuSlot(слот) | Int | номер слота инвентаря в открытом меню, -1 если недоступен |

### ItemInfo - подробности предмета
id(), name(), count(), maxCount(), damage(), durability(), durabilityPercent(), enchanted(), isA(id), nameContains(часть), а также:
| Метод | Описание |
|---|---|
| lore(), loreContains(часть) | строки описания |
| enchantments(), enchantLevel("sharpness"), hasEnchant(id) | чары: Map id -> уровень (включая книги) |
| customName(), hasCustomName(), rarity() | имя и редкость |
| components(), hasComponent("food"), component("custom_data") | компоненты предмета |
| nbt(), customData() | изменённые компоненты и custom_data строкой |
| isBlock(), isFood(), isArmor(), equipmentSlot(), cooldown() | тип предмета и кулдаун 0..1 |

### Очередь кликов
clickQueue(задержка_в_тиках) выполняет клики по одному с паузой - для серверов с античитом на инвентарь.
| Метод | Описание |
|---|---|
| click(слот), rightClick, shiftClick, swap(слот, хотбар), swapOffhand, drop, dropAll, move(из, в) | слоты открытого меню, как в container |
| then { } | свой шаг |
| waitTicks(n), waitUntil { условие } | пауза |
| onDone { }, clear(), size(), busy(), pause(true), delay(n) | управление |
```
val q = clickQueue(3)
on<ContainerOpenEvent> { e ->
    if (e.titleContains("награда")) q.waitTicks(5).then { container.lootAll() }.onDone { container.close() }
}
```

```
name("Auto Totem Slot")
on<PlayerTickEvent> {
    if (player.health() > 10f) return@on
    val slot = inventory.findInHotbar("totem_of_undying")
    if (slot >= 0 && inventory.selectedSlot() != slot) inventory.selectSlot(slot)
}
```
## Пакеты
Чтение и отмена пакетов - события PacketReceiveEvent и PacketSendEvent. Отправка - game.packets().
| Метод | Описание |
|---|---|
| e.packet() | сам пакет |
| e.type() | имя класса пакета |
| e.cancel() | отменить: пакет не дойдёт |
| game.packets().send(packet) | отправить пакет на сервер |
```
on<PacketReceiveEvent> { e ->
    if (e.type() == "ClientboundSoundPacket") e.cancel()
}
```

## Сервер и игра
game - экраны, сервер, таб. gameSettings - настройки игры.
| Метод | Тип | Описание |
|---|---|---|
| game.screenOpen() | Boolean | открыт любой экран |
| game.screenKind() | String | NONE, CHAT, INVENTORY, CONTAINER, OTHER |
| game.screenTitle() | Text | заголовок экрана, например сундука |
| game.closeScreen() | Unit | закрыть экран |
| game.serverAddress(), serverName() | String | адрес и имя сервера |
| game.singleplayer() | Boolean | одиночная игра |
| game.server().tabList() | TabList | таб |
| tabList.header(), footer() | String | шапка и подвал таба |
| tabList.players() | List | игроки: name(), uuid(), pingMs(), gameMode(), displayName(), team(), isFriend() |
| tabList.size(), find(ник), contains(ник), names() | | поиск в табе |
| game.server().brand() | String | ядро сервера (Paper, Spigot...) |
| game.server().tps(), lagMillis(), lagging() | Double / Long / Boolean | оценка TPS сервера и лаг |
| game.server().pingMs(), onlineCount(), motd(), connected() | | пинг, онлайн |
| game.server().scoreboard() | Scoreboard | present(), title(), lines(), cleanLines(), find(часть), scores() |
| game.server().bossBars(), bossBar(часть) | List / BossBar | name, progress, percent(), color, uuid |
| gameSettings.perspective() | CameraType | вид от 1 или 3 лица |
| gameSettings.perspective(THIRD_PERSON_BACK) | Unit | сменить вид |
| gameSettings.mouseSensitivity() | Double | чувствительность 0..1 |

### Клиент
| Метод | Тип | Описание |
|---|---|---|
| client.tick() | Long | номер тика с запуска |
| client.millis(), nanos() | Long | время |
| client.fps() | Float | FPS |
| client.inGame(), singleplayer() | Boolean | |
| client.version() | String | версия Minecraft |
| client.apiVersion() | Int | версия API скриптов |
| client.clipboard(), clipboard("текст") | String | буфер обмена |
| client.title(заголовок, подзаголовок, fadeIn, stay, fadeOut) | Unit | показать тайтл себе |
| client.actionBar(текст), clearTitle() | Unit | текст над хотбаром |
| client.command(".goto 0 64 0") | Unit | выполнить команду клиента (с точкой или без) |
| client.commands() | List | все команды клиента и скриптов |
| client.playUiSound("ui.button.click", 1f) | Unit | звук интерфейса |
| client.screenWidth(), screenHeight(), windowWidth(), windowHeight(), focused() | | размеры окна |
| client.usedMemoryMb(), maxMemoryMb(), playerName(), date("HH:mm") | | память, ник, время |
| client.disconnect() | Unit | выйти с сервера в главное меню |

## Клавиши
Константы клавиш: Key.A..Key.Z, Key.NUM_0..NUM_9, Key.F1..F12, Key.SPACE, ENTER, TAB, ESCAPE, BACKSPACE, DELETE, UP, DOWN, LEFT, RIGHT, LEFT_SHIFT, LEFT_CONTROL, LEFT_ALT.
Мышь: Key.MOUSE_LEFT, MOUSE_RIGHT, MOUSE_MIDDLE, MOUSE_4, MOUSE_5.
| Метод | Тип | Описание |
|---|---|---|
| keys.isDown(Key.W) | Boolean | зажата ли клавиша |
| keys.mouseDown(0) | Boolean | зажата ли кнопка мыши: 0 - ЛКМ, 1 - ПКМ |
| keys.inputBlocked() | Boolean | открыт экран (чат, инвентарь) |
| Key.name(код), Key.fromName("G") | | имя клавиши и обратно |
| keys.ctrl(), shift(), alt() | Boolean | зажаты ли модификаторы |
| keys.isDown("ctrl"), anyDown(a, b), allDown(a, b) | Boolean | проверка по имени и наборам |
| keys.name(код), code("F5"), isMouse(код), mouseX(), mouseY() | | имена и позиция курсора |
Ещё клавиши: Key.INSERT, HOME, END, PAGE_UP, PAGE_DOWN, CAPS_LOCK, GRAVE, MINUS, EQUALS, COMMA, PERIOD, SLASH, BACKSLASH, SEMICOLON, APOSTROPHE, LEFT_BRACKET, RIGHT_BRACKET, NUMPAD_0..NUMPAD_9, NUMPAD_ENTER, RIGHT_SHIFT, RIGHT_CONTROL, RIGHT_ALT.

### Бинды
bind - своя клавиша скрипта прямо в коде, без настройки. Работает, пока скрипт включён, и по умолчанию не срабатывает в открытых экранах.
| Функция | Описание |
|---|---|
| bind(Key.R) { } | нажатие клавиши или кнопки мыши |
| bind(Key.F, ctrl = true, shift = false, alt = false) { } | с модификаторами (сравниваются точно) |
| bind("ctrl+shift+K") { }, bind("mouse4") { } | то же строкой: ctrl, shift, alt, lmb, rmb, mmb, mouse4, mouse5, f1..f12, space... |
| bindHold(Key.MOUSE_4) { } | каждый тик, пока зажато |
| b.onRelease { }, b.whileHeld { }, b.onPress { } | дополнительные обработчики |
| b.inScreens(true), b.repeat(true) | работать в меню, повторять при удержании |
| b.held(), heldTicks(), heldMillis(), name() | состояние |
| b.active(false), b.unbind() | выключить, удалить |
```
val b = bind(Key.V) { control.sneak(true) }
b.onRelease { control.sneak(false) }
bind("ctrl+shift+H") { chat.print("HP: " + player.health()) }
```

```
hotkey("Друг под прицелом", Key.MOUSE_MIDDLE) {
    val e = raycast.entityAtCrosshair(50.0) ?: return@hotkey
    chat.runCommand("/friend add " + e.name())
}
```

## Рендер
### HUD (Render2DEvent)
Координаты - экран клиента, (0, 0) - левый верхний угол. Цвета - Int в формате 0xAARRGGBB.
| Метод | Описание |
|---|---|
| rect(x, y, w, h, цвет) | прямоугольник |
| roundedRect(x, y, w, h, радиус, цвет) | скруглённый прямоугольник |
| outline(x, y, w, h, цвет, толщина) | рамка |
| roundedOutline(x, y, w, h, радиус, цвет, толщина) | скруглённая рамка |
| gradient(x, y, w, h, лв, пв, пн, лн) | градиент по 4 углам |
| circle(cx, cy, радиус, цвет) | круг |
| arc(cx, cy, радиус, старт, доля, цвет) | дуга-прогресс, доля 0..1 |
| shadow(x, y, w, h, радиус, размытие, разброс, цвет) | тень |
| blur(x, y, w, h, радиус) | размытие фона |
| text(x, y, размер, текст, цвет) | текст |
| boldText(...), centredText(центрX, y, размер, текст, цвет) | жирный и по центру |
| textWidth(текст, размер) | ширина текста |
| pushClip(x, y, w, h), popClip() | обрезать отрисовку областью |
| project(vec) | Vec экрана для точки мира или null за спиной |

```
on<Render2DEvent> { e ->
    val r = e.render()
    val x = 10f
    val y = 10f
    r.shadow(x, y, 90f, 22f, 5f, 8f, 0f, Colors.rgba(0, 0, 0, 120))
    r.roundedRect(x, y, 90f, 22f, 5f, Colors.rgba(15, 15, 20, 200))
    r.text(x + 6f, y + 7f, 7f, "FPS: " + client.fps().toInt(), Colors.WHITE)
}
```

### Мир (Render3DEvent)
| Метод | Описание |
|---|---|
| box(aabb, цвет, сквозьСтены) | рамка коробки |
| filledBox(aabb, цвет, сквозьСтены) | залитая коробка |
| entityBox(entity, цвет, сквозьСтены) | рамка вокруг сущности |
| line(от, до, цвет, сквозьСтены) | линия |
| tracer(точка, цвет, сквозьСтены) | линия от прицела |
| triangle(a, b, c, цвет, ...), quad(a, b, c, d, цвет, ...) | залитые фигуры |
| polygon(список точек, цвет, ...) | многоугольник |

```
on<Render3DEvent> { e ->
    for (p in world.players()) {
        val col = if (p.isAlly()) Colors.GREEN else Colors.RED
        e.render().entityBox(p, col, true)
        e.render().tracer(p.renderPosition(), Colors.withAlpha(col, 120), true)
    }
}
```

### HUD - дополнительно
| Метод | Описание |
|---|---|
| width(), height(), centerX(), centerY() | размер экрана |
| mouseX(), mouseY(), hovered(x, y, w, h) | мышь |
| roundedRect(x, y, w, h, лв, пв, пн, лн, цвет) | разные радиусы углов |
| panel(x, y, w, h, радиус, фон, рамка) | панель с рамкой |
| circleOutline(cx, cy, r, цвет, толщина) | окружность |
| arc(cx, cy, r, старт, доля, цвет, толщина) | дуга, старт в градусах теперь учитывается |
| roundedGradient, horizontalGradient(x, y, w, h, r, лево, право), verticalGradient | градиенты |
| blur(x, y, w, h, r, сила), glow(x, y, w, h, r, цвет) | размытие и свечение |
| progressBar(x, y, w, h, доля, фон, цвет) | полоса прогресса |
| line(x1, y1, x2, y2, толщина, цвет), gradientLine(...) | линии |
| polyline(точки, толщина, цвет, замкнуть), triangle(...), polygon(точки, цвет), regularPolygon(cx, cy, r, стороны, поворот, цвет) | фигуры, точки - vec(x, y, 0) |
| shadowText, gradientText(x, y, size, текст, лево, право), rainbowText(x, y, size, текст, периодМс) | стили текста |
| rightText, centeredText, multilineText(x, y, size, текст, цвет, отступ), label(x, y, size, текст, цвет, фон, отступ) | раскладка текста |
| textHeight(size), boldTextWidth(текст, size) | размеры |
| item(stack или "diamond_sword", x, y, размер) | предмет |
| head(entity, x, y, размер, радиус), selfHead(x, y, размер, радиус) | голова игрока |
| image(assets.image("logo.png"), x, y, w, h [, радиус, оттенок]) | PNG из папки ассетов скрипта |
| push(), pop(), translate(x, y), scale(sx, sy), scale(cx, cy, k), rotate(cx, cy, градусы), transformed { } | трансформации |
| withAlpha(0.5f) { }, clipped(x, y, w, h) { } | прозрачность и обрезка для блока кода |
| onScreen(vec), screenBox(aabb или entity) | проекция мира: [x, y, w, h] или null |
| entityBox2D(e, цвет, толщина), entityCorners2D(...), healthBar2D(e, ширина) | 2D ESP |
| worldText(vec, текст, size, цвет, фон), nametag(e, текст, size, цвет, фон) | текст над точкой мира |
| crosshair(размер, зазор, толщина, цвет), arrowTo(vec, радиус, размер, цвет) | прицел и стрелка к цели |
Незакрытые push() закрываются автоматически после события.

### Мир - дополнительно
Вся геометрия события собирается в один батч и рисуется одним вызовом в конце.
сквозьСтены = false проверяет видимость лучами от камеры (центр и углы коробки): примитив скрывается, только если не видна ни одна точка. Попиксельного depth-теста нет - всё рисуется поверх мира.
| Метод | Описание |
|---|---|
| lineWidth(2f), additive(true) | толщина линий по умолчанию, аддитивное свечение |
| box(aabb, цвет, стены, толщина), line(от, до, цвет, стены, толщина) | с толщиной |
| outlinedBox(aabb, заливка, рамка, стены), gradientBox(aabb, низ, верх, стены) | коробки |
| blockBox(x, y, z, цвет, стены), filledBlock(...), block(Block, заливка, рамка, стены) | блоки |
| entityFilledBox(e, заливка, рамка, стены), entityTracer(e, цвет, стены), entityCircle(e, цвет, толщина, высота) | сущности |
| gradientLine(от, до, c1, c2, толщина), path(точки, цвет, толщина) | линии |
| circle(центр, r, цвет, толщина [, сегменты]), filledCircle(центр, r, цвет), gradientCircle(центр, r, толщина, периодМс) | круги на земле |
| cylinder(низ, r, высота, цвет, толщина), sphere(центр, r, цвет, толщина), beam(низ, высота, низЦвет, верхЦвет, толщина), cross(центр, размер, цвет, толщина) | объёмные фигуры |
| visible(vec или aabb), cameraPosition() | утилиты |

### Шейдеры
shader(имя, glsl) создаёт фрагментный шейдер. Он компилируется при первой отрисовке, рисуется прямоугольником в HUD через e.render().shader(s, x, y, w, h) или s.draw(r, x, y, w, h, оттенок).
Можно писать как на Shadertoy - mainImage(out vec4 fragColor, in vec2 fragCoord) - или свой main(), записывая результат в fragColor.
| Встроено | Значение |
|---|---|
| vUv | координаты внутри прямоугольника 0..1, y вниз |
| vFragCoord | координаты в пикселях прямоугольника |
| vColor | оттенок из draw (по умолчанию белый) |
| iTime | секунды с запуска |
| iFrame | номер кадра |
| iResolution | vec3(ширина, высота, 1) |
| iScreen | размер экрана |
| iRect | vec4(x, y, w, h) |
| iMouse | vec4(x, y относительно прямоугольника, ЛКМ, ПКМ) |
Свои параметры: объяви в GLSL `uniform float speed;` (float, int, bool, vec2, vec3, vec4, до 13 штук) и задай из Kotlin: s.set("speed", 2f), s.set("tint", 1f, 0.5f, 0f), s.setColor("col", Colors.RED), s.set("pos", vec). Значения передаются для каждого вызова draw отдельно.
Также: s.additive(true) до первой отрисовки, s.ready(), s.failed(), s.error(), s.uniforms(), s.drawAt(r, vec, w, h) - шейдер над точкой мира.
Если ошибка компиляции - скрипт пишет её в консоль, а подробный текст GLSL-ошибки в logs/latest.log.
```
val wave = shader("wave", """
    uniform vec4 col;
    void mainImage(out vec4 o, in vec2 p) {
        vec2 uv = p / iResolution.xy;
        float v = 0.5 + 0.5 * sin(uv.x * 10.0 + iTime * 3.0);
        o = vec4(col.rgb * v, 0.8);
    }
""")
wave.setColor("col", Colors.AQUA)
onRender2D { e -> e.render().shader(wave, 10f, 10f, 120f, 40f) }
```
Продвинутый режим: если исходник содержит #version, он используется как есть. Входы фрагмента: location 0 vec2 brokkeUv, 1 vec4 brokkeColor, 2 flat int brokkeRow; выход location 0 fragColor; параметры - текстура Sampler0.

### Цвета
| Функция | Описание |
|---|---|
| Colors.WHITE, BLACK, RED, GREEN, BLUE, CYAN, YELLOW, ORANGE, PURPLE, PINK, GRAY | готовые цвета |
| Colors.rgb(r, g, b), rgba(r, g, b, a) | из каналов 0..255 |
| Colors.hsb(оттенок, насыщ, ярк) | из HSB 0..1, удобно для радуги |
| Colors.withAlpha(цвет, a), fade(цвет, 0..1) | прозрачность |
| Colors.mix(a, b, t) | смешать два цвета |
| Colors.red(c), green(c), blue(c), alpha(c) | разобрать цвет |

## Чат и уведомления
| Метод | Описание |
|---|---|
| chat.print(текст) | сообщение только тебе |
| chat.printPrefixed("[Скрипт]", текст) | с префиксом |
| chat.sendToServer(текст) | написать в чат сервера |
| chat.runCommand("/home") | выполнить команду сервера |
| notify(текст, NotifyKind.OK) | уведомление клиента: OK, INFO, SUCCESS, WARN, FAIL, ERROR, ACCENT, ENABLE, DISABLE |
| notify("Заголовок", текст, NotifyKind.SUCCESS, 3000) | уведомление со своим заголовком и временем показа (мс) |
| log.debug(...), log.debugEnabled(true) | отладочные строки, видны только когда включено |
| log.every("ключ", 1000, текст) | писать не чаще раза в интервал |
| log.info(...), warn(...), error(...) | запись в консоль вкладки Скрипты |

### Форматированный текст
text создаёт текст с цветом, стилем и кликами - для chat.print.
| Метод | Описание |
|---|---|
| text.literal("Привет") | обычный текст |
| text.legacy("&aЗелёный") | текст с цветовыми кодами |
| .color(Colors.RED), bold(true), italic(true), underlined(true) | стиль |
| .append(другой), append("строка") | склеить |
| .runCommand("/spawn"), suggestCommand(...), openUrl(...), copyToClipboard(...) | действие по клику |
| .hoverText("подсказка") | всплывающая подсказка |
| .string() | текст без форматирования |
```
chat.print(text.literal("[Нажми] ").color(Colors.GREEN).runCommand("/spawn")
    .append(text.literal("чтобы вернуться на спавн").color(Colors.GRAY)))
```

## Таймеры и задачи
| Функция | Описание |
|---|---|
| timer() | новый таймер |
| t.passed(тики), passedMillis(мс) | прошло ли время |
| t.passedAndReset(тики), passedMillisAndReset(мс) | прошло - и сбросить |
| t.elapsedTicks(), elapsedMillis() | сколько прошло |
| t.reset() | сбросить |
| afterTicks(n) { } | выполнить через n тиков |
| everyTicks(n) { } | выполнять каждые n тиков |
| nextTick { } | в следующем тике |
| onClientThread { } | в главном потоке игры |
| task.cancel() | отменить задачу |
Задачи работают только пока скрипт включён.

```
val t = timer()
on<ClientTickEvent> {
    if (t.passedMillisAndReset(60_000)) chat.sendToServer("/kit start")
}
```

## Команды
Свои команды в чате с точкой: .имя аргументы.
```
command("calc") {
    description("Сложить два числа")
    usage("<a> <b>")
    alias("c")
    runs { reply("Ответ: " + (doubleArg(0) + doubleArg(1))) }
    sub("help") { runs { reply("Пример: .calc 2 3") } }
    completes(0) { listOf("1", "10", "100") }
}
```
| Метод контекста | Описание |
|---|---|
| arg(i), argOr(i, по_умолчанию) | аргумент как строка |
| intArg(i), doubleArg(i), booleanArg(i) | аргумент нужного типа, при ошибке ответит игроку |
| argCount(), args(), rest(от) | все аргументы |
| reply(текст), replyError(текст) | ответ в чат |
| label() | как вызвали команду |

## Хранилище
storage - данные скрипта, которые переживают перезапуск игры. Отдельные файлы - config("имя").
| Метод | Описание |
|---|---|
| storage.put(ключ, значение) | записать String, Int, Long, Double, Boolean |
| storage.get(ключ, по_умолч), getInt, getLong, getDouble, getBoolean | прочитать |
| storage.has(ключ), keys(), remove(ключ), clear() | работа с ключами |
| storage.save(), load() | сохранить сразу или перечитать |
| config("имя") | отдельное хранилище с таким именем |
| configs.names(), exists(имя), delete(имя) | список сохранённых конфигов |
Хранилище само сохраняется при выгрузке скрипта.

## Ассеты
Ассеты - это файлы, которые скрипт читает: списки ответов, слов, ников, координат и любые другие данные. Так большие списки не приходится писать прямо в коде, а игрок может менять их без правки скрипта.
У каждого скрипта своя папка: scripts/assets/<id скрипта>/. Например, скрипт autootvet.kts читает файлы из scripts/assets/autootvet/.
```
scripts/
  autootvet.kts
  assets/
    autootvet/
      otveti.txt
      data/triggers.txt
```
Файлы перечитываются сами, когда ты их меняешь - перезапускать скрипт не нужно.

### Чтение
| Метод | Тип | Описание |
|---|---|---|
| assets.exists("otveti.txt") | Boolean | есть ли файл |
| assets.text("otveti.txt") | String | весь файл одной строкой |
| assets.textOr(файл, по_умолч) | String | текст или значение по умолчанию |
| assets.lines("otveti.txt") | List<String> | строки без пустых и без строк с # в начале |
| assets.rawLines(файл) | List<String> | все строки как есть |
| assets.randomLine("otveti.txt") | String | случайная строка из lines |
| assets.pairs("words.txt") | Map<String, String> | строки вида ключ=значение или ключ: значение |
| assets.list() | List<String> | все файлы в папке скрипта |
| assets.folder() | String | путь папки, например assets/autootvet/ |

### Запись
| Метод | Описание |
|---|---|
| assets.write(файл, текст) | создать или перезаписать файл |
| assets.writeLines(файл, список) | записать список строками |
| assets.append(файл, строка) | дописать строку в конец |
| assets.delete(файл) | удалить файл |
| assets.bytes(файл), assets.writeBytes(файл, байты) | двоичные файлы (картинки, архивы) - например для отправки в Telegram или Discord |
Можно использовать подпапки: assets.lines("data/triggers.txt"). Выйти за папку своего скрипта нельзя. Размер файла - до 4 МБ.

### Пример: авто-ответчик
Файл scripts/assets/autootvet/otveti.txt:
```
# ключевое слово = ответ
привет = здарова
как дела = норм, сам как?
продашь = пиши в лс
```
Скрипт autootvet.kts:
```
name("Авто-ответчик")
description("Отвечает по словам из assets/autootvet/otveti.txt")
val cooldown by slider("Задержка (сек)", 3, 1, 30)
val t = timer()

onEnable {
    if (!assets.exists("otveti.txt")) {
        assets.writeLines("otveti.txt", listOf("# ключевое слово = ответ", "привет = здарова"))
        chat.print("Создан файл " + assets.folder() + "otveti.txt")
    }
}

on<ChatReceiveEvent> { e ->
    val msg = e.message.lowercase()
    if (msg.contains(player.name().lowercase())) return@on
    for ((word, answer) in assets.pairs("otveti.txt")) {
        if (msg.contains(word.lowercase()) && t.passedMillisAndReset((cooldown * 1000).toLong())) {
            chat.sendToServer(answer)
            log.info("ответил на '$word'")
            return@on
        }
    }
}
```

## Модули клиента
Скрипт может управлять обычными модулями Brokke.
| Метод | Описание |
|---|---|
| client.modules().all() | все модули |
| client.modules().find("Sprint") | модуль или null |
| client.modules().get("Sprint") | модуль, ошибка если нет |
| m.name(), enabled(), bind() | данные модуля |
| m.setEnabled(true), toggle() | включить, переключить |
| m.settings() | имена настроек |
| m.checkBox("Игроки"), slider(...), selectable(...), combo(...), input(...), colorPicker(...) | доступ к настройке модуля |
| m.get("Range"), m.set("Range", 3.5), m.values(), m.settingType(имя) | любая настройка как строка, запись с сохранением |
| m.bind(Key.R), bindName(), holdMode(true), category(), description(), favorite(), fromScript() | бинд и данные |
| clientConfigs.list(), save("pvp"), load("pvp"), exists(имя), delete(имя), backup() | конфиги клиента (то же, что client.configs()) |
| client.scriptModules() | имена модулей, созданных скриптами |
```
on<ClientTickEvent> {
    val aura = client.modules().find("KillAura") ?: return@on
    if (player.health() < 6f && aura.enabled()) aura.setEnabled(false)
}
```

## Расширенные таймеры
| Функция | Что делает |
|---|---|
| everyTick { } | каждый тик, пока скрипт включён |
| everyMillis(500) { } / everySeconds(1.5) { } | периодически по реальному времени |
| afterMillis(1000) { } / afterSeconds(2.0) { } | один раз через указанное время |
| onBecome({ условие }) { } | когда условие стало true (срабатывает на фронте) |
| watch({ значение }) { новое -> } | вызывается, когда значение изменилось |
| onTick { } / onRender2D { e -> } / onRender3D { e -> } | короткие формы on<...> |
| onWorldLoad { dim -> } / onEntitySpawn { e -> } / onEntityRemove { e -> } | короткие формы событий |
Все функции возвращают ScriptTask - его можно отменить через cancel().

## Последовательности
sequence { } описывает цепочку шагов, которая выполняется по тикам без блокировки игры.
| Шаг | Что делает |
|---|---|
| run { } | выполнить код |
| wait(ticks) / waitMs(ms) | подождать |
| waitUntil(timeoutTicks) { условие } | ждать условия, timeout -1 = бесконечно |
| repeat(n) { } | выполнить n раз, по разу за тик |
| repeatWhile({ условие }) { } | повторять каждый тик, пока условие true |
| stopIf { условие } | остановить цепочку |
| loop(true) | крутить по кругу |
| whenDone { } | после завершения |
Управление: start(), stop(), cancel(), running(), finished(), step().
```
val combo = sequence {
    run { inventory.selectSlot(0) }
    wait(2)
    run { interaction.useItem("MAIN_HAND") }
    waitUntil(40) { !player.usingItem() }
    run { inventory.selectSlot(1) }
}
hotkey("Комбо", Key.R) { combo.start() }
```

## Математика
Объект math. Также глобально: clamp, lerp, random, randomInt, chance, vec.
| Функция | Описание |
|---|---|
| clamp, clamp01, lerp, inverseLerp, map, mapClamped | базовые операции |
| approach(cur, target, step), smooth(cur, target, k) | плавное приближение |
| round(v, digits), roundTo(v, step), sign, between, near, wrap | округление и диапазоны |
| wrapDegrees, angleDifference, approachAngle, lerpAngle | работа с углами |
| random(), random(a, b), randomInt(a, b), chance(p), gaussian(m, d), seed(n) | случайность |
| pick(list), weighted(items, weights) | случайный элемент |
| sum, average, median, stdDev | статистика по списку чисел |
| noise(x), noise(x, y) | плавный шум -1..1 |
| wave(periodMs), pingPong(t, len) | периодические значения |
| ease("outCubic", t) | функции плавности |
| direction(yaw, pitch), rotateY(v, deg), horizontalDistance, closestPoint(box, p) | векторы |
| boxAround(center, r), box(...), center(box), circle(center, r, n) | коробки и фигуры |
| predict(entity, ticks), intercept(from, entity, speed) | предсказание позиции |
Easing: linear, inSine, outSine, inOutSine, inQuad, outQuad, inOutQuad, inCubic, outCubic, inOutCubic, inQuart, outQuart, inOutQuart, inExpo, outExpo, inOutExpo, inCirc, outCirc, inBack, outBack, inElastic, outElastic, inBounce, outBounce, smoothstep, smootherstep.
Векторы поддерживают операторы: a + b, a - b, a * 2.0, a / 2.0, -a, val (x, y, z) = v, a distTo b.

## Анимации
| Функция | Описание |
|---|---|
| animation(0.0, 300, "outCubic") | анимация числа: animate(target), set(v), value(), floatValue(), done(), color(a, b) |
| spring(0.0, 180.0, 22.0) | пружина: target(v), impulse(v), value(), settled() |
| fadeToggle(250) | плавное появление: state(true/false), value(), visible() |
```
val anim = fadeToggle(200)
onRender2D { e ->
    anim.state(player.sneaking())
    if (anim.visible()) e.render().rect(10f, 10f, 100f * anim.floatValue(), 4f, Colors.rainbow())
}
```

## Утилиты
| Функция | Описание |
|---|---|
| rate(1000) | счётчик событий в окне: mark(), count(), perSecond() - например CPS |
| cooldowns() | именованные задержки: use("key", ms), ready, remaining, reset |
| counter() | подсчёт по ключам: add(key), get(key), top(n), total() |
| history<T>(n) | кольцевой буфер: add, latest, get(ago), all, average |
| stateMachine("idle") { state("idle") { }; enter("run") { } } | машина состояний: go(name), tick(), current(), ticksInState() |

## Строки
Объект strings.
| Функция | Описание |
|---|---|
| stripColors(s), colorize("&aтекст") | цветовые коды |
| containsIgnoreCase, containsAny(s, list), matches(s, regex) | поиск |
| find(s, regex), findAll(s, regex), groups(s, regex) | регулярки (возвращает первую группу, если есть) |
| numbers(s), firstNumber(s, def), parseMoney("1.5к") | числа из текста |
| formatNumber, compact(15300) = 15.3K, fixed(v, 2), percent(0.5) | форматирование |
| duration(ms), durationTicks(t), time("HH:mm"), date(ms, pattern) | время |
| pad, padLeft, truncate, capitalize, prettyId("minecraft:iron_ingot"), progressBar(f, w, "#", ".") | оформление |
| levenshtein, similarity, closest(value, options) | похожесть строк |
| base64, unbase64, hash, uuid, translit | прочее |

## JSON
| Функция | Описание |
|---|---|
| json.parse(text) | Map / List / String / Number / Boolean |
| json.parseObject(text), json.parseArray(text) | с приведением типа |
| json.stringify(value), json.pretty(value) | в строку (Map, List, Vec и т.д.) |
| json.path(obj, "a.b.0.c") | достать вложенное значение |
| json.valid(text) | проверка |

## Общие данные между скриптами
| Функция | Описание |
|---|---|
| shared.put(key, v), get, getInt, getDouble, getString, getBoolean, has, remove | общее хранилище в памяти |
| shared.increment(key, by), keys(), snapshot() | счётчики и просмотр |
| send(channel, data) | отправить сообщение всем слушателям, возвращает количество получателей |
| listen(channel) { msg -> } | подписка: msg.sender(), msg.data(), string(), number(), map() |
Подписки снимаются автоматически при выгрузке скрипта и не срабатывают, пока скрипт выключен.

## Поиск сущностей
world.query() или query() - конструктор выборки. targets(range) - живые не-друзья рядом, отсортированные по дистанции.
| Фильтр | Описание |
|---|---|
| players(), living(), monsters(), animals(), items(), projectiles() | по типу |
| alive(), notSelf(), notAllies(), allies(), notBots(), notInvisible(), visible() | по состоянию |
| type(id), types(list), named(n), nameContains(s), holding(item) | по данным |
| within(r), within(center, r), beyond(r), inFov(deg), healthBelow(hp), healthAbove(hp) | по положению |
| where { e -> } | свой фильтр |
| closest(), farthest(), lowestHealth(), highestHealth(), closestToCrosshair(), sortBy { }, limit(n) | сортировка |
| list(), first(), count(), any(), none() | результат |
```
val t = query().players().alive().notSelf().notAllies().within(4.0).inFov(90f).lowestHealth().first()
```
Также в world: playerByName, entityByUuid, findBlocks(center, radius, id, список или фильтр), nearestBlock, blocksIn(box), blockBelow(), highestBlockY(x, z), isLoaded(x, z), lightLevel, biome, isThundering, minY, maxY, entityCount, playSoundAt.

### Новые поля Entity
x, y, z, blockX/Y/Z, previousPosition, speed, horizontalSpeed, headYaw, age, invisible, glowing, riding, inLava, swimming, displayName, isItem, isMonster, isAnimal, isProjectile, isArmorStand, isA(type), itemId, itemCount, absorption, totalHealth, healthPercent, armorPoints, hurtTicks, blocking, usingItem, gliding, baby, mainHandItem, offHandItem, held, offHand, effects, hasEffect, pingMs, inTab, isBot, distanceToPlayer, visible, yawFromPlayer, inFov(deg), lookingAtPlayer(deg), vehicle, passengers.

### Новые поля Block
center, name, solid, unbreakable, isAny(list), distanceTo, distanceToPlayer, offset, up, down, north, south, east, west, neighbours, property("facing"), properties.
## Контейнеры
Объект container работает с открытым меню (сундук, инвентарь и т.д.).
| Функция | Описание |
|---|---|
| open(), id(), title(), size(), containerSize() | состояние |
| slot(i), items(), containerItems(), carried() | ItemInfo: id, name, count, maxCount, damage, durability, durabilityPercent, enchanted, isA, nameContains |
| find(id), find { item -> }, findByName(part), findAll(id), count(id), firstEmpty() | поиск |
| click, rightClick, shiftClick, doubleClick, drop, dropAll, dropCarried | клики по слотам |
| swapWithHotbar(slot, hotbar), swapWithOffhand(slot), move(from, to) | перемещение |
| quickMoveAll(id), lootAll(), close() | массовые действия |

## Цвета (дополнительно)
Colors.hex("#ff8800"), toHex, rainbow(periodMs, offset), pulse(c, periodMs, min), wave(a, b, periodMs, offset), gradient(list, t), health(fraction), brighter, darker, invert, grayscale, hue, saturation, brightness, shiftHue, random(), fromName(name). Новые константы: GOLD, LIME, AQUA, MAGENTA, DARK_RED, DARK_GREEN, DARK_BLUE, TRANSPARENT.

## Модули из скриптов
module(...) создаёт настоящий модуль клиента. Он появляется в ClickGUI в своей категории, у него свой бинд (Toggle или Hold), настройки и сохранение в конфиг - как у встроенных модулей.
Модуль работает независимо от включателя скрипта во вкладке Скрипты: его включают в ClickGUI. При перезагрузке файла модуль пересоздаётся, а включённость, бинд и настройки сохраняются.
```
module("AutoSoup", ModuleCategory.COMBAT) {
    description("Ест суп при низком здоровье")
    val hp by slider("Здоровье", 8f, 1f, 20f, 0.5f)
    val notifyOn by checkBox("Уведомлять", true)
    onEnable { if (notifyOn) notify("AutoSoup включён") }
    onTick {
        if (player.health() < hp && inventory.select("mushroom_stew")) interaction.useItem("MAIN_HAND")
    }
    on<AttackEvent> { e -> log.debug("удар по " + e.target.name.string) }
}
```
| Внутри module { } | Описание |
|---|---|
| description("..."), key(Key.R), holdMode(true), suffix("x") | описание, бинд по умолчанию, режим удержания |
| checkBox, slider, rangeSlider, input, selectable, combo, colorPicker, hotkey, button | настройки модуля (как у скрипта) |
| onEnable { }, onDisable { }, onTick { } | жизненный цикл. onTick - каждый тик, пока модуль включён и есть игрок |
| on<Событие> { }, onRender2D { }, onRender3D { } | события - только пока модуль включён |
| active(), active(true), enable(), disable(), toggle(), ticksEnabled() | состояние модуля |
Категории: ModuleCategory.COMBAT, MOVEMENT, RENDER, PLAYER, MISC или строкой: module("Имя", "combat") { }.
Имя модуля должно быть уникальным: встроенный модуль с тем же именем вызовет ошибку загрузки.
Возвращаемый объект можно сохранить: val m = module(...) { } и потом m.active().

## HUD-элементы
hud(...) добавляет элемент в HUD клиента. В чате его можно перетаскивать мышью, позиция сохраняется в конфиг.
```
hud("Координаты", 10f, 80f, 100f, 14f) {
    background()
    render().text(5f, 4f, 7f, "" + player.blockX() + " " + player.blockY() + " " + player.blockZ(), Colors.WHITE)
}
```
| Внутри hud { } | Описание |
|---|---|
| render() | Draw2D - те же функции, что e.render() в Render2DEvent. Координаты считаются от угла элемента |
| width(), height(), size(w, h) | размер области (по нему работает перетаскивание) |
| x(), y() | позиция элемента на экране |
| background(), background(радиус) | фон в стиле HUD клиента |
| alpha(), editing(), hovered(), mouseX(), mouseY() | прозрачность, открыт ли чат (режим редактирования), мышь над элементом |
Объект элемента: h.visible(false), h.position(x, y), h.size(w, h), h.background(true). Пока скрипт выключен, элемент виден только в режиме редактирования как рамка.

## Окна
screen(...) описывает своё окно с элементами. Элементы раскладываются сверху вниз, длинное окно прокручивается колёсиком.
```
val menu = screen("Мои настройки") {
    width(220f)
    label { "HP: " + player.health() }
    val name = textInput("Ник")
    button("Добавить в друзья") { friends.add(name.value()) }
    toggle("Звук", true) { on -> log.info("звук: $on") }
    slider("Громкость", 0.5f, 0f, 1f) { v -> log.info(v) }
    selector("Режим", "Легит", "Рейдж") { m -> log.info(m) }
    canvas(40f) { c -> c.render().gradient(c.x(), c.y(), c.width(), c.height(), Colors.RED, Colors.BLUE, Colors.BLUE, Colors.RED) }
    button("Закрыть") { close() }
}
bind(Key.M) { menu.toggle() }
```
| Элемент | Описание |
|---|---|
| label("текст"), label { динамический текст } | надпись. .color(c), .size(s), .centered(true) |
| button("текст") { } | кнопка |
| toggle("текст", значение) { v -> } (или checkBox) | переключатель. .value() |
| slider("текст", значение, min, max, шаг) { v -> } | слайдер Float или Int: slider("Кол-во", 3, 0, 10) { n -> } |
| textInput("подсказка", значение) { текст -> } (или input) | поле ввода. .value(), .onSubmit { }, .maxLength(n), .focus() |
| selector("текст", "A", "B") { v -> } (или selectable) | выбор из списка, ЛКМ - вперёд, ПКМ - назад |
| separator() | разделитель |
| canvas(высота) { c -> } | своя отрисовка: c.render(), c.x(), c.y(), c.width(), c.height(), c.mouseX(), c.hovered(). .onClick { xy -> } |
| width(240f), pause(false), dim(true), closeOnOutsideClick(true) | вид окна |
| onOpen { }, onClose { }, onKey { key -> } | события окна |
| open(), close(), toggle(), isOpen(), clear(), remove(элемент) | управление |
Каждый элемент можно скрыть: el.visible(false). Окно закрывается само при выгрузке скрипта.

## Корутины
launch { } запускает код, который может ждать, не блокируя игру. Ожидание идёт по тикам клиента, код выполняется в игровом потоке.
```
bind(Key.H) {
    launch("heal") {
        if (!inventory.select("golden_apple")) return@launch
        control.useKey(true)
        val ate = waitUntil(40) { !player.usingItem() }
        control.useKey(false)
        waitTicks(5)
        chat.print(if (ate) "съел" else "не получилось")
    }
}
```
| Функция | Описание |
|---|---|
| launch(имя) { } | запустить, вернёт Job |
| waitTicks(n), nextTick() | подождать тики |
| waitMillis(ms), waitSeconds(s) | подождать по времени |
| waitUntil(таймаут_тиков) { условие } | ждать условия. Вернёт false, если вышел таймаут (-1 - без таймаута) |
| waitWhile(таймаут) { условие } | ждать, пока условие true |
| fetch(url), fetchPost(url, json) | HTTP-запрос с ожиданием ответа |
| turnTo(точка, градусов_за_тик) | плавно повернуть голову |
| walkTo(x, y, z, таймаут) | дойти через навигацию, вернёт true если дошёл |
| job.cancel(), active(), done(), cancelled(), failed(), error(), whenDone { } | управление Job |
| jobs(), cancelJobs() | все активные корутины скрипта |
Пока скрипт выключен, ожидание стоит на паузе. Ошибка внутри launch пишется в консоль с номером строки. Внутри launch выходи через return@launch.

## HTTP
Запросы идут в отдельном потоке, ответ приходит в игровой поток. Только http и https, ответ до 4 МБ, таймаут до 60 секунд, не больше 6 запросов одновременно на скрипт.
| Функция | Описание |
|---|---|
| httpGet(url) { r -> } | GET |
| httpGet(url, mapOf("Authorization" to "...")) { r -> } | GET с заголовками |
| httpPost(url, mapOf("a" to 1)) { r -> } | POST с JSON |
| httpPostText(url, текст, тип) { r -> } | POST с текстом |
| http.postForm(url, map) { }, http.request(метод, url, тело, тип, заголовки, таймаут) { } | формы и любые запросы |
| http.header(имя, значение), http.encode(текст), http.pending() | общие заголовки, кодирование для url |
| r.ok(), status(), body(), lines(), json(), jsonObject(), jsonArray(), header(имя), error(), millis() | ответ |
```
httpGet("https://api.ipify.org?format=json") { r ->
    if (r.ok()) chat.print("IP: " + r.jsonObject()["ip"]) else log.warn(r.error())
}
```

## Интеграция с клиентом
### Друзья
| Функция | Описание |
|---|---|
| friends.isFriend("Nick"), isFriend(entity), isLocal(ник) | друг или нет |
| friends.add(ник), remove(ник), toggle(ник) | изменить список |
| friends.list(), count(), online() | список, количество, друзья рядом (Entity) |
| friends.noFriendDamage() | включена ли защита от урона по друзьям |

### Точки (waypoints)
| Функция | Описание |
|---|---|
| waypoints.all(), here() | все точки и точки текущего сервера и измерения |
| waypoints.add(имя, x, y, z), addHere(имя) | добавить (видна в мире и в .way) |
| waypoints.get(имя), exists(имя), remove(имя), clear(), nearest() | поиск и удаление |
| wp.name(), x(), y(), z(), position(), dimension(), server(), distanceToPlayer(), here() | данные точки |

### Навигация
nav использует встроенный Baritone клиента.
| Функция | Описание |
|---|---|
| nav.goTo(x, y, z), goTo(vec), goTo(block), goToXZ(x, z) | идти к точке |
| nav.goNear(x, y, z, радиус), follow(entity, радиус) | подойти поближе |
| nav.elytraTo(x, y, z), elytraToXZ(x, z) | лететь на элитрах |
| nav.mine(x, y, z), mine(block) | дойти и сломать блок |
| nav.stop(), active(), task(), status(), path() | управление и путь (List<Vec>) |

### Управление движением
| Функция | Описание |
|---|---|
| lookAtSmooth(точка или entity, градусов_за_тик) { готово } | плавный поворот головы |
| control.lookAt(entity), stepTowards(yaw, pitch, шаг) | мгновенный и пошаговый поворот |
| control.move(вперёд, вбок), walkTowards(точка) | зажать клавиши движения в сторону точки |
| control.releaseMovement(), releaseAll(), stopAll() | отпустить клавиши, stopAll ещё останавливает навигацию и использование предмета |
| interaction.placeAt(x, y, z, "MAIN_HAND") | поставить блок в точку, опору ищет сам |
| interaction.startBreaking(x, y, z, side), stopBreaking(), breaking() | ломание блоков |

### Мир - дополнительно
| Функция | Описание |
|---|---|
| world.blockEntities(радиус), blockEntities(радиус, "chest") | сундуки, печки, спавнеры рядом: type(), x(), y(), z(), center(), block(), distanceToPlayer() |
| world.blockEntityAt(x, y, z) | тип блок-сущности или "" |
| world.dayTime(), moonPhase(), weather() | время суток 0..24000, фаза луны, CLEAR / RAIN / THUNDER |
| world.skyLight(x, y, z), blockLight(x, y, z), lightHere(), biomeHere() | свет и биом |
| entity.armor(), equipment("HEAD"), armorPieces(), armorDurabilityPercent() | броня сущности (ItemInfo) |
| entity.effectLevel("speed"), effectTicks(id) | уровень и длительность эффекта |
| entity.totemPops(), isFriend(), gameMode() | тотемы с захода, друг, режим игры из таба |
| render.worldItem(точка, предмет, размер), entityItems(entity, размер), item(ItemInfo, x, y, size) | предметы в 2D-рендере |

## Между скриптами и перезагрузка
| Функция | Описание |
|---|---|
| provide("имя") { args -> результат } | объявить функцию для других скриптов |
| call("имя", 1, 2) | вызвать функцию другого скрипта, ошибка если её нет |
| shared.provides(имя), callOr(имя, args, запасное), functions(), providerOf(имя) | проверка и список |
| retained("ключ") { начальное } | значение в памяти, которое переживает перезагрузку файла (но не перезапуск игры) |
| retain("ключ", значение), forget("ключ") | записать или удалить такое значение |
| scriptLoaded(id), scriptEnabled(id) | состояние других скриптов |
В retained храни простые значения: числа, строки, списки, Map. Свои классы скрипта после перезагрузки уже не совпадут.
```
// lib.kts
provide("distance") { args -> player.distanceTo(vec(args[0] as Int, args[1] as Int, args[2] as Int)) }
// другой скрипт
dependsOn("lib")
val d = call("distance", 0, 64, 0)
```

## Графики
Графики рисуются теми же примитивами, что и HUD клиента. Данные копятся в Series - кольцевом буфере на N последних значений.
```
val tps = series("TPS", capacity = 120, color = Colors.GREEN)
tps.sampleEvery(10) { game.server().tps() }
hudChart("TPS", tps) { area(tps); yRange(0.0, 20.0) }
```
### Series - ряд данных
| Функция | Описание |
|---|---|
| series("Имя", capacity = 120, color = null) | новый ряд. Цвет по умолчанию берётся из палитры |
| s.add(значение) | добавить значение (Int, Float, Double) |
| s.sampleEvery(тиков) { значение } | собирать значение автоматически. null - пропустить. Вернёт Task |
| s.last(), previous(), delta(), min(), max(), avg(), avg(n), sum() | статистика |
| s.size(), capacity(), empty(), get(i), values(), clear() | содержимое, get(0) - самое старое |
| s.color(c), unit(" мс"), decimals(1), visible(false) | вид: цвет, единицы и знаки после запятой в подписях |

### Chart - график
chart("Заголовок") { ... } создаёт график. Внутри:
| Настройка | Описание |
|---|---|
| line(s), area(s), bar(s) | добавить ряд как линию, линию с заливкой или столбцы. Рядов может быть несколько |
| yRange(min, max), yMin(v), yMax(v), autoRange() | фиксированная или автоматическая шкала Y |
| grid, gridLines | сетка и количество линий |
| legend, axisLabels, xLabel | легенда (имя и последнее значение), подписи осей, подпись снизу |
| background, tooltip, smooth | фон, подсказка со значениями под мышью, плавная анимация новых точек и шкалы |
| thickness, textSize, includeZero | толщина линий, размер текста, всегда показывать 0 |
Рисование: chart.draw(render, x, y, w, h) в Render2DEvent, chart.draw(this) внутри hud { }, chart.draw(c) внутри canvas { c -> }, или chart(график, высота) прямо в screen { }.
Подсказка под мышью видна в окнах screen и в HUD, когда открыт чат.
| Функция | Описание |
|---|---|
| hudChart("Имя", ряд1, ряд2, x =, y =, width =, height =) { настройки } | готовый перетаскиваемый HUD-элемент с графиком |
| hudChart("Имя", chart) | HUD из готового графика |
| charts.sparkline(render, ряд, x, y, w, h) | маленькая линия без осей |
| charts.bars(render, listOf(1, 5, 3), x, y, w, h, цвет) | простые столбцы из списка |
| charts.gauge(render, cx, cy, радиус, значение, min, max, цвет) | дуговой индикатор |
| charts.nextColor(), ScriptCharts.PALETTE | цвета палитры |

## Скриншоты
Скриншот снимается с кадра игры в игровом потоке, PNG и JPG кодируются в фоне. Результат приходит в колбэк или в корутину.
```
val hookUrl by input("Discord webhook", "")
screenshot {
    hideHud = true
    scale = 0.5
    onReady { shot -> shot.save("last.png") }
}
launch {
    val shot = capture(hideHud = false, scale = 0.75)
    discord.webhook(hookUrl).sendImage(shot, "мой экран")
}
```
| Функция | Описание |
|---|---|
| screenshot { hideHud; scale; jpegQuality; onReady { shot -> }; onError { текст -> } } | снять скриншот |
| screenshot(hideHud, scale) { shot -> }, takeScreenshot { shot -> } | короткие формы |
| capture(hideHud, scale), capture { ... } | то же внутри launch { }, вернёт Screenshot |
| shot.width(), height(), pixel(x, y), takenAt() | размер и цвет пикселя |
| shot.bytes("png"), bytes("jpg"), sizeBytes(формат) | файл в памяти |
| shot.save("имя.png") | сохранить в assets/<скрипт>/, вернёт картинку для render.image(...) |
| shot.saveAs("deaths/1.jpg") | сохранить, вернёт путь |
hideHud прячет интерфейс игры и HUD клиента на пару кадров. scale от 0.05 до 1. Одновременно в очереди не больше 4 скриншотов.

## Discord
discord.webhook(адрес) - вебхук канала (Настройки канала - Интеграции - Вебхуки). Отправка идёт в фоне через очередь: сообщения уходят по одному, на ответ 429 очередь сама ждёт retry_after и повторяет до 3 раз. В очереди до 50 сообщений, файл до 24 МБ, до 10 файлов.
Адрес вебхука - это пароль от канала. Не пиши его в коде: заведи настройку input("Discord webhook", "") или храни в storage. Клиент нигде не пишет адрес в консоль.
```
val hookUrl by input("Discord webhook", "")
bind(Key.F8) {
    val hook = discord.webhook(hookUrl)
    hook.send {
        username = "Brokke"
        content = "Отчёт"
        embed {
            title = "Статус"
            color = Colors.GREEN
            field("HP", player.health(), true)
            field("Сервер", game.serverAddress(), true)
            footer = "brokke.cc"
            timestamp()
        }
    }
    takeScreenshot { shot -> hook.sendImage(shot, "экран") }
}
```
| Функция | Описание |
|---|---|
| hook.send("текст"), send("текст") { r -> } | простое сообщение |
| hook.send { content; username; avatarUrl; tts; embed { }; image(shot, "имя.png"); file(имя, байты); text(имя, строка); onResult { r -> } } | полное сообщение |
| embed { title; description; url; color; author(имя, иконка); field(имя, значение, inline); footer; footer(текст, иконка); thumbnail; image; imageAttachment("имя.png"); timestamp() } | карточка |
| hook.sendImage(shot, "подпись") { r -> } | скриншот |
| hook.sendFile(имя, байты, подпись), sendText(имя_файла, текст, подпись) | файлы |
| hook.username(имя), avatar(url) | значения по умолчанию |
| hook.pending(), sent(), failed(), rateLimitedMillis(), clearQueue() | очередь |
Упоминания @everyone и ролей в сообщениях вебхука отключены.

## Telegram
telegram.bot(токен) - бот от @BotFather. Опрос (long polling) идёт в отдельном фоновом потоке, все обработчики вызываются в игровом потоке и только пока скрипт включён. Поток останавливается при выгрузке скрипта.
Токен - это полный доступ к боту. Храни его в настройке input(...) или storage, а не в коде, и обязательно ограничь бота своим чатом: allowedChats(id). Если список пуст, бот отвечает всем, и в консоли будет предупреждение. Свой chat id можно узнать у @userinfobot.
```
val token by input("Токен бота", "")
val myChat by input("Мой chat id", "")
val bot = telegram.bot(token).allowedChats(myChat.toLong())
bot.onCommand("/hp") { m -> m.reply("HP: " + player.health()) }
bot.onCommand("/screen") { m -> takeScreenshot { shot -> m.replyPhoto(shot, "экран") } }
bot.onMessage { m -> log.info(m.from + ": " + m.text) }
```
| Функция | Описание |
|---|---|
| bot.allowedChats(id1, id2), allowChat(id), disallowChat(id), isAllowed(id) | белый список чатов |
| bot.onCommand("/cmd") { m -> } | команда. /cmd@имябота тоже работает |
| bot.onMessage { m -> } | все сообщения, кроме обработанных командами |
| bot.onCallback("data") { cb -> }, onCallback { cb -> } | нажатия inline-кнопок |
| bot.send(chatId, текст), send(chatId, текст, "HTML"), send(chatId, текст, клавиатура) | отправка |
| bot.sendPhoto(chatId, shot, подпись), sendDocument(chatId, shot или имя и байты, подпись), sendText(chatId, имя, текст, подпись) | файлы |
| bot.sendToAll(текст) | всем чатам из белого списка |
| bot.edit(chatId, messageId, текст, клавиатура) | изменить сообщение |
| bot.onError { текст -> }, skipOldUpdates(true), stop(), polling(), username(), received(), pending() | служебное |
| m.text, chatId, from, fromId, messageId, chatTitle, chatType, isCommand, command(), args(), rest() | сообщение |
| m.reply(текст), reply(текст, "Markdown"), reply(текст, клавиатура), replyPhoto(shot, подпись) | ответ |
| cb.data, chatId, from, messageId, answer(текст), answer(текст, true), reply(текст), edit(текст) | нажатие кнопки |
| keyboard { row("Текст", "data", "Текст2", "data2"); button(текст, data); link(текст, url); newRow() } | inline-клавиатура |
По умолчанию бот пропускает сообщения, пришедшие пока скрипт был выключен (skipOldUpdates). На 429 бот ждёт retry_after.
Опасные команды (выход с сервера, отправка в чат) делай только при заполненном allowedChats.

## Профилирование
| Функция | Описание |
|---|---|
| profile("метка") { код } | замерить блок и вернуть его результат |
| log.start("метка"), log.stop("метка") | замер вручную, stop вернёт мс |
| log.measure("метка") { } | замер без результата |
| log.timings(), printTimings(), resetTimings() | статистика: количество, среднее, последнее, максимум |

## Примеры
### Цель на HUD
```
name("Target HUD")
val range by slider("Дистанция", 8f, 2f, 32f, 0.5f)
val color by colorPicker("Цвет", 0xFF55FFFF)
var target: Entity? = null
on<ClientTickEvent> {
    target = if (inGame) world.nearestEntity(player.position(), range.toDouble(), filters.enemyPlayers()) else null
}
on<Render2DEvent> { e ->
    val t = target ?: return@on
    val r = e.render()
    val x = e.width() / 2f + 20f
    val y = e.height() / 2f
    r.roundedRect(x, y, 90f, 26f, 5f, Colors.rgba(10, 10, 15, 170))
    r.text(x + 6f, y + 8f, 7f, t.name(), Colors.WHITE)
    r.rect(x + 6f, y + 17f, 78f * (t.health() / t.maxHealth()), 3f, color)
}
on<Render3DEvent> { e -> target?.let { e.render().entityBox(it, color, true) } }
```

### Авто-ответ в чате
```
name("Auto reply")
val word by input("Слово", "привет")
val answer by input("Ответ", "привет!")
val cd = timer()
on<ChatReceiveEvent> { e ->
    if (e.message.contains(word, ignoreCase = true) && cd.passedMillisAndReset(3000)) {
        chat.sendToServer(answer)
    }
}
```

### Счётчик убийств
```
name("Kills")
var kills = storage.getInt("kills", 0)
on<EntityRemoveEvent> { e ->
    val ent = e.entity()
    if (ent.isPlayer() && !ent.alive() && player.distanceTo(ent) < 6.0) {
        kills++
        storage.put("kills", kills)
    }
}
on<Render2DEvent> { e -> e.render().text(10f, 40f, 7f, "Убийств: $kills", Colors.YELLOW) }
command("kills") { runs { reply("Убийств: $kills") } }
```

### Модуль с полётом
```
name("FastFly")
module("FastFly", ModuleCategory.MOVEMENT) {
    val speed by slider("Скорость", 1.5f, 0.1f, 5f, 0.1f)
    onDisable { control.velocity(0.0, 0.0, 0.0) }
    onTick {
        val yaw = Math.toRadians(player.yaw().toDouble())
        val fwd = if (keys.isDown(Key.W)) 1.0 else 0.0
        val y = if (keys.isDown(Key.SPACE)) 0.5 else if (keys.isDown(Key.LEFT_SHIFT)) -0.5 else 0.0
        control.velocity(-Math.sin(yaw) * fwd * speed, y, Math.cos(yaw) * fwd * speed)
    }
}
```

### Авто-еда на корутине
```
name("Auto Eat")
var busy = false
everyTicks(10) {
    if (busy || player.food() >= 14) return@everyTicks
    val slot = inventory.find { it.isFood() }
    if (slot !in 0..8) return@everyTicks
    busy = true
    launch {
        val prev = inventory.selectedSlot()
        inventory.selectSlot(slot)
        waitTicks(2)
        control.useKey(true)
        waitUntil(60) { player.food() >= 20 }
        control.useKey(false)
        inventory.selectSlot(prev)
        busy = false
    }
}
```

### HUD-графики TPS и пинга
```
// ServerGraph.kts - HUD-графики TPS и пинга (двигаются в чате), подробное окно по Ctrl+G
name("Server Graph")

val tps = series("TPS", capacity = 120, color = Colors.GREEN).decimals(1)
val ping = series("Пинг", capacity = 120, color = 0xFF4FC3F7).unit(" мс")
tps.sampleEvery(10) { if (inGame) game.server().tps() else null }
ping.sampleEvery(10) { if (inGame) game.server().pingMs() else null }

hudChart("TPS", tps, x = 10f, y = 120f, width = 150f, height = 60f) {
    area(tps)
    yRange(0.0, 20.0)
    gridLines = 4
}
hudChart("Пинг", ping, x = 10f, y = 186f, width = 150f, height = 60f)

val big = chart("Сервер") {
    area(tps)
    line(ping)
    xLabel = "последние 60 секунд"
}

val window = screen("Сервер") {
    width(320f)
    label { "TPS сейчас " + strings.fixed(tps.last(), 1) + ", средний " + strings.fixed(tps.avg(), 1) + ", минимум " + strings.fixed(tps.min(), 1) }
    chart(big, 140f)
    button("Очистить") { tps.clear(); ping.clear() }
}
bind("ctrl+G") { window.toggle() }
```

### Скриншот смерти в Discord
```
// DeathShot.kts - при смерти делает скриншот и отправляет его в Discord
name("Death screenshot")
description("Скриншот смерти в Discord-вебхук")

// адрес вебхука вводится в настройках скрипта, а не хранится в коде
val hookUrl by input("Discord webhook", "")
val withHud by checkBox("С интерфейсом", true)

onDeath { e ->
    if (hookUrl.isBlank()) {
        notify("Death screenshot", "укажи вебхук в настройках", NotifyKind.WARN)
        return@onDeath
    }
    val pos = e.position()
    launch("death-shot") {
        waitTicks(5)
        val shot = capture(hideHud = !withHud, scale = 0.75)
        shot.save("deaths/" + strings.time("yyyy-MM-dd_HH-mm-ss") + ".png")
        discord.webhook(hookUrl).send {
            username = "Brokke"
            content = "**" + player.name() + "** умер"
            image(shot, "death.png")
            embed {
                title = e.message().ifBlank { "Смерть" }
                description = "Сервер: " + game.serverAddress().ifBlank { "одиночная игра" }
                color = Colors.RED
                field("Координаты", "${pos.x.toInt()} ${pos.y.toInt()} ${pos.z.toInt()}", true)
                field("Измерение", world.dimension(), true)
                imageAttachment("death.png")
                timestamp()
            }
            onResult { r -> if (!r.ok()) log.warn("Discord: " + r.status() + " " + r.error()) }
        }
    }
}
```

### Удалённое управление из Telegram
```
// TelegramRemote.kts - управление клиентом из Telegram: /screen, /status, /hp, /disconnect, /chat
name("Telegram remote")
description("Бот: скриншот, статус, здоровье, выход с сервера, сообщение в чат")

// токен от @BotFather и id своего чата вводятся в настройках скрипта
val token by input("Токен бота", "")
val myChat by input("Мой chat id", "")

fun status(): String =
    if (!inGame) "не в игре"
    else "Сервер: " + game.serverAddress().ifBlank { "одиночная" } +
        "
TPS: " + strings.fixed(game.server().tps(), 1) + ", пинг " + game.server().pingMs() + " мс" +
        "
HP: " + player.health().toInt() + "/" + player.maxHealth().toInt() +
        "
Позиция: " + player.blockX() + " " + player.blockY() + " " + player.blockZ()

if (token.isBlank() || myChat.toLongOrNull() == null) {
    log.warn("укажи токен бота и свой chat id в настройках и перезагрузи скрипт")
} else {
    val bot = telegram.bot(token).allowedChats(myChat.toLong())
    val menu = keyboard {
        row("Скриншот", "screen", "Статус", "status")
        row("Здоровье", "hp")
    }

    bot.onCommand("/start") { m -> m.reply("Привет! Команды: /screen /status /hp /disconnect /chat <текст>", menu) }
    bot.onCommand("/status") { m -> m.reply(status()) }
    bot.onCommand("/hp") { m -> m.reply(if (inGame) "HP: " + player.health() + ", еда: " + player.food() else "не в игре") }
    bot.onCommand("/screen") { m ->
        screenshot {
            scale = 0.6
            onReady { shot -> m.replyPhoto(shot, status()) }
            onError { err -> m.reply("не получилось: $err") }
        }
    }
    bot.onCommand("/disconnect") { m ->
        if (!inGame) m.reply("и так не в игре") else {
            m.reply("отключаюсь")
            afterTicks(10) { client.disconnect() }
        }
    }
    bot.onCommand("/chat") { m ->
        val text = m.rest()
        if (text.isBlank()) m.reply("напиши: /chat текст") else if (!inGame) m.reply("не в игре") else {
            chat.sendToServer(text)
            m.reply("отправлено")
        }
    }
    bot.onCallback("screen") { cb -> takeScreenshot { shot -> bot.sendPhoto(cb.chatId, shot, "") }; cb.answer("делаю скриншот") }
    bot.onCallback("status") { cb -> cb.reply(status()) }
    bot.onCallback("hp") { cb -> cb.answer("HP: " + player.health().toInt(), true) }
    bot.onMessage { m -> if (!m.isCommand) m.reply("не понял. /start - список команд") }

    onDeath { e -> bot.send(myChat.toLong(), "Ты умер: " + e.message()) }
}
```

## MCP - ИИ пишет скрипты
Как пользоваться (3 шага):
1: Установи ИИ-программу - например Codex, Claude Code или Antigravity.
2: Во вкладке Скрипты нажми MCP (в шапке консоли), затем Настроить у нужной программы.
3: Перезапусти программу и напиши ей: сделай скрипт для brokke, который ... Скрипт сам появится в списке.
Отключить можно там же: кнопка Отключить у программы или Отключить от всех программ.
| Инструмент | Что делает |
|---|---|
| read_docs | эта документация |
| api_reference | все функции API с типами |
| example_scripts | готовые примеры |
| scripts_list / script_source | список и код скриптов |
| script_deploy | сохранить, скомпилировать, вернуть ошибки |
| script_enable / script_disable | включить или выключить |
| script_settings / script_setting_set | читать и менять настройки |
| console_tail | логи и ошибки выполнения |
| asset_write / asset_read | создать или прочитать файл в assets/<скрипт>/ |
| asset_list / asset_delete | список файлов ассетов, удалить файл |
| modules_list | все модули клиента, включая созданные скриптами |
| game_state | игрок, сервер, измерение, здоровье |

## Ошибки
Ошибки компиляции видны в консоли вкладки Скрипты и во вкладке Настройки скрипта - с именем файла и номером строки.
Частые ошибки:
Unresolved reference: опечатка в имени функции - сверься с разделами этой документации.
Type mismatch: Float и Int - пиши 1f для Float и 1.0 для Double.
Return not allowed: внутри on { } выходи через return@on, внутри hotkey - return@hotkey.
... is not allowed in scripts: использован запрещённый пакет, убери import.
Скрипт выключился сам: обработчик работал дольше 250 мс или упал 5 раз подряд - смотри консоль.
Ошибки выполнения показывают строку и цепочку вызовов: Test.kts:12: NullPointerException (вызвано из строк 30, 41).
модуль 'Имя' уже существует: имя module(...) совпадает со встроенным модулем или модулем другого скрипта.
... may only be called at the top level: module, author, version, dependsOn и настройки объявляются только при загрузке.
Долгая работа: не жди в цикле while - используй launch { waitUntil { } } или clickQueue.
