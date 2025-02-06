package tororo1066.displaymonitor.actions

import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

abstract class CheckAction: AbstractAction() {

    var actions: Execute = Execute.empty()
    var failActions: Execute = Execute.empty()

    abstract fun isAllowed(context: IActionContext): Boolean

    override fun run(context: IActionContext): ActionResult {
        if (isAllowed(context)) {
            actions(context)
        } else {
            failActions(context)
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        actions = section.getConfigExecute("then") ?: actions
        failActions = section.getConfigExecute("else") ?: failActions
    }
}