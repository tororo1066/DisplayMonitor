package tororo1066.displaymonitor.actions.builtin

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction

class EditElement: AbstractAction() {

    var name = ""
    var edit: ConfigurationSection? = null

    override fun run(context: ActionContext) {
        val element = context.elements[name] ?: return
        threadBlockingRunTask {
            element.edit(context.caster, edit ?: YamlConfiguration())
        }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        edit = section.getConfigurationSection("edit")
    }
}