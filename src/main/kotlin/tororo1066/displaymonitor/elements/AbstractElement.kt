package tororo1066.displaymonitor.elements

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.scheduler.BukkitTask
import tororo1066.displaymonitor.elements.SettableProcessor.getSettableFields
import tororo1066.displaymonitor.elements.SettableProcessor.processValue
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.IAbstractElement
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.SJavaPlugin
import java.lang.reflect.Field
import java.util.UUID

abstract class AbstractElement: IAbstractElement {

    abstract val syncGroup: Boolean

    private var groupUUID: UUID? = null
    private var contextUUID: UUID? = null
    protected var tickTask: BukkitTask? = null

    protected fun getContext(): IActionContext? {
        return ActionStorage.contextStorage[groupUUID]?.get(contextUUID)
    }

    protected fun runExecute(execute: Execute) {
        val context = getContext() ?: return
        execute(context)
    }

    protected fun runExecute(execute: Execute, context: IActionContext) {
        execute(context)
    }

    protected fun runExecute(execute: Execute, modification: (IActionContext) -> Unit) {
        val context = (getContext() ?: return).clone()
        modification(context)
        execute(context)
    }

    override fun getGroupUUID(): UUID? {
        return groupUUID
    }

    override fun setGroupUUID(uuid: UUID?) {
        groupUUID = uuid
    }

    override fun getContextUUID(): UUID? {
        return contextUUID
    }

    override fun setContextUUID(uuid: UUID?) {
        contextUUID = uuid
    }

    private fun Field.isNullable(): Boolean {
        return this.annotations.any { it.annotationClass.simpleName == "Nullable" }
    }

    override fun prepare(section: IAdvancedConfigurationSection) {

        fun prepareChild(section: IAdvancedConfigurationSection, field: Field, instance: Any) {
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
                val value = section.withParameters(SettableProcessor.processVariable(field.get(instance))) {
                    it.processValue(key, field.type)
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

    override fun syncGroup(): Boolean {
        return syncGroup
    }

    protected fun startTick(entity: Entity?) {
        tickTask = Bukkit.getScheduler().runTaskTimer(SJavaPlugin.plugin, Runnable {
            tick(entity)
        }, 0, 1)
    }

    override fun clone(): IAbstractElement {
        val instance = this::class.java.getDeclaredConstructor().newInstance()

        instance.groupUUID = groupUUID
        instance.contextUUID = contextUUID
        instance.tickTask = tickTask

        val settableFields = this::class.java.getSettableFields()
        settableFields.forEach { field ->
            val defaultAccessible = field.canAccess(this)
            field.isAccessible = true
            field.set(instance, field.get(this))
            field.isAccessible = defaultAccessible
        }
        return instance
    }
}