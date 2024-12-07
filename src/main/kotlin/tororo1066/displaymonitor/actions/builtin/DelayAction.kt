package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionResult

class DelayAction: AbstractAction() {

    var delay = 0L

    override fun run(context: ActionContext): ActionResult {
        Thread.sleep(delay)
        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        delay = section.getLong("delay", 0L)
    }
}