package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.Execute
import tororo1066.tororopluginapi.sEvent.BiSEventUnit
import tororo1066.tororopluginapi.sEvent.SEvent

class WaitCommandAction: AbstractAction() {

    companion object {
        private val sEvent = SEvent()
    }

    var command = ""
    var actions: Execute = Execute.empty()
    var failActions: Execute = Execute.empty()
    var timeout = -1L
    var infinite = false
    var server = false
    var cancelCommand = false

    override fun run(context: ActionContext): ActionResult {
        if (command.isBlank()) return ActionResult.noParameters("Command is empty")
        val sender = if (server) Bukkit.getConsoleSender() else context.caster ?: return ActionResult.playerRequired()
        var complete = false

        fun process(sender: CommandSender, command: String, unit: BiSEventUnit<*>): Boolean {
            if (!server && sender != context.caster) return false
            if (command != this.command) return false
            actions(context)
            unit.unregister()
            complete = true
            return true
        }

        if (server) {
            sEvent.biRegister(ServerCommandEvent::class.java) { e, unit ->
                if (process(sender, e.command, unit)) {
                    if (cancelCommand) e.isCancelled = true
                }
            }
        } else {
            sEvent.biRegister(PlayerCommandPreprocessEvent::class.java) { e, unit ->
                if (process(sender, e.message.removePrefix("/"), unit)) {
                    if (cancelCommand) e.isCancelled = true
                }
            }
        }

        var time = 0L
        while (!complete) {
            if (!infinite && time >= timeout) {
                failActions(context)
                return ActionResult.success()
            }
            Thread.sleep(50)
            if (!infinite) time += 50
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        command = section.getString("command") ?: ""
        actions = section.getConfigExecute("actions") ?: actions
        failActions = section.getConfigExecute("fail") ?: failActions
        timeout = section.getLong("timeout", -1L)
        infinite = section.getBoolean("infinite", timeout == -1L)
        server = section.getBoolean("server", false)
        cancelCommand = section.getBoolean("cancelCommand", false)
    }
}