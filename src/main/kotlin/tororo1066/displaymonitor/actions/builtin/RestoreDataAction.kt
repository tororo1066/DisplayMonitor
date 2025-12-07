package tororo1066.displaymonitor.actions.builtin

import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.StringList
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.util.UUID

@ClassDoc(
    name = "RestoreData",
    description = "StoreDataで保存されたデータを復元する。"
)
class RestoreDataAction: AbstractAction() {

    @ParameterDoc(
        name = "storeType",
        description = "取得先の種類。\n" +
                "RAW: メモリ上に保存される。サーバーを再起動すると消える。\n" +
                "FILE: プラグインのStoreDataフォルダ内に保存される。\n" +
                "DATABASE: 未対応。",
//                "DATABASE: データベースに保存される。config/StoreData.ymlでデータベースの設定が必要。",
        default = "RAW"
    )
    var storeType: StoreDataAction.StoreType = StoreDataAction.StoreType.RAW
    @ParameterDoc(
        name = "scope",
        description = "変数のスコープ。GLOBALはグローバル変数、LOCALはローカル変数。",
        default = "LOCAL"
    )
    var scope: ModifyVariableAction.Scope = ModifyVariableAction.Scope.LOCAL
    @ParameterDoc(
        name = "keys",
        description = "復元するキーのリスト。",
        type = StringList::class
    )
    var keys = listOf<String>()
    @ParameterDoc(
        name = "uuid",
        description = "対象のUUID。指定しない場合はアクションのターゲットのUUIDになる。"
    )
    var uuid: UUID? = null

    override fun run(context: IActionContext): ActionResult {
        val uuid = this.uuid ?: context.target?.uniqueId ?: return ActionResult.targetRequired()
        val configuration = context.configuration ?: return ActionResult.noParameters("Configuration not found")

        when (storeType) {
            StoreDataAction.StoreType.RAW -> {
                val data = StoreDataAction.rawData[uuid] ?: return ActionResult.noParameters("Data not found")
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
                val file = File(SJavaPlugin.plugin.dataFolder, "StoreData/${uuid}.yml")
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
                throw NotImplementedError("Database store type is not implemented yet")
//                checkAsync("RestoreDataAction")
//                val config = Config.getConfig<StoreDataConfig>() ?: return ActionResult.noParameters("Config not found")
//                val database = config.database ?: return ActionResult.noParameters("Database not found")
//                val result = database.select(config.tableName, SDBCondition().equal("uuid", uuid.toString()))
//                if (result.isEmpty()) {
//                    return ActionResult.noParameters("Data not found")
//                }
//                val data = result[0].getNullableString("data") ?: return ActionResult.noParameters("Data not found")
//                val json = JsonConfiguration().apply {
//                    options().pathSeparator(IAdvancedConfiguration.SEPARATOR)
//                }
//                json.loadFromString(data)
//                keys.forEach { key ->
//                    val value = json.get(key) ?: return@forEach
//                    if (scope == ModifyVariableAction.Scope.GLOBAL) {
//                        context.publicContext.parameters[key] = value
//                    } else {
//                        configuration.parameters[key] = value
//                    }
//                }
            }
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        storeType = section.getEnum("storeType", StoreDataAction.StoreType::class.java, StoreDataAction.StoreType.RAW)
        scope = section.getEnum("scope", ModifyVariableAction.Scope::class.java, ModifyVariableAction.Scope.LOCAL)
        keys = section.getStringList("keys")
        val uuidString = section.getString("uuid", null)
        if (uuidString != null) {
            uuid = try {
                UUID.fromString(uuidString)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}