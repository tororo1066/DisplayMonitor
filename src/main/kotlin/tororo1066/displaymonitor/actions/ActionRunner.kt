package tororo1066.displaymonitor.actions

import org.bukkit.entity.Player
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.parameters.ActionParameters
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.storage.ActionStorage
import java.util.concurrent.CompletableFuture

object ActionRunner {

    private const val runContext = "ActionRunner"

    fun run(config: AdvancedConfiguration, p: Player) {
        val configActions = config.getAdvancedConfigurationSectionList("actions")
        run(config, configActions, ActionContext(PublicActionContext(), p))
    }

    fun run(
        root: AdvancedConfiguration,
        actionList: List<AdvancedConfigurationSection>,
        context: ActionContext,
        async: Boolean = false
    ) {
        if (actionList.isEmpty()) return

        context.configuration = root

        root.parameters.putAll(context.prepareParameters)
        root.parameters.putAll(context.getDefaultParameters())
        val caster = context.caster
        if (caster != null) {
            root.parameters.putAll(ActionParameters.getEntityParameters("caster", caster))
        }
        val target = context.target
        if (target != null) {
            root.parameters.putAll(ActionParameters.getEntityParameters("target", target))
        }

        ActionStorage.contextStorage
            .computeIfAbsent(context.groupUUID)
            { mutableMapOf() }[context.uuid] = context

//        if (actionList.none { it.getString("class") == "Stop" }) {
//            DisplayMonitor.warn(runContext, DisplayMonitor.translate("action.stop.not.found"))
//        }

        fun invokeActions() {
            for (action in actionList) {
                if (context.publicContext.stop) {
                    break
                }
                if (context.stop) {
                    ActionStorage.contextStorage[context.groupUUID]?.remove(context.uuid)
                    if (ActionStorage.contextStorage[context.groupUUID]?.isEmpty() == true) {
                        ActionStorage.contextStorage.remove(context.groupUUID)
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
                    DisplayMonitor.error(
                        runContext,
                        DisplayMonitor.translate("action.prepare.error", actionClass, e.message)
                    )
                    e.printStackTrace()
                    continue
                }
                try {
                    actionInstance.run(context)
                } catch (e: Exception) {
                    DisplayMonitor.error(
                        runContext,
                        DisplayMonitor.translate("action.running.error", actionClass, e.message)
                    )
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