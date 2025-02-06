package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class RemoveElement: AbstractAction() {

    var name = ""
    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        val element = context.publicContext.elements[name] ?: return ActionResult.noParameters(DisplayMonitor.translate("action.removeElement.notFound", name))

        forceSync.orBlockingTask {
            element.remove()
            context.publicContext.elements.remove(name)
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        forceSync = section.getBoolean("forceSync", false)
    }
}