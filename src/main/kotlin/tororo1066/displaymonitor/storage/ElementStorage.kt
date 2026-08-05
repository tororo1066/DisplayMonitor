package tororo1066.displaymonitor.storage

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.elements.builtin.*
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.IAbstractElement
import tororo1066.displaymonitorapi.events.ElementRegisteringEvent
import tororo1066.displaymonitorapi.storage.IElementStorage
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File

object ElementStorage: IElementStorage {
    val presetElements = mutableMapOf<String, IAdvancedConfigurationSection>()
    val elementClasses = mutableMapOf<String, Class<out IAbstractElement>>()

    init {
        elementClasses["ItemElement"] = ItemElement::class.java
        elementClasses["BlockElement"] = BlockElement::class.java
        elementClasses["TextElement"] = TextElement::class.java
        elementClasses["GroupElement"] = GroupElement::class.java
        elementClasses["CollidableElement"] = CollidableElement::class.java

        load()
    }

    fun load() {
        presetElements.clear()
        val directory = File(SJavaPlugin.plugin.dataFolder, "elements")
        directory.mkdirs()
        loadElements(directory)
        ElementRegisteringEvent().callEvent()
    }

    override fun loadElements(directory: File) {
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
            val advancedConfiguration = AdvancedConfiguration.load(file)
            advancedConfiguration.getKeys(false).forEach { key ->
                val section = advancedConfiguration.getAdvancedConfigurationSection(key)
                if (section == null) {
                    DisplayMonitor.warn(context, DisplayMonitor.translate("element.section.not.found", key))
                    return@forEach
                }
                loadElement(section)
            }
        }
    }

    override fun loadElement(section: IAdvancedConfigurationSection) {
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
        presetElements[section.name] = section
    }

    override fun createElement(presetName: String?, clazz: String?, overrideParameters: IAdvancedConfigurationSection?, context: String): IAbstractElement? {
        return createElement(presetName, clazz, overrideParameters, emptyMap(), context)
    }

    override fun createElement(
        presetName: String?,
        clazz: String?,
        overrideParameters: IAdvancedConfigurationSection?,
        variables: Map<String, Any>,
        context: String
    ): IAbstractElement? {
        val presetElement = presetElements[presetName]
        if (presetElement != null) {
            return presetElement.withParameters(variables) { section ->
                val type = section.getString("type")
                val elementClass = elementClasses[type]
                if (elementClass == null) {
                    DisplayMonitor.error(context, DisplayMonitor.translate("element.type.not.found", type))
                    return@withParameters null
                }

                elementClass.getConstructor().newInstance().apply {
                    prepare(section)
                    prepare(overrideParameters ?: AdvancedConfiguration())
                }
            }
        }

        val elementClass = elementClasses[clazz]
        if (elementClass == null) {
            DisplayMonitor.error(context, DisplayMonitor.translate("element.type.not.found", clazz))
            return null
        }

        return elementClass.getConstructor().newInstance().apply {
            prepare(overrideParameters ?: AdvancedConfiguration())
        }
    }

    override fun registerElement(key: String, element: Class<out IAbstractElement>) {
        elementClasses[key] = element
    }
}
