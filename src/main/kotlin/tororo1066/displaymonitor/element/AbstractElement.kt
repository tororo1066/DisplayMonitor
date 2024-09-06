package tororo1066.displaymonitor.element

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.tororopluginapi.SJavaPlugin
import java.util.UUID

abstract class AbstractElement(val config: ConfigurationSection) {

    var contextUUID: UUID? = null
    protected var tickTask: BukkitTask? = null

    protected fun getContext(): ActionContext? {
        return contextUUID?.let { ActionStorage.contextStorage[it] }
    }

    abstract fun spawn(p: Player, location: Location)

    abstract fun remove(p: Player)

    abstract fun tick(p: Player)

    abstract fun edit(p: Player, edit: ConfigurationSection)

    protected fun startTick(p: Player) {
        tickTask = Bukkit.getScheduler().runTaskTimer(SJavaPlugin.plugin, Runnable {
            tick(p)
        }, 0, 1)
    }
}