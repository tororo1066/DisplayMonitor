package tororo1066.displaymonitor.actions.builtin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionResult

class MessageAction: AbstractAction() {

    var message: Component? = null

    override fun run(context: ActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val message = message ?: return ActionResult.noParameters(DisplayMonitor.translate("action.message.empty"))
        target.sendMessage(message)

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        message = section.getString("message")?.let { MiniMessage.miniMessage().deserialize(it) }
    }
}