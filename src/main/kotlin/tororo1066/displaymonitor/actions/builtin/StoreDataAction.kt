package tororo1066.displaymonitor.actions.builtin

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.config.Config
import tororo1066.displaymonitor.config.sub.StoreDataConfig
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBCondition
import java.io.File
import java.util.UUID

@ClassDoc(
    name = "StoreData",
    description = "データを保存する。",
)
class StoreDataAction: AbstractAction() {

    companion object {
        val rawData = HashMap<UUID, HashMap<String, Any>>()
    }

    @ParameterDoc(
        name = "storeType",
        description = "保存先の種類。\n" +
                "RAW: メモリ上に保存される。サーバーを再起動すると消える。\n" +
                "FILE: プラグインのStoreDataフォルダ内に保存される。\n" +
                "DATABASE: データベースに保存される。config/StoreData.ymlでデータベースの設定が必要。",
        default = "RAW"
    )
    var storeType: StoreType = StoreType.RAW
    @ParameterDoc(
        name = "data",
        description = "保存するデータのリスト。\n" +
                "- key: 保存するキー\n" +
                "  value: 保存する値\n" +
                "のように指定する。\n" +
                "- key: 保存するキー\n" +
                "  remove: true\n" +
                "とするとそのキーのデータを削除できる。"
    )
    var data: HashMap<String, Any?> = HashMap()

    @ParameterDoc(
        name = "uuid",
        description = "対象のUUID。指定しない場合はアクションのターゲットのUUIDになる。"
    )
    var uuid: UUID? = null

    override fun run(context: IActionContext): ActionResult {
        val uuid = this.uuid ?: context.target?.uniqueId ?: return ActionResult.targetRequired()
        val data = data
        when (storeType) {
            StoreType.RAW -> {
                val rawDataMap = rawData.getOrPut(uuid) { HashMap() }
                data.forEach { (key, value) ->
                    if (value == null) {
                        rawDataMap.remove(key)
                    } else {
                        rawDataMap[key] = value
                    }
                    if (rawDataMap.isEmpty()) {
                        rawData.remove(uuid)
                    }
                }
            }
            StoreType.FILE -> {
                checkAsync("StoreDataAction")
                val file = File(SJavaPlugin.plugin.dataFolder, "StoreData/${uuid}.yml")
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                val yml = YamlConfiguration().apply {
                    options().pathSeparator(IAdvancedConfiguration.SEPARATOR)
                    load(file)
                }
                data.forEach { (key, value) ->
                    yml.set(key, value)
                }
                if (yml.getKeys(false).isEmpty()) {
                    file.delete()
                } else {
                    yml.save(file)
                }
            }
            //Json
            StoreType.DATABASE -> {
                throw NotImplementedError("Database store type is not implemented yet")
//                checkAsync("StoreDataAction")
//                val config = Config.getConfig<StoreDataConfig>() ?: return ActionResult.noParameters("Config not found")
//                val database = config.database ?: return ActionResult.noParameters("Database not found")
//                val table = config.tableName
//                val result = database.select(table, SDBCondition().equal("uuid", uuid.toString()))
//                if (result.isEmpty()) {
//                    val json = JsonConfiguration().apply {
//                        options().pathSeparator(IAdvancedConfiguration.SEPARATOR)
//                    }
//                    data.forEach { (key, value) ->
//                        json[key] = value
//                    }
//                    val jsonString = json.saveToString()
//                    return if (database.insert(table, mapOf("uuid" to uuid.toString(), "data" to jsonString))) {
//                        ActionResult.success()
//                    } else {
//                        ActionResult.failed("Insert failed")
//                    }
//                } else {
//                    val json = JsonConfiguration().apply {
//                        options().pathSeparator(IAdvancedConfiguration.SEPARATOR)
//                    }
//                    val row = result[0]
//                    val jsonString = row.getString("data")
//                    json.loadFromString(jsonString)
//                    data.forEach { (key, value) ->
//                        json[key] = value
//                    }
//                    val newJsonString = json.saveToString()
//                    return if (database.update(table, mapOf("data" to newJsonString), SDBCondition().equal("uuid", uuid.toString()))) {
//                        ActionResult.success()
//                    } else {
//                        ActionResult.failed("Update failed")
//                    }
//                }
            }
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        storeType = section.getEnum<StoreType>("storeType", StoreType::class.java, StoreType.RAW)
        val dataList = section.getAdvancedConfigurationSectionList("data")
        dataList.forEach { dataSection ->
            val key = dataSection.getString("key") ?: return@forEach
            val value = dataSection.get("value")
            val remove = dataSection.getBoolean("remove", false)
            if (remove) {
                data[key] = null
            } else {
                data[key] = value ?: return@forEach
            }
        }
        val uuidString = section.getString("uuid")
        if (uuidString != null) {
            uuid = try {
                UUID.fromString(uuidString)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    enum class StoreType {
        RAW,
        FILE,
        DATABASE,
    }
}