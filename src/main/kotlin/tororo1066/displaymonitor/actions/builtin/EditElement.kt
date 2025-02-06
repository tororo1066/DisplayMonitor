package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class EditElement: AbstractAction() {

    var name = ""
    var edit: IAdvancedConfigurationSection? = null
    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        val element = context.publicContext.elements[name] ?: return ActionResult.noParameters(DisplayMonitor.translate("action.editElement.notFound", name))
        forceSync.orBlockingTask {
            element.edit(edit ?: AdvancedConfiguration())
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        edit = section.getAdvancedConfigurationSection("edit")
        forceSync = section.getBoolean("forceSync", false)
    }
}