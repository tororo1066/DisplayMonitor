package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "UpdateParameters",
    description = "デフォルトのパラメータを更新する。"
)
class UpdateParametersAction: AbstractAction() {

    override fun run(context: IActionContext): ActionResult {
        context.configuration?.parameters?.putAll(context.getDefaultParameters())
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
    }
}