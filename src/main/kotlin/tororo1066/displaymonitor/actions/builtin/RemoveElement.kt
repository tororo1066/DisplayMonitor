package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class RemoveElement: AbstractAction() {

    var name = ""
    var forceSync = false

    override fun run(context: ActionContext) {
        val element = context.elements[name] ?: return

        forceSync.orBlockingTask {
            element.remove(context.caster)
            context.elements.remove(name)
        }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        forceSync = section.getBoolean("forceSync", false)
    }
}