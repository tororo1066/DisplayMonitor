package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.elements.AllowedPlayers
import tororo1066.displaymonitor.elements.DisplayParameters
import tororo1066.displaymonitor.hitbox.IgnoreModify
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sEvent.SEvent
import java.lang.ref.WeakReference
import java.util.UUID

abstract class DisplayBaseElement<T: Display> : AbstractElement() {

    @Settable(childOnly = true) var displayParameters = DisplayParameters()

    @ParameterDoc(
        name = "interactionScale",
        description = "クリックの判定範囲。",
        default = "1,1,1"
    )
    @Settable var interactionScale = Vector(1.0, 1.0, 1.0)

    @ParameterDoc(
        name = "onSpawn",
        description = "スポーン時のアクション。"
    )
    @Settable var onSpawn: Execute? = null

    @ParameterDoc(
        name = "onTick",
        description = "1Tick毎に実行されるアクション。"
    )
    @Settable var onTick: Execute? = null

    @ParameterDoc(
        name = "onInteract",
        description = "クリック時のアクション。"
    )
    @Settable var onInteract: Execute? = null

    @ParameterDoc(
        name = "onHover",
        description = "ホバー時のアクション。"
    )
    @Settable var onHover: Execute? = null

    @ParameterDoc(
        name = "onUnhover",
        description = "ホバー解除時のアクション。"
    )
    @Settable var onUnhover: Execute? = null

    @ParameterDoc(
        name = "interactionDistance",
        description = "クリックの判定距離。",
        default = "4"
    )
    @Settable var interactionDistance = 4.0

    @ParameterDoc(
        name = "switchHover",
        description = "ホバー時のアクションを切り替えた時のみに実行するか。",
        default = "true"
    )
    @Settable var switchHover = true

    @ParameterDoc(
        name = "visualizeHitbox",
        description = "クリックの判定範囲を表示するか。 (デバッグ用)",
        default = "false"
    )
    @Settable var visualizeHitbox = false

    @ParameterDoc(
        name = "visiblePlayers",
        description = "表示するプレイヤーの制限。"
    )
    @Settable var visiblePlayers = AllowedPlayers()

    @ParameterDoc(
        name = "interactablePlayers",
        description = "操作可能なプレイヤーの制限。"
    )
    @Settable var interactablePlayers = AllowedPlayers()

    @ParameterDoc(
        name = "glow",
        description = "エンティティを光らせるか。",
        default = "false"
    )
    @Settable var glow = false

    @ParameterDoc(
        name = "glowColor",
        description = "エンティティの光る色。",
        default = "#FFFFFF"
    )
    @Settable var glowColor: Color = Color.WHITE

    @ParameterDoc(
        name = "ignoreModifies",
        description = "使用しない判定の範囲を変更する要素のリスト。\n" +
                "使える要素\n" +
                "- translation\n" +
                "- right_rotation\n" +
                "- scale\n" +
                "- left_rotation"
    )
    var ignoreModifies = listOf<IgnoreModify>()

    abstract val clazz: Class<out T>

    var entityRef: WeakReference<T>? = null
    val sEvent = SEvent()
    val hoverPlayers = mutableSetOf<UUID>()

    override val syncGroup = true

    abstract fun applyEntity(entity: T)

    private fun getEntityOrNull(): T? {
        val display = entityRef?.get() ?: return null
        return if (display.isValid) display else null
    }

    private fun applyChangesToEntity(entity: T) {
        entity.billboard = displayParameters.billboard
        entity.transformation = displayParameters.getTransformation()
        entity.interpolationDelay = displayParameters.interpolationDelay
        entity.interpolationDuration = displayParameters.interpolationDuration
        entity.teleportDuration = displayParameters.teleportDuration
        entity.shadowRadius = displayParameters.shadowRadius
        entity.shadowStrength = displayParameters.shadowStrength
        entity.brightness = displayParameters.brightness
        entity.isVisibleByDefault = visiblePlayers.allowedPlayers.isEmpty()
        entity.isGlowing = glow
        entity.glowColorOverride = glowColor

        if (visiblePlayers.allowedPlayers.isNotEmpty()) {
            visiblePlayers.allowedPlayersAction { player ->
                player.showEntity(SJavaPlugin.plugin, entity)
            }
        }
        if (visiblePlayers.disallowedPlayers.isNotEmpty()) {
            visiblePlayers.disallowedPlayersAction { player ->
                player.hideEntity(SJavaPlugin.plugin, entity)
            }
        }

        applyEntity(entity)
    }

    override fun spawn(entity: Entity?, location: Location) {
        val spawned = location.world.spawn(location, clazz) {
            applyChangesToEntity(it)
            it.persistentDataContainer.set(
                NamespacedKey(SJavaPlugin.plugin, "displayentity"),
                PersistentDataType.STRING,
                ""
            )
        }
        this.entityRef = WeakReference(spawned)

        runExecute(onSpawn)

        val dropInteract = arrayListOf<UUID>()

        fun checkInteract(player: Player): Boolean {
            if (!interactablePlayers.isAllowed(player)) return false
            if (dropInteract.contains(player.uniqueId)) return false
            val display = getEntityOrNull() ?: return false
            if (Utils.isPointInsideRotatedRect(
                    player,
                    display,
                    interactionScale,
                    interactionDistance,
                    ignoreModifies = ignoreModifies
            )) {
                runExecute(onInteract) {
                    it.target = player
                    it.location = player.location
                }

                return true
            }

            return false
        }

        sEvent.register<PlayerInteractEvent> { e ->
            if (checkInteract(e.player)) {
                e.isCancelled = true
            }
        }

        // エンティティをクリックした時はPlayerInteractEventが発火しないため、PlayerInteractEntityEventを使用
        sEvent.register<PlayerInteractEntityEvent> { e ->
            if (checkInteract(e.player)) {
                e.isCancelled = true
            }
        }

        // プレイヤーがダメージを与えた時もクリックとみなす
        sEvent.register<EntityDamageByEntityEvent> { e ->
            val damager = e.damager
            if (damager !is Player) return@register
            if (checkInteract(damager)) {
                e.isCancelled = true
            }
        }

        sEvent.register<PlayerDropItemEvent> { e ->
            if (!interactablePlayers.isAllowed(e.player)) return@register
            dropInteract.add(e.player.uniqueId)

            Bukkit.getScheduler().runTaskLater(SJavaPlugin.plugin, Runnable {
                dropInteract.remove(e.player.uniqueId)
            }, 1)
        }

        sEvent.register<PlayerJoinEvent> { e ->
            val player = e.player
            val display = getEntityOrNull() ?: return@register
            if (visiblePlayers.isAllowed(player)) {
                player.showEntity(SJavaPlugin.plugin, display)
            } else {
                player.hideEntity(SJavaPlugin.plugin, display)
            }
        }

        startTick(entity)
    }

    override fun attachEntity(entity: Entity) {
        val display = getEntityOrNull() ?: return
        entity.addPassenger(display)
    }

    override fun remove() {
        getEntityOrNull()?.remove()
        entityRef?.clear()
        stopTick()
        sEvent.unregisterAll()
    }

    override fun tick(entity: Entity?) {
        val display = getEntityOrNull()
        if (display == null) {
            remove()
            return
        }

        runExecute(onTick)

        interactablePlayers.allowedPlayersAction { player ->
            val onCursor = Utils.isPointInsideRotatedRect(
                player,
                display,
                interactionScale,
                interactionDistance,
                visualizeHitbox,
                ignoreModifies = ignoreModifies
            )

            if (!switchHover) {
                if (onCursor) {
                    runExecute(onHover) {
                        it.target = player
                        it.location = player.location
                    }
                } else {
                    runExecute(onUnhover) {
                        it.target = player
                        it.location = player.location
                    }
                }
            } else {
                if (onCursor && !hoverPlayers.contains(player.uniqueId)) {
                    hoverPlayers.add(player.uniqueId)
                    runExecute(onHover) {
                        it.target = player
                        it.location = player.location
                    }
                } else if (!onCursor && hoverPlayers.contains(player.uniqueId)) {
                    hoverPlayers.remove(player.uniqueId)
                    runExecute(onUnhover) {
                        it.target = player
                        it.location = player.location
                    }
                }
            }
        }
    }

    override fun move(location: Location) {
        getEntityOrNull()?.teleport(location)
    }

    override fun applyChanges() {
        val display = getEntityOrNull() ?: return
        applyChangesToEntity(display)
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        super.prepare(configuration)
        if (configuration.isSet("ignoreModifies")) {
            ignoreModifies = configuration.getStringList("ignoreModifies").mapNotNull {
                try {
                    IgnoreModify.valueOf(it.uppercase())
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}