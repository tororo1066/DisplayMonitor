package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction

class RemoveAllElement: AbstractAction() {

    override fun run(context: ActionContext) {
        context.elements.values.forEach {
            it.remove(context.caster)
        }
        context.elements.clear()
    }

    override fun prepare(section: AdvancedConfigurationSection) {

    }
}