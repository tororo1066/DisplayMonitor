package tororo1066.displaymonitor.actions.builtin

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.util.Vector
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.Utils.mergeConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.element.AbstractElement
import tororo1066.displaymonitor.storage.ElementStorage
import tororo1066.tororopluginapi.utils.addYaw
import tororo1066.tororopluginapi.utils.setPitchL
import java.util.UUID

class SummonElement: AbstractAction() {

    var name = ""
    var presetName = ""
    var offset = Vector(0, 0, 0)
    var relativeOffset = Vector(0, 0, 0)
    var clazz = ""
    var overrideParameters: ConfigurationSection? = null

    override fun run(context: ActionContext) {
        val element: AbstractElement
        val presetElement = ElementStorage.presetElements[presetName]
        if (presetElement != null) {
            element = presetElement.javaClass.getConstructor(ConfigurationSection::class.java)
                .newInstance(presetElement.config.mergeConfiguration(overrideParameters ?: YamlConfiguration()))
        } else {
            val elementClass = ElementStorage.elementClasses[clazz] ?: return
            element = elementClass.getConstructor(ConfigurationSection::class.java)
                .newInstance(overrideParameters ?: YamlConfiguration())
        }

        element.contextUUID = context.uuid

        context.elements[name] = element

        val location = context.location.clone().setPitchL(0f)
            .add(context.location.direction.normalize().multiply(relativeOffset.z))
            .add(context.location.direction.rotateAroundY(90.0).normalize().multiply(relativeOffset.x))
            .add(context.location.direction.rotateAroundZ(-90.0).normalize().multiply(relativeOffset.y))
            .add(offset)
            .addYaw(180f)

        threadBlockingRunTask {
            element.spawn(context.caster, location)
        }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        name = section.getString("name", UUID.randomUUID().toString())!!
        presetName = section.getString("element", "")!!
        offset = section.getBukkitVector("offset") ?: Vector(0, 0, 0)
        relativeOffset = section.getBukkitVector("relativeOffset") ?: Vector(0, 0, 0)
        clazz = section.getString("type", "")!!
        overrideParameters = section.getConfigurationSection("parameters")
    }
}