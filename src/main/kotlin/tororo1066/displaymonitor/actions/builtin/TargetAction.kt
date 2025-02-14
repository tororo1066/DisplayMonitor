package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Location
import org.bukkit.util.Vector
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Target",
    description = "位置を指定してアクションを実行する。"
)
class TargetAction: AbstractAction() {

    @ParameterDoc(
        name = "location",
        description = "実行する位置。",
        type = ParameterType.Location
    )
    var location: Location? = null
    @ParameterDoc(
        name = "offset",
        description = "位置のオフセット。",
        type = ParameterType.Vector
    )
    var offset: Vector? = null
    @ParameterDoc(
        name = "relativeOffset",
        description = "位置の相対オフセット。",
        type = ParameterType.Vector
    )
    var relativeOffset: Vector? = null
    @ParameterDoc(
        name = "actions",
        description = "実行するアクションのリスト。",
        type = ParameterType.Actions
    )
    var actions: Execute = Execute.empty()

    override fun run(context: IActionContext): ActionResult {

        val cloneContext = context.cloneWithRandomUUID()

        location?.let {
            val contextLocation = cloneContext.location
            if (contextLocation != null && it.world == null) {
                it.world = contextLocation.world
            }
            cloneContext.location = it
        }

        cloneContext.location?.let {
            offset?.let { offset ->
                it.add(offset)
            }

            relativeOffset?.let { relativeOffset ->
                val direction = it.direction
                it.add(direction.clone().rotateAroundY(Math.toRadians(90.0)).multiply(relativeOffset.x))
                it.add(direction.clone().multiply(relativeOffset.z))
                it.add(Vector(0.0, relativeOffset.y, 0.0))
            }
        }

        actions(cloneContext)

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        location = section.getStringLocation("location")
        offset = section.getBukkitVector("offset")
        relativeOffset = section.getBukkitVector("relativeOffset")
        actions = section.getConfigExecute("actions", actions)
    }
}