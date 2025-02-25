package tororo1066.displaymonitor.actions

import org.bukkit.entity.Player
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.parameters.ActionParameters
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.actions.IActionRunner
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import java.util.concurrent.CompletableFuture

object ActionRunner: IActionRunner {

    private const val runContext = "ActionRunner"

    fun run(config: AdvancedConfiguration, p: Player) {
        val configActions = config.getAdvancedConfigurationSectionList("actions")
        run(config, configActions, ActionContext(PublicActionContext(), p), null, async = false, disableAutoStop = false)
    }

    override fun run(
        root: IAdvancedConfiguration,
        actionList: List<IAdvancedConfigurationSection>,
        context: IActionContext,
        actionName: String?,
        async: Boolean,
        disableAutoStop: Boolean
    ) {
        if (actionList.isEmpty()) return

        if (disableAutoStop) {
            context.publicContext.shouldAutoStop = false
        }

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

        if (actionName != null) {
            ActionStorage.contextByName[actionName] = context.groupUUID
        }

        fun invokeActions() {
            for (action in actionList) {
                if (context.publicContext.stop) {
                    ActionStorage.contextStorage.remove(context.groupUUID)
                    break
                }
                if (context.stop) {
                    ActionStorage.contextStorage[context.groupUUID]?.let {
                        it.remove(context.uuid)
                        if (it.isEmpty()) {
                            ActionStorage.contextStorage.remove(context.groupUUID)
                        }
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

                if (!actionInstance.allowedAutoStop()) {
                    context.publicContext.shouldAutoStop = false
                }

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

            if (context.publicContext.shouldAutoStop) {
                ActionStorage.contextStorage.remove(context.groupUUID)
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