package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Repeat",
    description = "指定した回数だけアクションを繰り返す。"
)
class RepeatAction: AbstractAction() {

    @ParameterDoc(
        name = "times",
        description = "繰り返す回数。",
        type = ParameterType.Int
    )
    var times = 1
    @ParameterDoc(
        name = "isInfinity",
        description = "無限に繰り返すか。 times が無視される。",
        type = ParameterType.Boolean
    )
    var isInfinity = false
    @ParameterDoc(
        name = "actions",
        description = "繰り返すアクションのリスト。",
        type = ParameterType.Actions
    )
    var actions: Execute = Execute.empty()
    @ParameterDoc(
        name = "variableName",
        description = "繰り返し回数を格納する変数名。指定しない場合は repeat.count に格納される。",
        type = ParameterType.String
    )
    var variableName: String? = null

    override fun run(context: IActionContext): ActionResult {
        var count = 0

        fun step(): ActionResult? {
            if (context.publicContext.stop) {
                return ActionResult.success()
            }
            val cloneContext = context.cloneWithRandomUUID()
            variableName?.let {
                cloneContext.configuration?.parameters?.put(it, count)
            } ?: run {
                cloneContext.configuration?.parameters?.put("repeat.count", count)
            }
            actions(cloneContext)
            if (cloneContext.stop) {
                return ActionResult.success()
            }
            count++
            return null
        }

        if (isInfinity) {
            while (true) {
                val result = step()
                if (result != null) {
                    return result
                }
            }
        } else {
            repeat(times) {
                val result = step()
                if (result != null) {
                    return result
                }
            }
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        val times = section.get("times")
        if (times is Int) {
            this.times = times
        } else if (times is String && times == "infinity") {
            isInfinity = true
        }
        actions = section.getConfigExecute("actions", actions)
        variableName = section.getString("variableName")
    }
}