package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Location
import org.bukkit.util.Vector
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class TargetAction: AbstractAction() {

    var location: Location? = null
    var offset: Vector? = null
    var relativeOffset: Vector? = null
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