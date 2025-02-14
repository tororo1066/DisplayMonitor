package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "End",
    description = "全てのElementを削除し、Actionを終了する。"
)
class EndAction : AbstractAction() {

    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。",
        type = ParameterType.Boolean
    )
    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        forceSync.orBlockingTask {
            context.publicContext.elements.forEach { (_, element) ->
                element.remove()
            }
            context.publicContext.elements.clear()
        }
        context.publicContext.stop = true

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        forceSync = section.getBoolean("forceSync", false)
    }
}