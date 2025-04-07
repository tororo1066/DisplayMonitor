package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Package",
    description = "複数のアクションを一つのアクションに纏めて実行する。",
)
class PackageAction: AbstractAction() {

    @ParameterDoc(
        name = "actions",
        description = "実行するアクション。",
        type = ParameterType.Actions,
    )
    var actions: Execute = Execute.empty()

    override fun run(context: IActionContext): ActionResult {
        actions(context)
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        actions = section.getConfigExecute("actions", actions)
    }
}