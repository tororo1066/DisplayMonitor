package tororo1066.displaymonitor.actions.builtin

import kotlinx.coroutines.delay
import tororo1066.displaymonitor.actions.SuspendAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Delay",
    description = "指定した時間だけ待機する。"
)
class DelayAction: SuspendAction() {

    @ParameterDoc(
        name = "delay",
        description = "待機する時間。(ミリ秒)",
        default = "0"
    )
    var delay = 0L

    override suspend fun runSuspend(context: IActionContext): ActionResult {
        checkAsync("DelayAction")
        delay(delay)
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        delay = section.getLong("delay", 0L)
    }
}