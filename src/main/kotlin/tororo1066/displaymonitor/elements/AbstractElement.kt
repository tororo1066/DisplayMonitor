package tororo1066.displaymonitor.elements

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.scheduler.BukkitTask
import tororo1066.displaymonitor.elements.SettableProcessor.getSettableFields
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.elements.IAbstractElement
import tororo1066.tororopluginapi.SJavaPlugin
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.kotlinProperty

abstract class AbstractElement: IAbstractElement {

    abstract val syncGroup: Boolean

    private var actionContext: IActionContext? = null
    protected var tickTask: BukkitTask? = null

    protected fun runExecute(execute: Execute?) {
        execute ?: return
        val context = actionContext?.clone() ?: return
        execute(context)
    }

    protected fun runExecute(execute: Execute?, modification: (IActionContext) -> Unit) {
        execute ?: return
        val context = actionContext?.clone() ?: return
        modification(context)
        execute(context)
    }

    override fun getActionContext(): IActionContext? {
        return actionContext
    }

    override fun setActionContext(context: IActionContext?) {
        actionContext = context
    }

    override fun syncGroup(): Boolean {
        return syncGroup
    }

    override fun startTick(entity: Entity?) {
        tickTask = Bukkit.getScheduler().runTaskTimer(SJavaPlugin.plugin, Runnable {
            tick(entity)
        }, 0, 1)
    }

    override fun stopTick() {
        tickTask?.cancel()
    }

    override fun clone(): IAbstractElement {
        val instance = this::class.java.getDeclaredConstructor().newInstance()

        instance.actionContext = actionContext

        val settableFields = this::class.java.getSettableFields()
        settableFields.forEach { field ->
            val kotlinProperty = field.kotlinProperty as? KMutableProperty<*> ?: return@forEach
            kotlinProperty.setter.call(instance, kotlinProperty.getter.call(this))
        }
        return instance
    }
}