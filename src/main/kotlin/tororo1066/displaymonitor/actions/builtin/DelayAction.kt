package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction

class DelayAction: AbstractAction() {

    var delay = 0L

    override fun run(context: ActionContext) {
        Thread.sleep(delay)
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        delay = section.getLong("delay", 0L)
    }
}