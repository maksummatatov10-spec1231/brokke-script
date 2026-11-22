// Brokke Scripts API 4. Runtime testing in the installed client is required.
name("Script TriggerBot")
description("Настраиваемые удары по сущности под прицелом")
author("brokke-script")
version("0.1.0")
requireApi(4)

// Unique name: does not replace the built-in TriggerBot.
module("ScriptTriggerBot", ModuleCategory.COMBAT) {
    description("Удары под прицелом без автоповорота. Настройки — ПКМ по модулю.")

    val targets by combo("Цели", "Игроки", "Монстры", "Животные", "Другие живые", "Неживые",
        selected = listOf("Игроки", "Монстры", "Животные", "Другие живые"))
    val typeMode by selectable("Фильтр ID", "Выключен", "Белый список", "Чёрный список", selected = "Выключен")
    val typeIds by input("ID через запятую", "minecraft:zombie,minecraft:skeleton")
    val hitFriends by checkBox("Бить друзей", false)
    val hitInvisible by checkBox("Бить невидимых", false)
    val hitBots by checkBox("Бить ботов", false)
    val hitArmorStands by checkBox("Бить стойки для брони", false)

    val range by slider("Дальность", 3f, 0.5f, 6f, 0.1f)
    val respectReach by checkBox("Ограничить дальностью игрока", true)
    val throughBlocks by checkBox("Сквозь блоки", false)
    val criticalMode by selectable("Криты", "Обычные", "Только криты", "Умные криты", selected = "Умные криты")
    val onlySpace by checkBox("Только с пробелом", false)
    val cooldown by slider("Заряд удара", 0.95f, 0.1f, 1f, 0.01f)
    val intervalMs by slider("Мин. интервал мс", 100, 0, 2000, 10)
    val reactionMs by slider("Удержание прицела мс", 0, 0, 1000, 10)
    val pauseUsing by checkBox("Пауза при использовании предмета", true)
    val onlyWeapon by checkBox("Только меч или топор", false)
    val requireMouse by checkBox("Только при зажатой ЛКМ", false)

    var trackedUuid = ""
    var trackedSince = 0L
    var lastAttack = -1L
    var cachedIdText = ""
    var cachedIds = emptySet<String>()

    onEnable {
        trackedUuid = ""
        trackedSince = 0L
        lastAttack = -1L
    }
    onDisable {
        trackedUuid = ""
        // No keys, rotations, sprint state or inventory are modified.
    }

    onTick {
        if (!inGame) {
            trackedUuid = ""
            return@onTick
        }
        if (!player.alive() || player.gameMode() == "spectator" ||
            game.screenOpen() || !client.focused() || keys.inputBlocked()) {
            trackedUuid = ""
            return@onTick
        }
        if ((pauseUsing && player.usingItem()) ||
            (onlySpace && !keys.isDown(Key.SPACE)) ||
            (requireMouse && !keys.mouseDown(0))) {
            trackedUuid = ""
            return@onTick
        }
        if (onlyWeapon) {
            val held = player.held()
            if (!held.endsWith("_sword") && !held.endsWith("_axe")) {
                trackedUuid = ""
                return@onTick
            }
        }

        if (typeIds != cachedIdText) {
            cachedIdText = typeIds
            cachedIds = typeIds.split(",").map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .map { if (it.contains(":")) it else "minecraft:$it" }.toSet()
        }
        val reach = if (respectReach) minOf(range.toDouble(), player.entityReachBlocks()) else range.toDouble()
        // Trace all entities first: never select an allowed entity behind an excluded one.
        val hit = raycast.crosshair(reach, !throughBlocks, filters.any())
        if (hit !is Hit.OnEntity || hit.distance() > reach) {
            trackedUuid = ""
            return@onTick
        }
        val target = hit.entity()
        val category = when {
            target.isPlayer() -> "Игроки"
            target.isMonster() -> "Монстры"
            target.isAnimal() -> "Животные"
            target.isLiving() -> "Другие живые"
            else -> "Неживые"
        }
        val type = target.type().lowercase()
        val excluded = target.isSelf() || !targets.contains(category) ||
            (target.isLiving() && !target.alive()) ||
            (!hitFriends && target.isAlly()) ||
            (!hitInvisible && target.invisible()) ||
            (!hitBots && target.isBot()) ||
            (!hitArmorStands && target.isArmorStand()) ||
            (typeMode == "Белый список" && !cachedIds.contains(type)) ||
            (typeMode == "Чёрный список" && cachedIds.contains(type))
        if (excluded) {
            trackedUuid = ""
            return@onTick
        }

        val now = client.millis()
        if (trackedUuid != target.uuid()) {
            trackedUuid = target.uuid()
            trackedSince = now
        }
        if (now - trackedSince < reactionMs.toLong()) return@onTick
        if (lastAttack >= 0L && now - lastAttack < intervalMs.toLong()) return@onTick
        if (player.attackCooldown() < cooldown) return@onTick

        // Conservative vanilla-like conditions, NOT proof of a server-side critical.
        val criticalReady = !player.onGround() && player.velocity().y < 0.0 &&
            player.fallDistanceBlocks() > 0f && !player.sprinting() &&
            !player.inWater() && !player.inLava() && !player.swimming() &&
            !player.climbing() && !player.riding() && !player.flying() &&
            !player.gliding() && !player.usingRiptide() &&
            !player.hasEffect("minecraft:blindness") &&
            !player.hasEffect("minecraft:slow_falling") &&
            !player.hasEffect("minecraft:levitation") &&
            player.attackCooldown() > 0.9f
        when (criticalMode) {
            "Только криты" -> if (!target.isLiving() || !criticalReady) return@onTick
            // Grounded, no Space: ordinary hit. Jumping/falling: wait for critical conditions.
            "Умные криты" -> if (!(player.onGround() && !keys.isDown(Key.SPACE)) &&
                (!target.isLiving() || !criticalReady)) return@onTick
        }
        interaction.attack(target)
        lastAttack = now
    }
}
