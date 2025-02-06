package tororo1066.displaymonitor.actions.builtin

import com.ezylang.evalex.Expression
import tororo1066.displaymonitor.actions.CheckAction
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class IfAction: CheckAction() {

    var expression = ""

    override fun isAllowed(context: IActionContext): Boolean {
        return Expression(expression).withValues(context.configuration?.parameters ?: mapOf<String, Any>()).evaluate().booleanValue
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        super.prepare(section)
        expression = section.getString("expression", "")!!
    }
}