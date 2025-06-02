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
        description = "実行するActionの名前。"
    )
    var action = ""
    @ParameterDoc(
        name = "cloneContext",
        description = "コンテキストを複製するか。"
    )
    var cloneContext = false
    @ParameterDoc(
        name = "variables",
        description = "実行時の変数。 コンテキストを複製していると終了後に破棄される。",
        type = IAdvancedConfigurationSection::class
    )
    var variables = mutableMapOf<String, String>()
    @ParameterDoc(
        name = "actionName",
        description = "実行中のActionの名前。 指定した場合必ずコンテキストが複製される。"
    )
    var actionName = ""
    @ParameterDoc(
        name = "override",
        description = "既に存在するactionNameのActionを上書きするか。"
    )
    var override = false

    override fun run(context: IActionContext): ActionResult {
        if (!override && ActionStorage.contextByName.containsKey(actionName)) return ActionResult.failed("Action $actionName already exists.")
        val action = context.publicContext.workspace.actionConfigurations[action] ?: return ActionResult.noParameters("Action $action not found.")
        val newContext = if (cloneContext) context.cloneWithRandomUUID() else context
        variables.forEach { (key, value) ->
            newContext.configuration?.parameters?.put(key, value)
        }
        action.run(newContext, false, actionName.ifEmpty { null })
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
        actionName = section.getString("actionName", "")!!
        override = section.getBoolean("override", false)
    }
}