package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class ModifyVariableAction: AbstractAction() {

    var variable = ""
    var value = ""

    override fun run(context: IActionContext): ActionResult {
        val configuration = context.configuration ?: return ActionResult.failed("No configuration found")
        val parameters = configuration.parameters
        parameters[variable] = value
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        variable = section.getString("variable", "")!!
        value = section.getString("value", "")!!
    }
}