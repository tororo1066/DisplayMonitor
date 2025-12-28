package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import org.bukkit.block.data.BlockData
import tororo1066.displaymonitor.actions.SuspendAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

@ClassDoc(
    name = "SetBlock",
    description = "指定した位置にブロックを設置する。"
)
class SetBlockAction: SuspendAction() {

    @ParameterDoc(
        name = "block",
        description = "設置するブロック。"
    )
    var block: BlockData? = null
    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。"
    )
    var forceSync = false

    override suspend fun runSuspend(context: IActionContext): ActionResult {
        val block = block ?: return ActionResult.noParameters("Block not found")
        val location = context.location ?: return ActionResult.locationRequired()
        forceSync.orBlockingTask {
            location.block.blockData = block
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        block = UsefulUtility.sTry( { Bukkit.createBlockData(section.getString("block") ?: "") }, { null })
        forceSync = section.getBoolean("forceSync", false)
    }
}