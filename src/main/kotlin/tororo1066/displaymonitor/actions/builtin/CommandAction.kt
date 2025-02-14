package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Command",
    description = "コマンドを実行する。"
)
class CommandAction: AbstractAction() {

    @ParameterDoc(
        name = "command",
        description = "実行するコマンド。",
        type = ParameterType.String
    )
    var command = ""
    @ParameterDoc(
        name = "console",
        description = "コンソールから実行するか。",
        type = ParameterType.Boolean
    )
    var console = false
    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。",
        type = ParameterType.Boolean
    )
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