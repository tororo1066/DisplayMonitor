package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.SuspendAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "AttachEntity",
    description = "targetに対して指定したElementをアタッチする。"
)
class AttachEntityAction: SuspendAction() {

    @ParameterDoc(
        name = "name",
        description = "アタッチするElementの名前。",
        default = ""
    )
    var name: String = ""
    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。",
        default = "false"
    )
    var forceSync: Boolean = false

    override suspend fun runSuspend(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val element = context.publicContext.elements[name] ?: return ActionResult.noParameters("Element not found: $name")
        forceSync.orBlockingTask {
            element.attachEntity(target)
        }
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        name = configuration.getString("name", name) ?: name
        forceSync = configuration.getBoolean("forceSync", forceSync)
    }
}