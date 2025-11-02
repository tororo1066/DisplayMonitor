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

    enum class Scope {
        GLOBAL, LOCAL
    }

    @ParameterDoc(
        name = "variable",
        description = "変更する変数の名前。"
    )
    var variable: String? = null
    @ParameterDoc(
        name = "value",
        description = "変数に代入する値。"
    )
    var value: Any? = null
    @ParameterDoc(
        name = "variables",
        description = "変数のマップ。",
        type = IAdvancedConfigurationSection::class
    )
    var variables = mutableMapOf<String, Any>()
    @ParameterDoc(
        name = "scope",
        description = "変数のスコープ。GLOBALはグローバル変数、LOCALはローカル変数。"
    )
    var scope: Scope = Scope.LOCAL

    private fun formatValue(value: Any): Any {
        return when (value) {
            is IAdvancedConfigurationSection -> {
                value.getEvaluatedValues(true)
                    .mapNotNull { (k, v) -> k to formatValue(v ?: return@mapNotNull null) }
                    .toMap()
            }
            is List<*> -> {
                value.mapNotNull { formatValue(it ?: return@mapNotNull null) }
            }
            else -> {
                value
            }
        }
    }

    override fun run(context: IActionContext): ActionResult {
        val parameters = if (scope == Scope.GLOBAL) {
            context.publicContext.parameters
        } else {
            context.configuration?.parameters ?: return ActionResult.failed("No local parameters found")
        }
        variables.forEach { (key, value) ->
            parameters[key] = formatValue(value)
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        variable = section.getString("variable")
        value = section.get("value")

        variables.clear()

        variable?.let {
            variables[it] = value ?: ""
        }

        section.getAdvancedConfigurationSection("variables")?.let { variablesSection ->
            variablesSection.getKeys(false).forEach { key ->
                variables[key] = variablesSection.get(key) ?: ""
            }
        }
        scope = section.getEnum("scope", Scope::class.java, Scope.LOCAL)
    }
}