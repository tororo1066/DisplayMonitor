package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class LightningAction: AbstractAction() {

    var effectOnly = false

    override fun run(context: IActionContext): ActionResult {
        val location = context.location ?: return ActionResult.failed()
        if (!effectOnly) {
            location.world.strikeLightning(location)
        } else {
            location.world.strikeLightningEffect(location)
        }

        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        effectOnly = configuration.getBoolean("effectOnly", false)
    }
}