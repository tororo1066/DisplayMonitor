package tororo1066.displaymonitor.actions

import org.bukkit.entity.Player
import tororo1066.displaymonitor.config.Config
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

    private const val RUN_CONTEXT = "ActionRunner"

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

            fun checkStop(): Boolean {
                if (context.publicContext.stop) {
                    ActionStorage.contextByName.entries.removeIf {
                        it.value == context.groupUUID
                    }
                    ActionStorage.contextStorage.remove(context.groupUUID)
                    return true
                }
                if (context.stop) {
                    ActionStorage.contextStorage[context.groupUUID]?.let {
                        it.remove(context.uuid)
                        if (it.isEmpty()) {
                            ActionStorage.contextByName.entries.removeIf {
                                it.value == context.groupUUID
                            }
                            ActionStorage.contextStorage.remove(context.groupUUID)
                        }
                    }
                    return true
                }
                return false
            }

            for (action in actionList) {
                if (checkStop()) break
                val actionClass = action.getString("class")
                val actionData = ActionStorage.actions[actionClass]
                if (actionData == null) {
                    DisplayMonitor.warn(RUN_CONTEXT, DisplayMonitor.translate("action.class.not.found", actionClass))
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
                        RUN_CONTEXT,
                        DisplayMonitor.translate("action.prepare.error", actionClass, e.message)
                    )
                    e.printStackTrace()
                    continue
                }
                try {
                    val result = actionInstance.run(context)
                    if (Config.debug) {
                        DisplayMonitor.log(
                            RUN_CONTEXT,
                            "$actionClass ${result.resultType.name}: ${result.message}"
                        )
                    }
                } catch (e: Exception) {
                    DisplayMonitor.error(
                        RUN_CONTEXT,
                        DisplayMonitor.translate("action.running.error", actionClass, e.message)
                    )
                    e.printStackTrace()
                }

                if (checkStop()) break
            }

            if (context.publicContext.shouldAutoStop) {
                ActionStorage.contextByName.entries.removeIf {
                    it.value == context.groupUUID
                }
                ActionStorage.contextStorage.remove(context.groupUUID)
            }
        }

        if (async) {
            CompletableFuture.runAsync {
                invokeActions()
            }.exceptionally {
                DisplayMonitor.error(RUN_CONTEXT, DisplayMonitor.translate("action.unknown.error", it.message))
                it.printStackTrace()
                null
            }
        } else {
            try {
                invokeActions()
            } catch (e: Exception) {
                DisplayMonitor.error(RUN_CONTEXT, DisplayMonitor.translate("action.unknown.error", e.message))
                e.printStackTrace()
            }
        }
    }
}