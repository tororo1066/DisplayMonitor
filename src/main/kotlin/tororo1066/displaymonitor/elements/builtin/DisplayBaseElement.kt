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
import tororo1066.displaymonitor.configuration.DisplayParameters
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sEvent.SEvent
import java.util.UUID


abstract class DisplayBaseElement : AbstractElement() {

    @Settable(childOnly = true) var displayParameters = DisplayParameters()
    @Settable var interactionScale = Vector(1.0, 1.0, 1.0)
    @Settable var onSpawn: Execute = Execute.empty()
    @Settable var onInteract: Execute = Execute.empty()
    @Settable var onHover: Execute = Execute.empty()
    @Settable var onUnhover: Execute = Execute.empty()
    @Settable var interactionDistance = 4.0
    @Settable var switchHover = true
    @Settable var visualizeHitbox = false
    @Settable var visibleAll = false
    @Settable var public = false
    @Settable var persistent = true

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
            it.isPersistent = persistent
            (entity as? Player)?.showEntity(SJavaPlugin.plugin, it)

            it.persistentDataContainer.set(NamespacedKey(SJavaPlugin.plugin, "displayentity"), PersistentDataType.STRING, "")
        }

        runExecute(onSpawn)

        val dropInteract = arrayListOf<UUID>()

        sEvent.register<PlayerInteractEvent> { e ->
            if (!public && e.player != entity) return@register
            if (dropInteract.contains(e.player.uniqueId)) return@register
            if (Utils.isPointInsideRotatedRect(
                    e.player,
                    this.entity,
                    interactionScale,
                    interactionDistance,
                )
            ) {
                runExecute(onInteract) {
                    it.target = e.player
                    it.location = e.player.location
                }
            }
        }

        sEvent.register<PlayerDropItemEvent> { e ->
            if (!public && e.player != entity) return@register
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
        tickTask?.cancel()
        sEvent.unregisterAll()
    }

    override fun tick(entity: Entity?) {

        if (!this.entity.isValid) {
            remove()
            return
        }

        val players = if (public) this.entity.location.getNearbyPlayers(interactionScale.x + interactionDistance) else listOfNotNull(entity as? Player)
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
        entity.isPersistent = persistent

        applyEntity(entity)
    }
}