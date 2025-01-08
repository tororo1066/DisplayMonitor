package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionResult

class RemoveAllElement: AbstractAction() {

    var forceSync = false

    override fun run(context: ActionContext): ActionResult {
        forceSync.orBlockingTask {
            context.publicContext.elements.values.forEach { it.remove() }
            context.publicContext.elements.clear()
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}