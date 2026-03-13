package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Marker
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.StringList
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.storage.ElementStorage
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.IAbstractElement
import java.lang.ref.WeakReference

@ClassDoc(
    name = "GroupElement",
    description = "Elementのグループを管理する。\nElementの同時編集ができ、位置が同期される。"
)
open class GroupElement: AbstractElement() {

    @ParameterDoc(
        name = "elements",
        description = "新規に追加するElement。",
        type = IAdvancedConfigurationSection::class,
    )
    var elements = mutableMapOf<String, IAbstractElement>()

    @Suppress("UNUSED")
    @ParameterDoc(
        name = "edit",
        description = "Elementの編集内容。",
        type = IAdvancedConfigurationSection::class
    )
    val edit = null

    @Suppress("UNUSED")
    @ParameterDoc(
        name = "remove",
        description = "削除するElementの名前。",
        type = StringList::class
    )
    val remove = null

    private var centerEntityRef: WeakReference<Entity>? = null
    private var entityBySpawnRef: WeakReference<Entity>? = null
    private var locationBySpawn: Location? = null
    var calledPrepare = false

    override val syncGroup = true

    private fun getCenterEntityOrNull(): Entity? {
        val entity = centerEntityRef?.get() ?: return null
        return if (entity.isValid) entity else null
    }

    private fun getSpawnEntityOrNull(): Entity? {
        val entity = entityBySpawnRef?.get() ?: return null
        return if (entity.isValid) entity else null
    }

    override fun applyChanges() {
        elements.values.forEach { it.applyChanges() }
    }

    override fun spawn(entity: Entity?, location: Location) {
        entityBySpawnRef = entity?.let { WeakReference(it) }
        locationBySpawn = location

        val center = location.world.spawn(location, Marker::class.java)
        centerEntityRef = WeakReference(center)
        elements.values.forEach {
            it.actionContext = actionContext
            it.spawn(entity, location)
            it.attachEntity(center)
        }
        startTick(entity)
    }

    override fun tick(entity: Entity?) {
        val center = getCenterEntityOrNull()
        if (center == null) {
            remove()
            return
        }

        entityBySpawnRef = entity?.let { WeakReference(it) }
        elements.values.forEach { it.tick(entity) }
    }

    override fun remove() {
        stopTick()
        elements.values.forEach { it.remove() }
        getCenterEntityOrNull()?.remove()
        centerEntityRef?.clear()
        entityBySpawnRef?.clear()
    }

    override fun attachEntity(entity: Entity) {
        val center = getCenterEntityOrNull() ?: return
        entity.addPassenger(center)
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        if (section.getBoolean("clear", false)) {
            elements.values.forEach { it.remove() }
            elements.clear()
        }
        val elements = section.getAdvancedConfigurationSection("elements")
        elements?.getKeys(false)?.forEach { key ->
            val element = elements.getAdvancedConfigurationSection(key) ?: return@forEach
            val evalKey = element.getString("key", key)!!
            val presetName = element.getString("preset")
            val clazz = element.getString("type")
            val overrideParameters = element.getAdvancedConfigurationSection("parameters")
            ElementStorage.createElement(presetName, clazz, overrideParameters, "GroupElement")?.let {
                if (this.elements.containsKey(evalKey)) {
                    this.elements[evalKey]?.remove()
                }
                this.elements[evalKey] = it
                if (calledPrepare) {
                    it.actionContext = actionContext
                    val spawnLocation = locationBySpawn
                    val center = getCenterEntityOrNull()
                    if (spawnLocation != null && center != null) {
                        it.spawn(getSpawnEntityOrNull(), spawnLocation)
                        it.attachEntity(center)
                    }
                }
            }
        }

        val edit = section.getAdvancedConfigurationSection("edit")
        edit?.getKeys(false)?.forEach { key ->
            val evalKey = edit.getString("key", key)!!
            val split = evalKey.split(",")
            val editConfig = edit.getAdvancedConfigurationSection(key) ?: return@forEach
            split.forEach second@ { name ->
                val element = this.elements[name] ?: return@second
                element.edit(editConfig)
            }
        }

        val remove = section.getStringList("remove")
        remove.forEach {
            this.elements[it]?.remove()
            this.elements.remove(it)
        }

        calledPrepare = true
    }

    override fun move(location: Location) {
        locationBySpawn = location
        getCenterEntityOrNull()?.teleport(location)
        elements.values.forEach {
            if (!it.syncGroup()) {
                it.move(location)
            }
        }
    }

    override fun hasChildren(): Boolean {
        return elements.isNotEmpty()
    }

    override fun getChildren(): Map<String, IAbstractElement> {
        return elements
    }
}