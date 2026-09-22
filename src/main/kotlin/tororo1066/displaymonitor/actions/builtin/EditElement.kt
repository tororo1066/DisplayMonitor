package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.SuspendAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "EditElement",
    description = "Elementを編集する。"
)
class EditElement: SuspendAction() {

    @ParameterDoc(
        name = "name",
        description = "編集するElementの名前。"
    )
    var name = ""
    @ParameterDoc(
        name = "edit",
        description = "編集する内容。"
    )
    var edit: IAdvancedConfigurationSection? = null
    @ParameterDoc(
        name = "apply",
        description = "編集内容を適用するか。",
        default = "true"
    )
    var apply = true
    @ParameterDoc(
        name = "forceSync",
        description = "強制的に同期的に実行するか。",
        default = "false"
    )
    var forceSync = false

    override suspend fun runSuspend(context: IActionContext): ActionResult {
        val element = context.publicContext.elements[name] ?: return ActionResult.noParameters(DisplayMonitor.translate("action.editElement.notFound", name))
        val edit = edit
        forceSync.orBlockingTask {
            if (apply) {
                if (edit == null) {
                    element.applyChanges()
                } else {
                    element.edit(edit)
                }
            } else {
                if (edit != null) {
                    element.prepare(edit)
                }
            }
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        edit = section.getAdvancedConfigurationSection("edit")
        apply = section.getBoolean("apply", true)
        forceSync = section.getBoolean("forceSync", false)
    }
}