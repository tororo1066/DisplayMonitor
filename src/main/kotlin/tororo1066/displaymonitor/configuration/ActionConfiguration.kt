package tororo1066.displaymonitor.configuration

import org.bukkit.entity.Player
import tororo1066.displaymonitor.Utils.clone
import tororo1066.displaymonitor.actions.ActionRunner

class ActionConfiguration(val root: AdvancedConfiguration, configuration: AdvancedConfigurationSection) {

    val actions: MutableList<AdvancedConfigurationSection> = mutableListOf()

    val triggers = mutableListOf<String>()

    init {
        val actionSection = configuration.getAdvancedConfigurationSectionList("actions")
        actions.addAll(actionSection)
        triggers.addAll(configuration.getStringList("triggers"))
    }

    fun run(p: Player) {
        ActionRunner.run(root.clone(), actions, p, async = true)
    }

    override fun toString(): String {
        return "ActionConfiguration(actions=$actions, triggers=$triggers)"
    }
}