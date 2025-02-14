package tororo1066.displaymonitor.actions.builtin

import org.bukkit.entity.Player
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "RemoveEntity",
    description = "Entityを削除する。"
)
class RemoveEntityAction: AbstractAction() {

    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。",
        type = ParameterType.Boolean
    )
    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        if (target is Player) return ActionResult.failed("Player cannot be removed")
        forceSync.orBlockingTask {
            target.remove()
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}