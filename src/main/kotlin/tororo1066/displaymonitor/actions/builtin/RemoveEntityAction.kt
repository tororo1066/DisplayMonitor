package tororo1066.displaymonitor.actions.builtin

import org.bukkit.entity.Player
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class RemoveEntityAction: AbstractAction() {

    var forceSync = false

    override fun run(context: ActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        if (target is Player) return ActionResult.failed("Player cannot be removed")
        forceSync.orBlockingTask {
            target.remove()
        }
        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}