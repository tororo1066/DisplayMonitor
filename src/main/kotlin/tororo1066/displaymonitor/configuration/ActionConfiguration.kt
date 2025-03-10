package tororo1066.displaymonitor.configuration

import org.bukkit.entity.Player
import tororo1066.displaymonitor.Utils.clone
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitor.actions.PublicActionContext
import tororo1066.displaymonitorapi.actions.IActionContext

class ActionConfiguration(private val root: AdvancedConfiguration, configuration: AdvancedConfigurationSection) {

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

    fun run(context: IActionContext, async: Boolean = true, actionName: String? = null) {
        ActionRunner.run(root.clone(), actions, context, actionName, async = async, false)
    }

    override fun toString(): String {
        return "ActionConfiguration(actions=$actions, triggers=$triggers)"
    }
}