package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class ModifyVariableAction: AbstractAction() {

    var variable = ""
    var value = ""

    override fun run(context: ActionContext): ActionResult {
        val configuration = context.configuration ?: return ActionResult.failed("No configuration found")
        val parameters = configuration.parameters
        parameters[variable] = value
        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        variable = section.getString("variable", "")!!
        value = section.getString("value", "")!!
    }
}