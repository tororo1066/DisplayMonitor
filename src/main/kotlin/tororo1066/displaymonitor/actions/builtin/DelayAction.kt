package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Delay",
    description = "指定した時間だけ待機する。"
)
class DelayAction: AbstractAction() {

    @ParameterDoc(
        name = "delay",
        description = "待機する時間。"
    )
    var delay = 0L

    override fun run(context: IActionContext): ActionResult {
        checkAsync("DelayAction")
        Thread.sleep(delay)
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        delay = section.getLong("delay", 0L)
    }
}