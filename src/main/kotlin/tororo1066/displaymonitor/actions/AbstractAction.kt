package tororo1066.displaymonitor.actions

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.util.function.Consumer

abstract class AbstractAction {

    abstract fun run(context: ActionContext): ActionResult

    abstract fun prepare(section: AdvancedConfigurationSection)

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
}