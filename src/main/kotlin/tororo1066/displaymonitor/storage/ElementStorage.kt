package tororo1066.displaymonitor.storage

import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.Utils.mergeConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.elements.builtin.*
import tororo1066.displaymonitor.events.LoadPresetElementEvent
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File

object ElementStorage {
    val presetElements = mutableMapOf<String, AbstractElement>()
    val elementClasses = mutableMapOf<String, Class<out AbstractElement>>()

    init {
        elementClasses["ItemElement"] = ItemElement::class.java
        elementClasses["BlockElement"] = BlockElement::class.java
        elementClasses["TextElement"] = TextElement::class.java
        elementClasses["GroupElement"] = GroupElement::class.java

        load()
    }

    fun load() {
        presetElements.clear()
        val directory = File(SJavaPlugin.plugin.dataFolder, "elements")
        directory.mkdirs()
        loadElements(directory)
        LoadPresetElementEvent().callEvent()
    }

    fun loadElements(directory: File) {
        val context = "LoadElements(ElementStorage)"
        val files = directory.listFiles()
        if (files == null) {
            DisplayMonitor.warn(context, DisplayMonitor.translate("element.directory.not.found", directory.path))
            return
        }
        for (file in files) {
            if (file.isDirectory) {
                loadElements(file)
                continue
            }
            if (file.extension != "yml") continue
            val yaml = AdvancedConfiguration().mergeConfiguration(YamlConfiguration.loadConfiguration(file))
            yaml.getKeys(false).forEach { key ->
                val section = yaml.getAdvancedConfigurationSection(key)
                if (section == null) {
                    DisplayMonitor.warn(context, DisplayMonitor.translate("element.section.not.found", key))
                    return@forEach
                }
                loadElement(section)
            }
        }
    }

    fun loadElement(section: AdvancedConfigurationSection) {
        val context = "LoadElement(ElementStorage)"
        val type = section.getString("type")
        if (type == null) {
            DisplayMonitor.warn(context, DisplayMonitor.translate("element.type.not.found.no.name"))
            return
        }
        val clazz = elementClasses[type]
        if (clazz == null) {
            DisplayMonitor.warn(context, DisplayMonitor.translate("element.type.not.found", type))
            return
        }
        val element = clazz.getConstructor().newInstance()
        element.prepare(section)
        presetElements[section.name] = element
    }

    fun createElement(presetName: String?, clazz: String?, overrideParameters: AdvancedConfigurationSection?, context: String): AbstractElement? {
        val element: AbstractElement
        val presetElement = presetElements[presetName]
        if (presetElement != null) {
            element = presetElement.clone()
            element.prepare(overrideParameters ?: AdvancedConfiguration())
        } else {
            val elementClass = elementClasses[clazz]
            if (elementClass == null) {
                DisplayMonitor.error(context, DisplayMonitor.translate("element.type.not.found", clazz))
                return null
            }
            element = elementClass.getConstructor().newInstance()
            element.prepare(overrideParameters ?: AdvancedConfiguration())
        }

        return element
    }
}