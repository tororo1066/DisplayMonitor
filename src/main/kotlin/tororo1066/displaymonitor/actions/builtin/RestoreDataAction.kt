package tororo1066.displaymonitor.actions.builtin

import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File

@ClassDoc(
    name = "RestoreData",
    description = "Unsupported"
)
class RestoreDataAction: AbstractAction() {

    var storeType: StoreDataAction.StoreType = StoreDataAction.StoreType.RAW
    val paths = mutableListOf<String>()

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val configuration = context.configuration ?: return ActionResult.noParameters("Configuration not found")

        when (storeType) {
            StoreDataAction.StoreType.RAW -> {
                val data = StoreDataAction.rawData[target.uniqueId] ?: return ActionResult.noParameters("Data not found")
                paths.forEach { path ->
                    val split = path.split(".")
                    var current: HashMap<*, *> = data
                    for (i in 0 until split.size - 1) {
                        current = current[split[i]] as? HashMap<*, *> ?: return ActionResult.noParameters("Data not found")
                    }
                    val key = split.last()
                    val value = current[key] ?: return ActionResult.noParameters("Data not found")
                    configuration.parameters[key] = value
                }
            }
            StoreDataAction.StoreType.FILE -> {
                val file = File(SJavaPlugin.plugin.dataFolder, "${target.uniqueId}.yml")
                val yml = YamlConfiguration.loadConfiguration(file)
                paths.forEach { path ->
                    val value = yml.get(path) ?: return ActionResult.noParameters("Data not found")
                    configuration.parameters[path] = value
                }
            }
            StoreDataAction.StoreType.DATABASE -> {
                //Unsupported
            }
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        storeType = section.getEnum("storeType", StoreDataAction.StoreType::class.java, StoreDataAction.StoreType.RAW)!!
        val sectionPath = section.getAdvancedConfigurationSection("path")
        if (sectionPath != null) {
            fun deep(section: IAdvancedConfigurationSection, path: String) {
                section.getKeys(false).forEach { key ->
                    section.getAdvancedConfigurationSection(key)?.let {
                        deep(it, "$path.$key")
                    } ?: run {
                        val string = section.getString(key, "") ?: ""
                        if (string.isNotEmpty()) {
                            paths.add("$path.$key.$string")
                        }
                    }
                }
            }
            deep(sectionPath, "")
        } else {
            val string = section.getString("path", "") ?: ""
            if (string.isNotEmpty()) {
                paths.add(string)
            }
        }
    }
}