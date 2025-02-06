package tororo1066.displaymonitor.actions.builtin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class MessageAction: AbstractAction() {

    var message: Component? = null

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val message = message ?: return ActionResult.noParameters(DisplayMonitor.translate("action.message.empty"))
        target.sendMessage(message)

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        message = section.getString("message")?.let { MiniMessage.miniMessage().deserialize(it) }
    }
}