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
    var scope: ModifyVariableAction.Scope = ModifyVariableAction.Scope.LOCAL
    var keys = listOf<String>()

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val configuration = context.configuration ?: return ActionResult.noParameters("Configuration not found")

        when (storeType) {
            StoreDataAction.StoreType.RAW -> {
                val data = StoreDataAction.rawData[target.uniqueId] ?: return ActionResult.noParameters("Data not found")
                keys.forEach { key ->
                    val value = data[key] ?: return@forEach
                    if (scope == ModifyVariableAction.Scope.GLOBAL) {
                        context.publicContext.parameters[key] = value
                    } else {
                        configuration.parameters[key] = value
                    }
                }
            }
            StoreDataAction.StoreType.FILE -> {
                checkAsync("RestoreDataAction")
                val file = File(SJavaPlugin.plugin.dataFolder, "StoreData/${target.uniqueId}.yml")
                if (!file.exists()) {
                    return ActionResult.noParameters("File not found")
                }
                val yml = YamlConfiguration().apply {
                    options().pathSeparator(IAdvancedConfiguration.SEPARATOR)
                    load(file)
                }
                keys.forEach { key ->
                    val value = yml.get(key) ?: return@forEach
                    if (scope == ModifyVariableAction.Scope.GLOBAL) {
                        context.publicContext.parameters[key] = value
                    } else {
                        configuration.parameters[key] = value
                    }
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
                val json = JsonConfiguration().apply {
                    options().pathSeparator(IAdvancedConfiguration.SEPARATOR)
                }
                json.loadFromString(data)
                keys.forEach { key ->
                    val value = json.get(key) ?: return@forEach
                    if (scope == ModifyVariableAction.Scope.GLOBAL) {
                        context.publicContext.parameters[key] = value
                    } else {
                        configuration.parameters[key] = value
                    }
                }
            }
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        storeType = section.getEnum("storeType", StoreDataAction.StoreType::class.java, StoreDataAction.StoreType.RAW)
        scope = section.getEnum("scope", ModifyVariableAction.Scope::class.java, ModifyVariableAction.Scope.LOCAL)
        keys = section.getStringList("keys")
    }
}