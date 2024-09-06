package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction

class CommandAction: AbstractAction() {

    var command = ""
    var console = false

    override fun run(context: ActionContext) {
        if (command.isBlank()) return
        val cmd = replacePlaceholders(command, context)
        threadBlockingRunTask {
            if (console) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
            } else {
                Bukkit.dispatchCommand(context.caster, cmd)
            }
        }
    }

    private fun replacePlaceholders(str: String, context: ActionContext): String {
        return str
            .replace("%player%", context.caster.name)
            .replace("%world%", context.caster.world.name)
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        command = section.getString("command") ?: ""
        console = section.getBoolean("console", false)
    }
}