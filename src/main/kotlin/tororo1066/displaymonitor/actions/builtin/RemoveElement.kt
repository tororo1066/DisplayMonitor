package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class RemoveElement: AbstractAction() {

    var name = ""
    var forceSync = false

    override fun run(context: ActionContext): ActionResult {
        val element = context.elements[name] ?: return ActionResult.noParameters(DisplayMonitor.translate("action.removeElement.notFound", name))

        forceSync.orBlockingTask {
            element.remove()
            context.elements.remove(name)
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        forceSync = section.getBoolean("forceSync", false)
    }
}