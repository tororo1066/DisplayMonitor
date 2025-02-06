package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.AsyncExecute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

// Configのみでしか効果を発揮しないのでinternal化
internal class AsynchronousAction: AbstractAction() {

    override val allowedAutoStop = false

    var actions: AsyncExecute = AsyncExecute.empty()

    override fun run(context: IActionContext): ActionResult {
        actions(context.cloneWithRandomUUID())

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        actions = section.getAsyncConfigExecute("actions") ?: actions
    }
}