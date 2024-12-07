package tororo1066.displaymonitor.actions

import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.Execute

abstract class CheckAction: AbstractAction() {

    var actions: Execute = Execute.empty()
    var failActions: Execute = Execute.empty()

    abstract fun isAllowed(context: ActionContext): Boolean

    override fun run(context: ActionContext): ActionResult {
        if (isAllowed(context)) {
            actions(context)
        } else {
            failActions(context)
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        actions = section.getConfigExecute("actions") ?: actions
        failActions = section.getConfigExecute("fail") ?: failActions
    }
}