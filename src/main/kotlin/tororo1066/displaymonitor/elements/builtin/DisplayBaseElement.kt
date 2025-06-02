package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.elements.DisplayParameters
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.elements.AbstractElement
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
        name = "visibleAll",
        description = "全てのプレイヤーに表示するか。",
        default = "false"
    )
    @Settable var visibleAll = false
    @ParameterDoc(
        name = "public",
        description = "他のプレイヤーが操作できるようにするか。",
        default = "false"
    )
    @Settable var public = false

    abstract val clazz: Class<out Display>

    lateinit var entity: Display
    val sEvent = SEvent()
    var hover = false

    override val syncGroup = true

    abstract fun applyEntity(entity: Display)

    override fun spawn(entity: Entity?, location: Location) {
        this.entity = location.world.spawn(location, clazz) {
            it.billboard = displayParameters.billboard
            it.transformation = displayParameters.getTransformation()
            it.interpolationDelay = displayParameters.interpolationDelay
            it.interpolationDuration = displayParameters.interpolationDuration
            it.teleportDuration = displayParameters.teleportDuration
            it.shadowRadius = displayParameters.shadowRadius
            it.shadowStrength = displayParameters.shadowStrength
            it.brightness = displayParameters.brightness
            applyEntity(it)
            it.isVisibleByDefault = visibleAll
            (entity as? Player)?.showEntity(SJavaPlugin.plugin, it)

            it.persistentDataContainer.set(NamespacedKey(SJavaPlugin.plugin, "displayentity"), PersistentDataType.STRING, "")
        }

        runExecute(onSpawn)

        val dropInteract = arrayListOf<UUID>()

        sEvent.register<PlayerInteractEvent> { e ->
            if (!public && e.player.uniqueId != entity?.uniqueId) return@register
            if (dropInteract.contains(e.player.uniqueId)) return@register
            if (Utils.isPointInsideRotatedRect(
                    e.player,
                    this.entity,
                    interactionScale,
                    interactionDistance,
                )
            ) {
                e.isCancelled = true
                runExecute(onInteract) {
                    it.target = e.player
                    it.location = e.player.location
                }
            }
        }

        sEvent.register<PlayerDropItemEvent> { e ->
            if (!public && e.player.uniqueId != entity?.uniqueId) return@register
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

        val players = if (public) {
            this.entity.location.getNearbyPlayers(interactionScale.x + interactionDistance)
        } else {
            listOfNotNull(entity?.uniqueId?.let { Bukkit.getPlayer(it) })
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
        entity.billboard = displayParameters.billboard
        entity.transformation = displayParameters.getTransformation()
        entity.interpolationDelay = displayParameters.interpolationDelay
        entity.interpolationDuration = displayParameters.interpolationDuration
        entity.teleportDuration = displayParameters.teleportDuration
        entity.isVisibleByDefault = visibleAll

        applyEntity(entity)
    }
}