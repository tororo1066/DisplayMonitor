package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "RunAction",
    description = "指定されたActionを実行する。"
)
class RunAction: AbstractAction() {

    @ParameterDoc(
        name = "action",
        description = "実行するActionの名前。",
        type = ParameterType.String
    )
    var action = ""
    @ParameterDoc(
        name = "cloneContext",
        description = "コンテキストを複製するか。",
        type = ParameterType.Boolean
    )
    var cloneContext = false
    @ParameterDoc(
        name = "variables",
        description = "実行時の変数。 コンテキストを複製していると終了後に破棄される。",
        type = ParameterType.AdvancedConfigurationSection
    )
    var variables = mutableMapOf<String, String>()

    override fun run(context: IActionContext): ActionResult {
        val action = context.publicContext.workspace.actionConfigurations[action] ?: return ActionResult.noParameters("Action $action not found.")
        val newContext = if (cloneContext) context.cloneWithRandomUUID() else context
        variables.forEach { (key, value) ->
            newContext.configuration?.parameters?.put(key, value)
        }
        action.run(newContext, false, null)
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        action = section.getString("action", "")!!
        cloneContext = section.getBoolean("cloneContext", false)
        variables.clear()
        section.getAdvancedConfigurationSection("variables")?.let { variablesSection ->
            variablesSection.getKeys(false).forEach { key ->
                variables[key] = variablesSection.getString(key) ?: ""
            }
        }
    }
}