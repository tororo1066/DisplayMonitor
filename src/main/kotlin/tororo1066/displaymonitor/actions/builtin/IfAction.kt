package tororo1066.displaymonitor.actions.builtin

import com.ezylang.evalex.Expression
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.CheckAction
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class IfAction: CheckAction() {

    var expression = ""

    override fun isAllowed(context: ActionContext): Boolean {
        return Expression(expression).withValues(context.parameters ?: mapOf<String, Any>()).evaluate().booleanValue
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        super.prepare(section)
        expression = section.getString("expression", "")!!
    }
}