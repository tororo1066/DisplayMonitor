package tororo1066.displaymonitor.actions

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import tororo1066.displaymonitor.Utils.getConfigurationSectionList
import tororo1066.displaymonitor.Utils.mergeConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.tororopluginapi.SJavaPlugin
import java.util.concurrent.CompletableFuture

object ActionRunner {

    fun run(config: ConfigurationSection, p: Player) {
        val configActions = config.getConfigurationSectionList("actions")
        run(configActions, p)
    }

    fun run(actionList: List<ConfigurationSection>, p: Player, context: ActionContext? = null): CompletableFuture<Void> {
        if (actionList.isEmpty()) return CompletableFuture.completedFuture(null)
        val actionContext = context ?: ActionContext()
        actionContext.caster = p
        actionContext.location = p.location

        ActionStorage.contextStorage[actionContext.uuid] = actionContext

        val actions = ArrayList<AbstractAction>()
        for (action in actionList) {
            val actionData = ActionStorage.actions[action.getString("class", "")]
            if (actionData == null) {
                SJavaPlugin.plugin.logger.warning("Action class not found: ${action.getString("class")}")
                continue
            }
            val actionInstance = actionData.getConstructor().newInstance()
            val config = AdvancedConfiguration().mergeConfiguration(action)
            actionInstance.prepare(config)
            actions.add(actionInstance)
        }

        return CompletableFuture.runAsync {
            for (action in actions) {
                action.run(actionContext)
            }
        }
    }
}