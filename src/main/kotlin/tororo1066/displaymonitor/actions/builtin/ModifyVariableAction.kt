package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "ModifyVariable",
    description = "変数を変更する。"
)
class ModifyVariableAction: AbstractAction() {

    @ParameterDoc(
        name = "variable",
        description = "変更する変数の名前。",
        type = ParameterType.String
    )
    var variable = ""
    @ParameterDoc(
        name = "value",
        description = "変数に代入する値。",
        type = ParameterType.String
    )
    var value: Any = ""
    @ParameterDoc(
        name = "variables",
        description = "変数のマップ。",
        type = ParameterType.AdvancedConfigurationSection
    )
    var variables = mutableMapOf<String, Any>()

    override fun run(context: IActionContext): ActionResult {
        val configuration = context.configuration ?: return ActionResult.failed("No configuration found")
        val parameters = configuration.parameters
        variables.forEach { (key, value) ->
            parameters[key] = value
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        variable = section.getString("variable", "")!!
        value = section.get("value", "")!!

        variables.clear()
        variables[variable] = value
        section.getAdvancedConfigurationSection("variables")?.let { variablesSection ->
            variablesSection.getKeys(false).forEach { key ->
                variables[key] = variablesSection.get(key) ?: ""
            }
        }
    }
}