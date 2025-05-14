package tororo1066.displaymonitor.actions.builtin

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.config.Config
import tororo1066.displaymonitor.config.sub.StoreDataConfig
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBCondition
import java.io.File

@ClassDoc(
    name = "RestoreData",
    description = "Unsupported"
)
class RestoreDataAction: AbstractAction() {

    var storeType: StoreDataAction.StoreType = StoreDataAction.StoreType.RAW
    val paths = mutableListOf<String>()

    private val separator = IAdvancedConfiguration.SEPARATOR

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val configuration = context.configuration ?: return ActionResult.noParameters("Configuration not found")

        when (storeType) {
            StoreDataAction.StoreType.RAW -> {
                val data = StoreDataAction.rawData[target.uniqueId] ?: return ActionResult.noParameters("Data not found")
                paths.forEach { path ->
                    val split = path.split(separator)
                    var current: HashMap<*, *> = data
                    for (i in 0 until split.size - 1) {
                        current = current[split[i]] as? HashMap<*, *> ?: return@forEach
                    }
                    val key = split.last()
                    val value = current[key] ?: return@forEach
                    configuration.parameters[key] = value
                }
            }
            StoreDataAction.StoreType.FILE -> {
                checkAsync("RestoreDataAction")
                val file = File(SJavaPlugin.plugin.dataFolder, "StoreData/${target.uniqueId}.yml")
                if (!file.exists()) {
                    return ActionResult.noParameters("File not found")
                }
                val yml = YamlConfiguration.loadConfiguration(file)
                paths.forEach { path ->
                    val value = yml.get(path) ?: return@forEach
                    configuration.parameters[path] = value
                }
            }
            StoreDataAction.StoreType.DATABASE -> {
                checkAsync("RestoreDataAction")
                val config = Config.getConfig<StoreDataConfig>() ?: return ActionResult.noParameters("Config not found")
                val database = config.database ?: return ActionResult.noParameters("Database not found")
                val result = database.select(config.tableName, SDBCondition().equal("uuid", target.uniqueId.toString()))
                if (result.isEmpty()) {
                    return ActionResult.noParameters("Data not found")
                }
                val data = result[0].getNullableString("data") ?: return ActionResult.noParameters("Data not found")
                val json = JsonConfiguration()
                json.loadFromString(data)
                paths.forEach { path ->
                    val value = json.get(path) ?: return@forEach
                    configuration.parameters[path] = value
                }
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
                        deep(it, "${path}${separator}${key}")
                    } ?: run {
                        val string = section.getString(key, "") ?: ""
                        if (string.isNotEmpty()) {
                            paths.add("${path}${separator}${key}${separator}${string}")
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