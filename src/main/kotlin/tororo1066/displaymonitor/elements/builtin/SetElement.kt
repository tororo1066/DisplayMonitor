package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.storage.ElementStorage

class SetElement: AbstractElement() {

    var elements = mutableMapOf<String, AbstractElement>()

    override fun applyChanges() {
        elements.values.forEach { it.applyChanges() }
    }

    override fun spawn(p: Player, location: Location) {
        elements.values.forEach { it.spawn(p, location) }
    }

    override fun tick(p: Player) {
        elements.values.forEach { it.tick(p) }
    }

    override fun remove(p: Player) {
        elements.values.forEach { it.remove(p) }
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        if (section.getBoolean("clear", false)) {
            elements.clear()
        }
        val elements = section.getAdvancedConfigurationSection("elements")
        elements?.getKeys(false)?.forEach { key ->
            val element = elements.getAdvancedConfigurationSection(key) ?: return@forEach
            val presetName = element.getString("preset")
            val clazz = element.getString("type")
            val overrideParameters = element.getAdvancedConfigurationSection("parameters")
            ElementStorage.createElement(presetName, clazz, overrideParameters, "SetElement")?.let {
                this.elements[key] = it
            }
        }

        val edit = section.getAdvancedConfigurationSection("edit")
        edit?.getKeys(false)?.forEach { key ->
            val split = key.split(",")
            val editConfig = edit.getAdvancedConfigurationSection(key) ?: return@forEach
            split.forEach second@ { name ->
                val element = this.elements[name] ?: return@second
                element.prepare(editConfig)
                element.applyChanges()
            }
        }
    }

    override fun clone(): AbstractElement {
        val element = super.clone() as SetElement
        element.elements = elements.mapValues { it.value.clone() }.toMutableMap()
        return element
    }
}