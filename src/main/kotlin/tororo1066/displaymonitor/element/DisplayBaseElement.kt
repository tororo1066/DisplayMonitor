package tororo1066.displaymonitor.element

import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitor.configuration.DisplayParameters
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.Utils.getBukkitVector
import tororo1066.displaymonitor.Utils.getConfigurationSectionList
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sEvent.SEvent

abstract class DisplayBaseElement(config: ConfigurationSection): AbstractElement(config) {

    var displayParameters = DisplayParameters.fromConfig(
        config.getConfigurationSection("displayParameters") ?: YamlConfiguration()
    )
    var scale = config.getBukkitVector("scale") ?: Vector(1.0, 1.0, 1.0)
    var onSpawn = config.getConfigurationSectionList("onSpawn")
    var onInteract = config.getConfigurationSectionList("onInteract")
    var onHover = config.getConfigurationSectionList("onHover")
    var onUnhover = config.getConfigurationSectionList("onUnhover")
    var switchHover = config.getBoolean("switchHover", false)
    var visualizeHitbox = config.getBoolean("visualizeHitbox", false)

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
            applyEntity(it)
            it.isVisibleByDefault = false
            p.showEntity(SJavaPlugin.plugin, it)
        }

        ActionRunner.run(onSpawn, p, getContext())
        sEvent.register<PlayerInteractEvent> { e ->
            if (e.player != p) return@register
            if (Utils.isPointInsideRotatedRect(
                    e.player,
                    entity,
                    scale,
                    4.0
                )
            ) {
                ActionRunner.run(onInteract, p, getContext())
            }
        }

        startTick(p)
    }

    override fun remove(p: Player) {
        entity.remove()
        tickTask?.cancel()
        sEvent.unregisterAll()
    }

    override fun tick(p: Player) {

        val onCursor = Utils.isPointInsideRotatedRect(
            p,
            entity,
            scale,
            4.0,
            visualizeHitbox
        )

        if (!switchHover) {
            if (onCursor) {
                ActionRunner.run(onHover, p, getContext())
            } else {
                ActionRunner.run(onUnhover, p, getContext())
            }
        } else {
            if (onCursor && !hover) {
                ActionRunner.run(onHover, p, getContext())
                hover = true
            } else if (!onCursor && hover) {
                ActionRunner.run(onUnhover, p, getContext())
                hover = false
            }
        }
    }

    override fun edit(p: Player, edit: ConfigurationSection) {
        displayParameters.edit(edit.getConfigurationSection("displayParameters") ?: YamlConfiguration())
        scale = edit.getBukkitVector("scale") ?: scale
        onSpawn = edit.getConfigurationSectionList("onSpawn", onSpawn)
        onInteract = edit.getConfigurationSectionList("onInteract", onInteract)
        onHover = edit.getConfigurationSectionList("onHover", onHover)
        onUnhover = edit.getConfigurationSectionList("onUnHover", onUnhover)
        switchHover = edit.getBoolean("switchHover", switchHover)
        visualizeHitbox = edit.getBoolean("visualizeHitbox", visualizeHitbox)
        entity.billboard = displayParameters.billboard
        entity.transformation = displayParameters.getTransformation()
        entity.interpolationDelay = displayParameters.interpolationDelay
        entity.interpolationDuration = displayParameters.interpolationDuration
        entity.teleportDuration = displayParameters.teleportDuration
        applyEntity(entity)
    }
}