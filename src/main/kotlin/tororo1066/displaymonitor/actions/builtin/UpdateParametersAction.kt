package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class UpdateParametersAction: AbstractAction() {

    override fun run(context: ActionContext) {
        context.configuration?.parameters?.putAll(context.getDefaultParameters())
    }

    override fun prepare(section: AdvancedConfigurationSection) {
    }
}