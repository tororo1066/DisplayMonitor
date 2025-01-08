package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class EndAction: AbstractAction() {

    var forceSync = false

    override fun run(context: ActionContext): ActionResult {
        forceSync.orBlockingTask {
            context.publicContext.elements.forEach { (_, element) ->
                element.remove()
            }
        }
        context.publicContext.stop = true

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}