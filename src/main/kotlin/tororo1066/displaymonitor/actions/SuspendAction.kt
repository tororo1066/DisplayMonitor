package tororo1066.displaymonitor.actions

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.tororopluginapi.SJavaPlugin

abstract class SuspendAction: AbstractAction() {
    abstract suspend fun runSuspend(context: IActionContext): ActionResult

    override fun run(context: IActionContext): ActionResult {
        throw UnsupportedOperationException("Use runSuspend instead")
    }

    protected suspend fun Boolean.orBlockingTask(run: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            run()
        } else {
            if (this) {
                withContext(SJavaPlugin.plugin.minecraftDispatcher) {
                    run()
                }
            } else {
                runTask {
                    run()
                }
            }
        }
    }
}