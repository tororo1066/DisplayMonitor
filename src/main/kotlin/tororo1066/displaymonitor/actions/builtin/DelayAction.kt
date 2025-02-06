package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class DelayAction: AbstractAction() {

    var delay = 0L

    override fun run(context: IActionContext): ActionResult {
        Thread.sleep(delay)
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        delay = section.getLong("delay", 0L)
    }
}