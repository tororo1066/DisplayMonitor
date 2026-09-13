package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "OnlinePlayers",
    description = "オンラインのプレイヤーに対してアクションを実行する。"
)
class OnlinePlayersAction: AbstractAction() {

    @ParameterDoc(
        name = "actions",
        description = "実行するアクションのリスト。"
    )
    var actions: Execute = Execute.empty()

    @ParameterDoc(
        name = "variableName",
        description = "繰り返し回数を格納する変数名。指定しない場合は`online.count`に格納される。"
    )
    var variableName = "online.count"

    override fun run(context: IActionContext): ActionResult {
        Bukkit.getOnlinePlayers().forEachIndexed { index, player ->
            if (context.publicContext.stop) {
                return ActionResult.success()
            }
            val cloneContext = context.clone()
            cloneContext.target = player
            cloneContext.configuration?.parameters?.put(variableName, index)
            actions(cloneContext)
            if (cloneContext.stop) {
                return ActionResult.success()
            }
        }
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        actions = configuration.getConfigExecute("actions") ?: Execute.empty()
        variableName = configuration.getString("variableName", "online.count") ?: variableName
    }
}