package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.AsyncExecute

// Configのみでしか効果を発揮しないのでinternal化
internal class AsynchronousAction: AbstractAction() {

    var actions: AsyncExecute = AsyncExecute.empty()

    override fun run(context: ActionContext): ActionResult {
        actions(context.cloneWithRandomUUID())

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        actions = section.getAsyncConfigExecute("actions") ?: actions
    }
}