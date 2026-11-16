package tororo1066.displaymonitor.actions.builtin

import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ObjectList
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterItemDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
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
                "FILE: プラグインのStoreDataフォルダ内に保存される。",
        default = "RAW",
        allowExpression = false,
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
                "とするとそのキーのデータを削除できる。",
        type = ObjectList::class,
        items = [
            ParameterItemDoc(name = "key", description = "保存または削除するキー。", type = String::class, required = true),
            ParameterItemDoc(name = "value", description = "保存する値。", type = Any::class),
            ParameterItemDoc(name = "remove", description = "trueの場合、キーのデータを削除する。", type = Boolean::class),
        ],
        itemRequiredAnyOf = ["value", "remove"],
    )
    var data: HashMap<String, Any?> = HashMap()

    @ParameterDoc(
        name = "uuid",
        description = "対象のUUID。指定しない場合はアクションのターゲットのUUIDになる。"
    )
    var uuid: UUID? = null

    private fun formatValue(value: Any): Any {
        return when (value) {
            is IAdvancedConfigurationSection -> {
                value.getEvaluatedValues(true)
                    .mapNotNull { (k, v) -> k to formatValue(v ?: return@mapNotNull null) }
                    .toMap()
            }
            is List<*> -> {
                value.mapNotNull { formatValue(it ?: return@mapNotNull null) }
            }
            else -> {
                value
            }
        }
    }

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
                        rawDataMap[key] = formatValue(value)
                    }
                }
                if (rawDataMap.isEmpty()) {
                    rawData.remove(uuid)
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
                FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE).use { channel ->
                    channel.lock().use {
                        val yml = YamlConfiguration().apply {
                            load(file)
                        }
                        data.forEach { (key, value) ->
                            yml.set(key, value?.let { formatValue(it) })
                        }
                        if (yml.getKeys(false).isEmpty()) {
                            file.delete()
                        } else {
                            yml.save(file)
                        }
                    }
                }
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
    }
}
