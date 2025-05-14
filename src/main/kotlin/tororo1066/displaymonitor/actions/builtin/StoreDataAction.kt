package tororo1066.displaymonitor.actions.builtin

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.config.Config
import tororo1066.displaymonitor.config.sub.StoreDataConfig
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBCondition
import java.io.File
import java.util.UUID

@ClassDoc(
    name = "StoreData",
    description = "Unsupported"
)
class StoreDataAction: AbstractAction() {

    companion object {
        val rawData = HashMap<UUID, HashMap<String, Any>>()
    }

    var storeType: StoreType = StoreType.RAW
    var data: IAdvancedConfigurationSection? = null

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val data = data ?: return ActionResult.noParameters("")
        when (storeType) {
            StoreType.RAW -> {
                rawData.computeIfAbsent(target.uniqueId) { HashMap() }.putAll(data.getValues(true))
            }
            StoreType.FILE -> {
                checkAsync("StoreDataAction")
                val file = File(SJavaPlugin.plugin.dataFolder, "StoreData/${target.uniqueId}.yml")
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                val yml = YamlConfiguration.loadConfiguration(file)
                data.getValues(true).forEach { (key, value) ->
                    yml.set(key, value)
                }
                yml.save(file)
            }
            //Json
            StoreType.DATABASE -> {
                checkAsync("StoreDataAction")
                val config = Config.getConfig<StoreDataConfig>() ?: return ActionResult.noParameters("Config not found")
                val database = config.database ?: return ActionResult.noParameters("Database not found")
                val table = config.tableName
                val result = database.select(table, SDBCondition().equal("uuid", target.uniqueId.toString()))
                if (result.isEmpty()) {
                    val json = JsonConfiguration()
                    data.getValues(true).forEach { (key, value) ->
                        json.set(key, value)
                    }
                    val jsonString = json.saveToString()
                    return if (database.insert(table, mapOf("data" to jsonString))) {
                        ActionResult.success()
                    } else {
                        ActionResult.failed("Insert failed")
                    }
                } else {
                    val json = JsonConfiguration()
                    val row = result[0]
                    val jsonString = row.getString("data")
                    json.loadFromString(jsonString)
                    data.getValues(true).forEach { (key, value) ->
                        json.set(key, value)
                    }
                    val newJsonString = json.saveToString()
                    return if (database.update(table, mapOf("data" to newJsonString), SDBCondition().equal("uuid", target.uniqueId.toString()))) {
                        ActionResult.success()
                    } else {
                        ActionResult.failed("Update failed")
                    }
                }
            }
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        storeType = section.getEnum<StoreType>("storeType", StoreType::class.java, StoreType.RAW)!!
        data = section.getAdvancedConfigurationSection("data")
    }

    enum class StoreType {
        RAW,
        FILE,
        DATABASE,
    }
}