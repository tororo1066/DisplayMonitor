package tororo1066.displaymonitor.actions.builtin

import org.bukkit.util.Vector
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.storage.ElementStorage
import tororo1066.tororopluginapi.utils.addYaw
import java.util.UUID

class SummonElement: AbstractAction() {

    var name = ""
    var presetName = ""
    var offset = Vector(0, 0, 0)
    var relativeOffset = Vector(0, 0, 0)
    var clazz = ""
    var overrideParameters: AdvancedConfigurationSection? = null

    var lockPitch = false

    var forceSync = false

    override fun run(context: ActionContext): ActionResult {
        val caster = context.caster ?: return ActionResult.playerRequired()
        val location = context.location?.clone() ?: return ActionResult.locationRequired()

        val element = ElementStorage.createElement(presetName, clazz, overrideParameters, "SummonElement")
            ?: return ActionResult.noParameters(DisplayMonitor.translate("action.summonElement.noElement"))

        element.groupUUID = context.groupUUID
        element.contextUUID = context.uuid

        context.elements[name] = element
        if (lockPitch) {
            location.pitch = 0f
        }
        val clone = location.clone()
        location
            .add(clone.direction.normalize().multiply(relativeOffset.z))
            .add(clone.direction.rotateAroundY(90.0).normalize().multiply(relativeOffset.x))
            .add(clone.direction.rotateAroundZ(-90.0).normalize().multiply(relativeOffset.y))
            .add(offset)
            .addYaw(180f)

        forceSync.orBlockingTask {
            element.spawn(caster, location)
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        name = section.getString("name", UUID.randomUUID().toString())!!
        presetName = section.getString("preset", "")!!
        offset = section.getBukkitVector("offset") ?: Vector(0, 0, 0)
        relativeOffset = section.getBukkitVector("relativeOffset") ?: Vector(0, 0, 0)
        clazz = section.getString("type", "")!!
        overrideParameters = section.getAdvancedConfigurationSection("parameters")
        lockPitch = section.getBoolean("lockPitch", false)
        forceSync = section.getBoolean("forceSync", false)
    }
}