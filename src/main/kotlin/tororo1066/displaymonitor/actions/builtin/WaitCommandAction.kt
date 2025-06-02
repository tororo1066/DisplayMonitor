package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.sEvent.BiSEventUnit
import tororo1066.tororopluginapi.sEvent.SEvent

@ClassDoc(
    name = "WaitCommand",
    description = "指定したコマンドが実行されるまで待機する。"
)
class WaitCommandAction: AbstractAction() {

    companion object {
        private val sEvent = SEvent()
    }

    enum class CommandType {
        TARGET,
        SERVER,
        ANY_PLAYER
    }

    @ParameterDoc(
        name = "command",
        description = "待機するコマンド。"
    )
    var command = ""
    @ParameterDoc(
        name = "then",
        description = "コマンドが実行された場合のアクション。"
    )
    var actions: Execute = Execute.empty()
    @ParameterDoc(
        name = "else",
        description = "タイムアウトした場合のアクション。"
    )
    var failActions: Execute = Execute.empty()
    @ParameterDoc(
        name = "timeout",
        description = "タイムアウトするまでの時間。単位はミリ秒。\n`infinity` で無限に待機する。"
    )
    var timeout = -1L
    var infinity = false

    @ParameterDoc(
        name = "commandType",
        description = "コマンドを実行する対象を指定する。",
        default = "target"
    )
    var commandType = CommandType.TARGET
    @ParameterDoc(
        name = "cancelCommand",
        description = "コマンドの実行をキャンセルするか。"
    )
    var cancelCommand = false

    override fun run(context: IActionContext): ActionResult {
        if (command.isBlank()) return ActionResult.noParameters("Command is empty")
        var complete = false

        val target = context.target
        if (commandType == CommandType.TARGET && target == null) return ActionResult.targetRequired()

        fun process(sender: CommandSender, command: String, unit: BiSEventUnit<*>): Boolean {
            if (context.publicContext.stop) {
                unit.unregister()
                return false
            }
            if (commandType == CommandType.TARGET && target != sender) return false
            if (command != this.command) return false
            val cloneContext = context.clone()
            if (commandType != CommandType.SERVER) {
                cloneContext.target = sender as Entity
            }
            actions(cloneContext)
            unit.unregister()
            complete = true
            return true
        }

        if (commandType == CommandType.SERVER) {
            sEvent.biRegister(ServerCommandEvent::class.java) { e, unit ->
                if (process(e.sender, e.command, unit)) {
                    if (cancelCommand) e.isCancelled = true
                }
            }
        } else {
            sEvent.biRegister(PlayerCommandPreprocessEvent::class.java) { e, unit ->
                if (process(e.player, e.message.removePrefix("/"), unit)) {
                    if (cancelCommand) e.isCancelled = true
                }
            }
        }

        var time = 0L
        while (!complete) {
            if (!infinity && time >= timeout) {
                failActions(context)
                return ActionResult.success()
            }
            Thread.sleep(50)
            if (!infinity) time += 50
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        command = section.getString("command") ?: ""
        actions = section.getConfigExecute("then") ?: actions
        failActions = section.getConfigExecute("else") ?: failActions
        if (section.contains("timeout")) {
            val timeout = section.get("timeout")
            if (timeout is Int) {
                this.timeout = timeout.toLong()
            } else if (timeout is String && timeout == "infinity") {
                infinity = true
            }
        }
        commandType = section.getEnum("commandType", CommandType::class.java, CommandType.TARGET)
        cancelCommand = section.getBoolean("cancelCommand", false)
    }
}