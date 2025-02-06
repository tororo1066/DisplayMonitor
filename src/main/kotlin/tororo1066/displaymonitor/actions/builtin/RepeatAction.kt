package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

class RepeatAction: AbstractAction() {

    var times = 1
    var isInfinity = false
    var actions: Execute = Execute.empty()
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