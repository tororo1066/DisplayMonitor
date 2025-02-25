package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Marker
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.storage.ElementStorage
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.IAbstractElement

@ClassDoc(
    name = "GroupElement",
    description = "Elementのグループを管理する。\nElementの同時編集ができ、位置が同期される。"
)
class GroupElement: AbstractElement() {

    @ParameterDoc(
        name = "elements",
        description = "グループに含まれるElement。",
        type = ParameterType.AdvancedConfigurationSection
    )
    var elements = mutableMapOf<String, IAbstractElement>()

    @Suppress("UNUSED")
    @ParameterDoc(
        name = "edit",
        description = "Elementの編集内容。",
        type = ParameterType.AdvancedConfigurationSection
    )
    val edit = null

    lateinit var centerEntity: Entity

    override val syncGroup = true

    override fun applyChanges() {
        elements.values.forEach { it.applyChanges() }
    }

    override fun spawn(entity: Entity?, location: Location) {
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
            elements.clear()
        }
        val elements = section.getAdvancedConfigurationSection("elements")
        elements?.getKeys(false)?.forEach { key ->
            val element = elements.getAdvancedConfigurationSection(key) ?: return@forEach
            val presetName = element.getString("preset")
            val clazz = element.getString("type")
            val overrideParameters = element.getAdvancedConfigurationSection("parameters")
            ElementStorage.createElement(presetName, clazz, overrideParameters, "GroupElement")?.let {
                this.elements[key] = it
            }
        }

        val edit = section.getAdvancedConfigurationSection("edit")
        edit?.getKeys(false)?.forEach { key ->
            val split = key.split(",")
            val editConfig = edit.getAdvancedConfigurationSection(key) ?: return@forEach
            split.forEach second@ { name ->
                val element = this.elements[name] ?: return@second
                element.edit(editConfig)
            }
        }
    }

    override fun move(location: Location) {
        centerEntity.teleport(location)
        elements.values.forEach {
            if (!it.syncGroup()) {
                it.move(location)
            }
        }
    }
}