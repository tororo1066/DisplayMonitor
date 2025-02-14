package tororo1066.displaymonitor.actions.builtin

import com.ezylang.evalex.Expression
import tororo1066.displaymonitor.actions.CheckAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "If",
    description = "指定した条件を満たすかどうかを確認する。"
)
class IfAction: CheckAction() {

    @ParameterDoc(
        name = "expression",
        description = "評価する式。",
        type = ParameterType.String
    )
    var expression = ""

    override fun isAllowed(context: IActionContext): Boolean {
        return Expression(expression).withValues(context.configuration?.parameters ?: mapOf<String, Any>()).evaluate().booleanValue
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        super.prepare(section)
        expression = section.getString("expression", "")!!
    }
}