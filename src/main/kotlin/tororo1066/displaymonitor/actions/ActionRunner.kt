package tororo1066.displaymonitor.actions

import org.bukkit.entity.Player
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.storage.ActionStorage
import java.util.concurrent.CompletableFuture

object ActionRunner {

    val runContext = "ActionRunner"

    fun run(config: AdvancedConfiguration, p: Player) {
        val configActions = config.getAdvancedConfigurationSectionList("actions")
        run(config, configActions, p)
    }

    fun run(
        root: AdvancedConfiguration,
        actionList: List<AdvancedConfigurationSection>,
        p: Player,
        context: ActionContext? = null,
        async: Boolean = false
    ) {
        if (actionList.isEmpty()) return

        val actionContext = context ?: ActionContext()
        actionContext.configuration = root
        actionContext.caster = p
        actionContext.location = p.location.clone()

        root.parameters.putAll(actionContext.getDefaultParameters())

//        if (context == null) {
//            ActionStorage.contextStorage[actionContext.uuid] = actionContext
//        }
        ActionStorage.contextStorage
            .computeIfAbsent(actionContext.groupUUID)
            { mutableMapOf() }[actionContext.uuid] = actionContext

//        if (actionList.none { it.getString("class") == "Stop" }) {
//            DisplayMonitor.warn(runContext, DisplayMonitor.translate("action.stop.not.found"))
//        }

        fun invokeActions() {
            for (action in actionList) {
                if (actionContext.stop) {
                    ActionStorage.contextStorage[actionContext.groupUUID]?.remove(actionContext.uuid)
                    if (ActionStorage.contextStorage[actionContext.groupUUID]?.isEmpty() == true) {
                        ActionStorage.contextStorage.remove(actionContext.groupUUID)
                    }
                    break
                }
                val actionClass = action.getString("class")
                val actionData = ActionStorage.actions[actionClass]
                if (actionData == null) {
                    DisplayMonitor.warn(runContext, DisplayMonitor.translate("action.class.not.found", actionClass))
                    continue
                }
                val actionInstance = actionData.getConstructor().newInstance()
                try {
                    actionInstance.prepare(action)
                } catch (e: Exception) {
                    DisplayMonitor.error(runContext, DisplayMonitor.translate("action.prepare.error", actionClass, e.message))
                    e.printStackTrace()
                    continue
                }
                try {
                    actionInstance.run(actionContext)
                } catch (e: Exception) {
                    DisplayMonitor.error(runContext, DisplayMonitor.translate("action.running.error", actionClass, e.message))
                    e.printStackTrace()
                }
            }
        }

        if (async) {
            CompletableFuture.runAsync {
                invokeActions()
            }.exceptionally {
                DisplayMonitor.error(runContext, DisplayMonitor.translate("action.unknown.error", it.message))
                it.printStackTrace()
                null
            }
        } else {
            invokeActions()
        }
    }
}