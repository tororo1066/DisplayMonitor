package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import java.util.UUID

class AccessAction: AbstractAction() {

    var action = ""
    var actions: Execute = Execute.empty()

    override fun run(context: IActionContext): ActionResult {
        val uuid = ActionStorage.contextByName[action] ?: return ActionResult.failed()
        val contexts = ActionStorage.contextStorage[uuid] ?: return ActionResult.failed()
        if (contexts.isEmpty()) return ActionResult.failed()
        val firstContext = contexts.values.first()
        val newContext = context.cloneWithNewPublicContext(firstContext.publicContext).apply {
            configuration = firstContext.configuration
            setUUID(UUID.randomUUID())
            setGroupUUID(firstContext.groupUUID)
        }
        actions(newContext)
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        action = configuration.getString("action", "")!!
        actions = configuration.getConfigExecute("actions") ?: Execute.empty()
    }
}