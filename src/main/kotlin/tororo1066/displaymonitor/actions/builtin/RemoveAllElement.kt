package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class RemoveAllElement: AbstractAction() {

    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        forceSync.orBlockingTask {
            context.publicContext.elements.values.forEach { it.remove() }
            context.publicContext.elements.clear()
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}