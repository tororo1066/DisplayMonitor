package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class EndAction: AbstractAction() {

    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        forceSync.orBlockingTask {
            context.publicContext.elements.forEach { (_, element) ->
                element.remove()
            }
            context.publicContext.elements.clear()
        }
        context.publicContext.stop = true

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}