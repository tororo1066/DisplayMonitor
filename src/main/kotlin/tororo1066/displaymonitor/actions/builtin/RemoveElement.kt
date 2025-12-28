package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.SuspendAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "RemoveElement",
    description = "Elementを削除する。"
)
class RemoveElement: SuspendAction() {

    @ParameterDoc(
        name = "name",
        description = "削除するElementの名前。"
    )
    var name = ""
    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。"
    )
    var forceSync = false

    override suspend fun runSuspend(context: IActionContext): ActionResult {
        val element = context.publicContext.elements[name] ?: return ActionResult.noParameters(DisplayMonitor.translate("action.removeElement.notFound", name))

        forceSync.orBlockingTask {
            element.remove()
            context.publicContext.elements.remove(name)
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        forceSync = section.getBoolean("forceSync", false)
    }
}