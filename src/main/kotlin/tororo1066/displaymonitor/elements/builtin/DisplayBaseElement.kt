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
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.elements.AllowedPlayers
import tororo1066.displaymonitor.elements.DisplayParameters
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sEvent.SEvent
import java.util.UUID

abstract class DisplayBaseElement : AbstractElement() {

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
    @Settable var onSpawn: Execute = Execute.empty()

    @ParameterDoc(
        name = "onTick",
        description = "1Tick毎に実行されるアクション。"
    )
    @Settable var onTick: Execute = Execute.empty()

    @ParameterDoc(
        name = "onInteract",
        description = "クリック時のアクション。"
    )
    @Settable var onInteract: Execute = Execute.empty()

    @ParameterDoc(
        name = "onHover",
        description = "ホバー時のアクション。"
    )
    @Settable var onHover: Execute = Execute.empty()

    @ParameterDoc(
        name = "onUnhover",
        description = "ホバー解除時のアクション。"
    )
    @Settable var onUnhover: Execute = Execute.empty()

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

    abstract val clazz: Class<out Display>

    lateinit var entity: Display
    val sEvent = SEvent()
    var hover = false

    override val syncGroup = true

    abstract fun applyEntity(entity: Display)

    private fun applyChangesToEntity(entity: Display) {
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
        this.entity = location.world.spawn(location, clazz) {
            applyChangesToEntity(it)
            it.persistentDataContainer.set(NamespacedKey(SJavaPlugin.plugin, "displayentity"), PersistentDataType.STRING, "")
        }

        runExecute(onSpawn)

        val dropInteract = arrayListOf<UUID>()

        fun checkInteract(player: Player) {
            if (!interactablePlayers.isAllowed(player)) return
            if (dropInteract.contains(player.uniqueId)) return
            if (Utils.isPointInsideRotatedRect(
                    player,
                    this.entity,
                    interactionScale,
                    interactionDistance,
                )
            ) {
                runExecute(onInteract) {
                    it.target = player
                    it.location = player.location
                }
            }
        }

        sEvent.register<PlayerInteractEvent> { e ->
            checkInteract(e.player)
        }

        // エンティティをクリックした時はPlayerInteractEventが発火しないため、PlayerInteractEntityEventを使用
        sEvent.register<PlayerInteractEntityEvent> { e ->
            checkInteract(e.player)
        }

        // プレイヤーがダメージを与えた時もクリックとみなす
        sEvent.register<EntityDamageByEntityEvent> { e ->
            val damager = e.damager
            if (damager !is Player) return@register
            checkInteract(damager)
        }

        sEvent.register<PlayerDropItemEvent> { e ->
            if (!interactablePlayers.isAllowed(e.player)) return@register
            dropInteract.add(e.player.uniqueId)

            Bukkit.getScheduler().runTaskLater(SJavaPlugin.plugin, Runnable {
                dropInteract.remove(e.player.uniqueId)
            }, 1)
        }

        startTick(entity)
    }

    override fun attachEntity(entity: Entity) {
        entity.addPassenger(this.entity)
    }

    override fun remove() {
        entity.remove()
        stopTick()
        sEvent.unregisterAll()
    }

    override fun tick(entity: Entity?) {

        if (!this.entity.isValid) {
            remove()
            return
        }

        runExecute(onTick)

        val players = arrayListOf<Player>()
        interactablePlayers.allowedPlayersAction { player ->
            players.add(player)
        }
        players.forEach { player ->
            val onCursor = Utils.isPointInsideRotatedRect(
                player,
                this.entity,
                interactionScale,
                interactionDistance,
                visualizeHitbox
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
                if (onCursor && !hover) {
                    runExecute(onHover) {
                        it.target = player
                        it.location = player.location
                    }
                    hover = true
                } else if (!onCursor && hover) {
                    runExecute(onUnhover) {
                        it.target = player
                        it.location = player.location
                    }
                    hover = false
                }
            }
        }
    }

    override fun move(location: Location) {
        entity.teleport(location)
    }

    override fun applyChanges() {
        applyChangesToEntity(entity)
    }
}