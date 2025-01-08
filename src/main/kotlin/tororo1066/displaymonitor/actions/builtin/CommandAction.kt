package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionResult

class CommandAction: AbstractAction() {

    var command = ""
    var console = false
    var forceSync = false

    override fun run(context: ActionContext): ActionResult {
        if (command.isBlank()) return ActionResult.noParameters(DisplayMonitor.translate("action.command.empty"))
        val sender = if (console) Bukkit.getConsoleSender() else context.target ?: return ActionResult.targetRequired()
        forceSync.orBlockingTask {
            Bukkit.dispatchCommand(sender, command)
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        command = section.getString("command") ?: ""
        console = section.getBoolean("console", false)
        forceSync = section.getBoolean("forceSync", false)
    }
}