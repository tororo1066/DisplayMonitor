package tororo1066.displaymonitor.actions.builtin.debug

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class PrintVariables: AbstractAction() {

    override fun run(context: IActionContext): ActionResult {
        val variables = context.configuration?.parameters ?: return ActionResult.noParameters("No variables found.")
        variables.forEach { (key, value) ->
            context.target?.sendMessage("$key: $value")
        }
        context.publicContext.parameters.forEach { (key, value) ->
            context.target?.sendMessage("Public $key: $value")
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
    }
}