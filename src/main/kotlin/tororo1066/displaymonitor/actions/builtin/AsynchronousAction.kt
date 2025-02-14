package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.AsyncExecute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

// Configのみでしか効果を発揮しないのでinternal化
@ClassDoc(
    name = "Asynchronous",
    description = "非同期でアクションを実行する。"
)
class AsynchronousAction: AbstractAction() {

    override val allowedAutoStop = false

    @ParameterDoc(
        name = "actions",
        description = "非同期で実行するアクションのリスト。",
        type = ParameterType.Actions
    )
    var actions: AsyncExecute = AsyncExecute.empty()

    override fun run(context: IActionContext): ActionResult {
        actions(context.cloneWithRandomUUID())

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        actions = section.getAsyncConfigExecute("actions") ?: actions
    }
}