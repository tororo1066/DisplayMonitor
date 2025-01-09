package tororo1066.displaymonitor.configuration

import org.bukkit.entity.Player
import tororo1066.displaymonitor.Utils.clone
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitor.actions.PublicActionContext

class ActionConfiguration(val root: AdvancedConfiguration, configuration: AdvancedConfigurationSection) {

    val actions: MutableList<AdvancedConfigurationSection> = mutableListOf()

    val triggers = mutableMapOf<String, AdvancedConfigurationSection>()

    init {
        val actionSection = configuration.getAdvancedConfigurationSectionList("actions")
        actions.addAll(actionSection)
        configuration.getAdvancedConfigurationSectionList("triggers").forEach {
            val trigger = it.getString("trigger") ?: return@forEach
            triggers[trigger] = it
        }
    }

    fun run(p: Player) {
        ActionRunner.run(root.clone(), actions, ActionContext(PublicActionContext(), p), async = true)
    }

    fun run(context: ActionContext) {
        ActionRunner.run(root.clone(), actions, context, async = true)
    }

    override fun toString(): String {
        return "ActionConfiguration(actions=$actions, triggers=$triggers)"
    }
}