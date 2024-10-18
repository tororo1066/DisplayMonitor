package tororo1066.displaymonitor.actions

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.util.function.Consumer

abstract class AbstractAction {

    abstract fun run(context: ActionContext)

    abstract fun prepare(section: AdvancedConfigurationSection)

    protected fun threadBlockingRunTask(run: (BukkitTask) -> Unit) {
        var lock = true
        Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Consumer {
            run(it)
            lock = false
        })
        while (lock) {
            Thread.sleep(1)
        }
    }

    protected fun runTask(run: (BukkitTask) -> Unit) {
        Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Consumer {
            run(it)
        })
    }

    protected fun Boolean.orBlockingTask(run: (BukkitTask) -> Unit) {
        if (this) {
            threadBlockingRunTask(run)
        } else {
            runTask(run)
        }
    }
}