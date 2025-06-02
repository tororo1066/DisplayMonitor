package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.storage.WorkspaceStorage
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "SetWorkspace",
    description = "Workspaceを設定する。"
)
class SetWorkspaceAction: AbstractAction() {

    @ParameterDoc(
        name = "workspace",
        description = "Workspace名。"
    )
    var workspace: String = ""

    override fun run(context: IActionContext): ActionResult {
        val workspace = WorkspaceStorage.workspaces[workspace] ?: return ActionResult.noParameters("Workspace not found: $workspace")
        context.publicContext.workspace = workspace
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        workspace = configuration.getString("workspace", "")!!
    }
}