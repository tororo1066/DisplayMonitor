package tororo1066.displaymonitor.actions.builtin

import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.util.UUID

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
                val file = File(SJavaPlugin.plugin.dataFolder, "${target.uniqueId}.yml")
                val yml = YamlConfiguration.loadConfiguration(file)
                data.getValues(true).forEach { (key, value) ->
                    yml.set(key, value)
                }
                yml.save(file)
            }
            StoreType.DATABASE -> {
                //Unsupported
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