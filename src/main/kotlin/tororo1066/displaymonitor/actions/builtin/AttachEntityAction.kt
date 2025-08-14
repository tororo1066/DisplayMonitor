package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class AttachEntityAction: AbstractAction() {

    var element: String = ""
    var forceSync: Boolean = false

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val element = context.publicContext.elements[element] ?: return ActionResult.noParameters("Element not found: $element")
        forceSync.orBlockingTask {
            element.attachEntity(target)
        }
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        element = configuration.getString("element", element) ?: element
        forceSync = configuration.getBoolean("forceSync", forceSync)
    }
}