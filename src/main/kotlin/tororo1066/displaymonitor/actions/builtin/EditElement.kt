package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class EditElement: AbstractAction() {

    var name = ""
    var edit: AdvancedConfigurationSection? = null
    var forceSync = false

    override fun run(context: ActionContext) {
        val element = context.elements[name] ?: return
        forceSync.orBlockingTask {
            element.edit(edit ?: AdvancedConfiguration())
        }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        edit = section.getAdvancedConfigurationSection("edit")
        forceSync = section.getBoolean("forceSync", false)
    }
}