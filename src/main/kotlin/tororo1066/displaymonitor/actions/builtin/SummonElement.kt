package tororo1066.displaymonitor.actions.builtin

import org.bukkit.util.Vector
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.storage.ElementStorage
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.utils.addYaw
import java.util.UUID

@ClassDoc(
    name = "SummonElement",
    description = "Elementを召喚する。"
)
class SummonElement: AbstractAction() {

    override val allowedAutoStop = false

    @ParameterDoc(
        name = "name",
        description = "Elementの名前。"
    )
    var name = ""
    @ParameterDoc(
        name = "preset",
        description = "Elementのプリセット名。 presetが指定されている場合、typeは無視される。"
    )
    var presetName = ""
    @ParameterDoc(
        name = "offset",
        description = "Elementの位置のオフセット。"
    )
    var offset = Vector(0, 0, 0)
    @ParameterDoc(
        name = "relativeOffset",
        description = "Elementの位置の相対オフセット。"
    )
    var relativeOffset = Vector(0, 0, 0)
    @ParameterDoc(
        name = "type",
        description = "Elementのクラス名。"
    )
    var clazz = ""
    @ParameterDoc(
        name = "parameters",
        description = "Elementのパラメータ。"
    )
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
            .add(clone.direction.normalize().rotateAroundY(Math.toRadians(90.0)).multiply(relativeOffset.x))
            .add(clone.direction.normalize().rotateAroundZ(Math.toRadians(-90.0)).multiply(relativeOffset.y))
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