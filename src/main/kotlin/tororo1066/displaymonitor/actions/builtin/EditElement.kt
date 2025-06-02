package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "EditElement",
    description = "Elementを編集する。"
)
class EditElement: AbstractAction() {

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
        name = "forceSync",
        description = "強制的に同期的に実行するか。"
    )
    var forceSync = false

    override fun run(context: IActionContext): ActionResult {
        val element = context.publicContext.elements[name] ?: return ActionResult.noParameters(DisplayMonitor.translate("action.editElement.notFound", name))
        forceSync.orBlockingTask {
            element.edit(edit ?: AdvancedConfiguration())
        }
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        name = section.getString("name", "")!!
        edit = section.getAdvancedConfigurationSection("edit")
        forceSync = section.getBoolean("forceSync", false)
    }
}