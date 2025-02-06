package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class StopAction: AbstractAction() {

    override fun run(context: IActionContext): ActionResult {
        context.stop = true
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
    }
}