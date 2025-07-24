package tororo1066.displaymonitor.actions

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.parameters.ActionParameters
import tororo1066.displaymonitor.config.Config
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.actions.IActionRunner
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.util.concurrent.CompletableFuture

object ActionRunner: IActionRunner {

    private const val RUN_CONTEXT = "ActionRunner"

    override fun run(
        root: IAdvancedConfiguration,
        actionList: List<IAdvancedConfigurationSection>,
        context: IActionContext,
        actionName: String?,
        async: Boolean,
        disableAutoStop: Boolean
    ): CompletableFuture<Void> {
        if (actionList.isEmpty()) return CompletableFuture.completedFuture(null)

        if (disableAutoStop) {
            context.publicContext.shouldAutoStop = false
        }

        context.configuration = root
        root.set("temp", actionList)
        val newActionList = root.getAdvancedConfigurationSectionList("temp")

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
                            ActionStorage.contextByName.entries.removeIf { condition ->
                                condition.value == context.groupUUID
                            }
                            ActionStorage.contextStorage.remove(context.groupUUID)
                        }
                    }
                    return true
                }
                return false
            }

            for (action in newActionList) {
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
            return CompletableFuture.runAsync {
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
            return CompletableFuture.completedFuture(null)
        }
    }
}