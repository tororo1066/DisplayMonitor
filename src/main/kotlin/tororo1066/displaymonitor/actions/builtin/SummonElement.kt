package tororo1066.displaymonitor.actions.builtin

import org.bukkit.util.Vector
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.storage.ElementStorage
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.utils.addYaw
import java.util.UUID

class SummonElement: AbstractAction() {

    override val allowedAutoStop = false

    var name = ""
    var presetName = ""
    var offset = Vector(0, 0, 0)
    var relativeOffset = Vector(0, 0, 0)
    var clazz = ""
    var overrideParameters: IAdvancedConfigurationSection? = null

    var lockPitch = false

    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val location = context.location?.clone() ?: return ActionResult.locationRequired()

        val element = ElementStorage.createElement(presetName, clazz, overrideParameters, "SummonElement")
            ?: return ActionResult.noParameters(DisplayMonitor.translate("action.summonElement.noElement"))

        element.groupUUID = context.groupUUID
        element.contextUUID = context.uuid

        context.publicContext.elements[name] = element

        val clone = location.clone()
        location
            .add(clone.direction.normalize().multiply(relativeOffset.z))
            .add(clone.direction.rotateAroundY(90.0).normalize().multiply(relativeOffset.x))
            .add(clone.direction.rotateAroundZ(-90.0).normalize().multiply(relativeOffset.y))
            .add(offset)
            .addYaw(180f)

        if (lockPitch) {
            location.pitch = 0f
        }

        forceSync.orBlockingTask {
            element.spawn(target, location)
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
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