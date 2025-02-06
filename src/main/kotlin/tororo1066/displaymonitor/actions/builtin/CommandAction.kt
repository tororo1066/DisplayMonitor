package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class CommandAction: AbstractAction() {

    var command = ""
    var console = false
    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        if (command.isBlank()) return ActionResult.noParameters(DisplayMonitor.translate("action.command.empty"))
        val sender = if (console) Bukkit.getConsoleSender() else context.target ?: return ActionResult.targetRequired()
        forceSync.orBlockingTask {
            Bukkit.dispatchCommand(sender, command)
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        command = section.getString("command") ?: ""
        console = section.getBoolean("console", false)
        forceSync = section.getBoolean("forceSync", false)
    }
}