package tororo1066.displaymonitor.actions

import org.bukkit.Bukkit
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitorapi.actions.IAbstractAction
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.util.function.Consumer

abstract class AbstractAction: IAbstractAction {

    open val allowedAutoStop = true

    override fun allowedAutoStop(): Boolean {
        return allowedAutoStop
    }

    protected fun threadBlockingRunTask(run: () -> Unit) {
        var lock = true
        Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Consumer {
            run()
            lock = false
        })
        while (lock) {
            Thread.sleep(1)
        }
    }

    protected fun runTask(run: () -> Unit) {
        Bukkit.getScheduler().runTask(SJavaPlugin.plugin, run)
    }

    protected fun Boolean.orBlockingTask(run: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            run()
            return
        }
        if (this) {
            threadBlockingRunTask(run)
        } else {
            runTask(run)
        }
    }

    protected fun createExecute(actions: List<IAdvancedConfigurationSection>, isAsync: Boolean = false): Execute {
        return Execute { context ->
            if (actions.isEmpty()) return@Execute
            ActionRunner.run(
                actions.first().root as IAdvancedConfiguration,
                actions,
                context,
                actionName = null,
                async = isAsync,
                disableAutoStop = false
            )
        }
    }

    protected fun checkAsync(actionName: String) {
        if (Bukkit.isPrimaryThread()) {
            throw IllegalStateException(DisplayMonitor.translate("action.must.async", actionName))
        }
    }
}