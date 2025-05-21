package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import java.util.UUID

@ClassDoc(
    name = "AccessAction",
    description = "実行中のActionのコンテキストにアクセスする。"
)
class AccessAction: AbstractAction() {

    @ParameterDoc(
        name = "action",
        description = "アクセスするActionの名前。",
        type = ParameterType.String
    )
    var action = ""
    @ParameterDoc(
        name = "actions",
        description = "アクセスしたActionに対して実行するアクション。",
        type = ParameterType.Actions
    )
    var actions: Execute = Execute.empty()

    override fun run(context: IActionContext): ActionResult {
        val uuid = ActionStorage.contextByName[action] ?: return ActionResult.failed("Failed to find action $action")
        val contexts = ActionStorage.contextStorage[uuid] ?: return ActionResult.failed("Failed to find action $action by uuid $uuid")
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