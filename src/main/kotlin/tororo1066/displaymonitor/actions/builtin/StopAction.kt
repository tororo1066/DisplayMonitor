package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class StopAction: AbstractAction() {

    override fun run(context: ActionContext): ActionResult {
        context.stop = true
        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
    }
}