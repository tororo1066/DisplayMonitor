package tororo1066.displaymonitor.elements

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.SettableProcessor.getSettableFields
import tororo1066.displaymonitor.elements.SettableProcessor.processValue
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.tororopluginapi.SJavaPlugin
import java.lang.reflect.Field
import java.util.UUID

abstract class AbstractElement: Cloneable {

    var groupUUID: UUID? = null
    var contextUUID: UUID? = null
    protected var tickTask: BukkitTask? = null

    protected fun getContext(): ActionContext? {
        return ActionStorage.contextStorage[groupUUID]?.get(contextUUID)
    }

    protected fun runExecute(execute: Execute) {
        val context = getContext() ?: return
        execute(context)
    }

    abstract fun spawn(p: Player, location: Location)

    abstract fun remove()

    abstract fun tick(p: Player)

    private fun Field.isNullable(): Boolean {
        return this.annotations.any { it.annotationClass.simpleName == "Nullable" }
    }

    open fun prepare(section: AdvancedConfigurationSection) {

        fun prepareChild(section: AdvancedConfigurationSection, field: Field, instance: Any) {
            val defaultAccessible = field.canAccess(instance)
            field.isAccessible = true
            val annotation = field.getAnnotation(Settable::class.java) ?: return
            if (annotation.childOnly) {
                val key = annotation.name.ifEmpty { field.name }
                val newSection = section.getAdvancedConfigurationSection(key) ?: return
                val newInstance = field.get(instance)
                val newFields = newInstance.javaClass.getSettableFields()
                newFields.forEach { newField ->
                    prepareChild(newSection, newField, newInstance)
                }
            } else {
                val key = annotation.name.ifEmpty { field.name }
                val value = section.withParameters(SettableProcessor.processVariables(field.get(instance))) {
                    this.processValue(key, field.type)
                }
                if (value != null || field.isNullable()) {
                    field.set(instance, value)
                }
            }
            field.isAccessible = defaultAccessible
        }


        val settableFields = this::class.java.getSettableFields()
        settableFields.forEach { field ->
            prepareChild(section, field, this)
        }

    }

    abstract fun applyChanges()

    fun edit(edit: AdvancedConfigurationSection) {
        prepare(edit)
        applyChanges()
    }

    protected fun startTick(p: Player) {
        tickTask = Bukkit.getScheduler().runTaskTimer(SJavaPlugin.plugin, Runnable {
            tick(p)
        }, 0, 1)
    }



    public override fun clone(): AbstractElement {
        return super.clone() as AbstractElement
    }
}