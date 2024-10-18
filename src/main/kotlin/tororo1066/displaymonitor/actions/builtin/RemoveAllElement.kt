package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction

class RemoveAllElement: AbstractAction() {

    var forceSync = false

    override fun run(context: ActionContext) {
        forceSync.orBlockingTask {
            context.elements.values.forEach { it.remove(context.caster) }
            context.elements.clear()
        }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}