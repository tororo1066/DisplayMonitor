package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.Execute

class RepeatAction: AbstractAction() {

    var times = 1
    var isInfinity = false
    var actions: Execute = Execute.empty()

    override fun run(context: ActionContext): ActionResult {
        val cloneContext = context.cloneWithRandomUUID()
        if (isInfinity) {
            while (true) {
                if (context.stop) {
                    break
                }
                actions(cloneContext)
            }
        } else {
            repeat(times) {
                if (context.stop) {
                    return ActionResult.success()
                }
                actions(cloneContext)
            }
        }

        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        val times = section.get("times")
        if (times is Int) {
            this.times = times
        } else if (times is String && times == "infinity") {
            isInfinity = true
        }
        actions = section.getConfigExecute("actions", actions)!!
    }
}