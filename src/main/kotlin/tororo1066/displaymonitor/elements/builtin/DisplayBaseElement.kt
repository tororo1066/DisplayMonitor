package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.configuration.DisplayParameters
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.elements.AsyncExecute
import tororo1066.displaymonitor.elements.Settable
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sEvent.SEvent


abstract class DisplayBaseElement : AbstractElement() {

    @Settable(childOnly = true) var displayParameters = DisplayParameters()
    @Settable var interactionScale = Vector(1.0, 1.0, 1.0)
    @Settable var onSpawn: AsyncExecute = AsyncExecute.empty()
    @Settable var onInteract: AsyncExecute = AsyncExecute.empty()
    @Settable var onHover: AsyncExecute = AsyncExecute.empty()
    @Settable var onUnhover: AsyncExecute = AsyncExecute.empty()
    @Settable var interactionDistance = 4.0
    @Settable var switchHover = true
    @Settable var visualizeHitbox = false
    @Settable var visibleAll = false
    @Settable var persistent = true

    abstract val clazz: Class<out Display>

    lateinit var entity: Display
    val sEvent = SEvent()
    var hover = false

    abstract fun applyEntity(entity: Display)

    override fun spawn(p: Player, location: Location) {
        entity = location.world.spawn(location, clazz) {
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
            p.showEntity(SJavaPlugin.plugin, it)
        }

        runExecute(onSpawn)
        sEvent.register<PlayerInteractEvent> { e ->
            if (e.player != p) return@register
            if (Utils.isPointInsideRotatedRect(
                    e.player,
                    entity,
                    interactionScale,
                    interactionDistance,
                )
            ) {
                runExecute(onInteract)
            }
        }

        startTick(p)
    }

    override fun remove() {
        entity.remove()
        tickTask?.cancel()
        sEvent.unregisterAll()
    }

    override fun tick(p: Player) {

        if (!entity.isValid) {
            remove()
            return
        }

        val onCursor = Utils.isPointInsideRotatedRect(
            p,
            entity,
            interactionScale,
            interactionDistance,
            visualizeHitbox
        )

        if (!switchHover) {
            if (onCursor) {
                runExecute(onHover)
            } else {
                runExecute(onUnhover)
            }
        } else {
            if (onCursor && !hover) {
                runExecute(onHover)
                hover = true
            } else if (!onCursor && hover) {
                runExecute(onUnhover)
                hover = false
            }
        }
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

    override fun clone(): AbstractElement {
        val element = super.clone() as DisplayBaseElement
        element.displayParameters = displayParameters
        element.interactionScale = interactionScale.clone()
        element.onSpawn = onSpawn
        element.onInteract = onInteract
        element.onHover = onHover
        element.onUnhover = onUnhover
        element.switchHover = switchHover
        element.visualizeHitbox = visualizeHitbox
        element.visibleAll = visibleAll
        element.persistent = persistent
        return element
    }
}