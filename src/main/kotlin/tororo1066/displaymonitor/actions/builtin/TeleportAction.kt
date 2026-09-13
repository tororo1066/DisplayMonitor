package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.SuspendAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Teleport",
    description = "対象を指定した位置にテレポートする。"
)
class TeleportAction: SuspendAction() {

    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。"
    )
    var forceSync = false

    override suspend fun runSuspend(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val location = context.location ?: return ActionResult.locationRequired()

        forceSync.orBlockingTask {
            target.teleport(location)
        }
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        forceSync = configuration.getBoolean("forceSync", false)
    }
}