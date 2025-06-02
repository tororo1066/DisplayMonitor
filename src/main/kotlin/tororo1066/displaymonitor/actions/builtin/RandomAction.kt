package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Random",
    description = "重み付きでランダムにアクションを実行する。"
)
class RandomAction: AbstractAction() {

    @ParameterDoc(
        name = "actions",
        description = "実行するアクションのリスト。そのセクション内にrandom_weightを指定することで重みを指定できる。",
        type = Execute::class
    )
    var actions: List<IAdvancedConfigurationSection> = listOf()

    override fun run(context: IActionContext): ActionResult {
        if (actions.isEmpty()) {
            return ActionResult.noParameters("No actions to choose from")
        }
        val weights = actions.map { it.getDouble("random_weight", 1.0) }
        val totalWeight = weights.sum()
        val randomValue = Math.random() * totalWeight
        var cumulativeWeight = 0.0
        var action: IAdvancedConfigurationSection? = null
        for (i in actions.indices) {
            cumulativeWeight += weights[i]
            if (randomValue <= cumulativeWeight) {
                action = actions[i]
                break
            }
        }
        if (action == null) {
            return ActionResult.noParameters("No action selected")
        }

        createExecute(listOf(action)).invoke(context)
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        actions = configuration.getAdvancedConfigurationSectionList("actions")
    }
}