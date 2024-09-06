package tororo1066.displaymonitor.actions.builtin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.actions.AbstractAction

class MessageAction: AbstractAction() {

    var message: Component? = null

    override fun run(context: ActionContext) {
        message?.let { context.caster.sendMessage(it) }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        message = section.getString("message")?.let { MiniMessage.miniMessage().deserialize(it) }
    }
}