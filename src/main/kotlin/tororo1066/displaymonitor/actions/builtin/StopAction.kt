package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.storage.ActionStorage

class StopAction: AbstractAction() {

    var forceSync = false

    override fun run(context: ActionContext): ActionResult {
        val contexts = ActionStorage.contextStorage[context.groupUUID]
        if (contexts != null) {
            forceSync.orBlockingTask {
                contexts.forEach { (_, actionContext) ->
                    actionContext.elements.forEach { (_, element) ->
                        element.remove()
                    }
                }
            }
            contexts.forEach { (_, actionContext) ->
                actionContext.stop = true
            }
        } else {
            context.elements.forEach { (_, element) ->
                element.remove()
            }
            context.stop = true
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}