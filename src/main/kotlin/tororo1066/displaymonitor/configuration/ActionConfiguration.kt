package tororo1066.displaymonitor.configuration

import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitor.storage.VariableStorage
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IActionConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class ActionConfiguration(private val key: String, configuration: IAdvancedConfigurationSection): IActionConfiguration {

    val actions: MutableList<IAdvancedConfigurationSection> = mutableListOf()

    val triggers = mutableMapOf<String, IAdvancedConfigurationSection>()

    val variables = mutableMapOf<String, Any>()

    init {
        val actionSection = configuration.getAdvancedConfigurationSectionList("actions")
        actions.addAll(actionSection)
        configuration.getAdvancedConfigurationSectionList("triggers").forEach {
            val trigger = it.getString("trigger") ?: return@forEach
            triggers[trigger] = it
        }
        configuration.getStringList("variables").forEach {
            val yml = VariableStorage.getVariableFile(it) ?: return@forEach
            yml.getKeys(true).forEach { key ->
                val value = yml.get(key)
                if (value != null) {
                    variables[key] = value
                }
            }
        }
    }

    override fun getKey(): String {
        return key
    }

    override fun run(context: IActionContext, async: Boolean, actionName: String?) {
        val root = context.configuration ?: AdvancedConfiguration()
        val newActions = mutableListOf<AdvancedConfigurationSection>()
        actions.forEach {
            newActions.add(AdvancedConfigurationSection(root, "").apply {
                it.getValues(true).forEach { (key, value) ->
                    set(key, value)
                }
            })
        }
        ActionRunner.run(root, newActions, context, actionName, async = async, false)
    }

    override fun toString(): String {
        return "ActionConfiguration(actions=$actions, triggers=$triggers)"
    }
}