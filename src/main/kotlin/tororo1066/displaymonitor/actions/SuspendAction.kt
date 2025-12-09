package tororo1066.displaymonitor.actions

import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext

abstract class SuspendAction: AbstractAction() {
    abstract suspend fun runSuspend(context: IActionContext): ActionResult

    override fun run(context: IActionContext): ActionResult {
        throw UnsupportedOperationException("Use runSuspend instead")
    }
}