package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction

class CommandAction: AbstractAction() {

    var command = ""
    var console = false
    var forceSync = false

    override fun run(context: ActionContext) {
        if (command.isBlank()) return
        forceSync.orBlockingTask {
            Bukkit.dispatchCommand(if (console) Bukkit.getConsoleSender() else context.caster, command)
        }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        command = section.getString("command") ?: ""
        console = section.getBoolean("console", false)
        forceSync = section.getBoolean("forceSync", false)
    }
}