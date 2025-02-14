package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Stop",
    description = "現在のセクションの実行を停止する。\nAsynchronousやRepeatなどでセクションが分岐している場合、そのセクションのみが停止される。 \n非推奨。"
)
class StopAction: AbstractAction() {

    override fun run(context: IActionContext): ActionResult {
        context.stop = true
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
    }
}