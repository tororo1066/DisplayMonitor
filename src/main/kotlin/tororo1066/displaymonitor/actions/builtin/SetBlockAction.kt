package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import org.bukkit.block.data.BlockData
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

class SetBlockAction: AbstractAction() {

    var block: BlockData? = null
    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
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