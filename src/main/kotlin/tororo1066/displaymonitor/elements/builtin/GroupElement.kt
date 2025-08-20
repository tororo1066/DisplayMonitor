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

    lateinit var centerEntity: Entity
    var entityBySpawn: Entity? = null
    lateinit var locationBySpawn: Location
    var calledPrepare = false

    override val syncGroup = true

    override fun applyChanges() {
        elements.values.forEach { it.applyChanges() }
    }

    override fun spawn(entity: Entity?, location: Location) {
        entityBySpawn = entity
        locationBySpawn = location
        centerEntity = location.world.spawn(location, Marker::class.java)
        elements.values.forEach {
            it.groupUUID = groupUUID
            it.contextUUID = contextUUID
            it.spawn(entity, location)
            it.attachEntity(centerEntity)
        }
        startTick(entity)
    }

    override fun tick(entity: Entity?) {
        if (!centerEntity.isValid) {
            remove()
            return
        }
        entityBySpawn = entity
        elements.values.forEach { it.tick(entity) }
    }

    override fun remove() {
        tickTask?.cancel()
        elements.values.forEach { it.remove() }
        centerEntity.remove()
    }

    override fun attachEntity(entity: Entity) {
        entity.addPassenger(centerEntity)
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
                    it.groupUUID = groupUUID
                    it.contextUUID = contextUUID
                    it.spawn(entityBySpawn, locationBySpawn)
                    it.attachEntity(centerEntity)
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
        centerEntity.teleport(location)
        elements.values.forEach {
            if (!it.syncGroup()) {
                it.move(location)
            }
        }
    }
}