package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
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
        description = "繰り返す回数。\n`infinity` で無限に繰り返す。"
    )
    var times = 1
    var isInfinity = false
    @ParameterDoc(
        name = "actions",
        description = "繰り返すアクションのリスト。"
    )
    var actions: Execute = Execute.empty()
    @ParameterDoc(
        name = "variableName",
        description = "繰り返し回数を格納する変数名。指定しない場合は`repeat.count`に格納される。"
    )
    var variableName: String = "repeat.count"

    @ParameterDoc(
        name = "iterable",
        description = "繰り返し処理を行うリスト、またはその変数名。指定した場合、`times`は無視される。"
    )
    var iterable: Iterable<*>? = null
    var iterableVariable: String? = null

    @ParameterDoc(
        name = "iterableVariableName",
        description = "`iterable`で指定したリストの各要素を格納する変数名。指定しない場合は`repeat.item`に格納される。"
    )
    var iterableVariableName: String = "repeat.item"

    override fun run(context: IActionContext): ActionResult {
        var count = 0

        fun step(item: Any? = null): ActionResult? {
            if (context.publicContext.stop) {
                return ActionResult.success()
            }
            val cloneContext = context.cloneWithRandomUUID()
            cloneContext.configuration?.parameters?.put(variableName, count)
            if (item != null) {
                cloneContext.configuration?.parameters?.put(iterableVariableName, item)
            }
            actions(cloneContext)
            if (cloneContext.stop) {
                return ActionResult.success()
            }
            if (count != Int.MAX_VALUE) {
                count++
            }
            return null
        }

        iterableVariable?.let {
            val obj = context.allParameters[it]
            if (obj is Iterable<*>) {
                iterable = obj
            }
        }

        iterable?.let {
            for (item in it) {
                val result = step(item)
                if (result != null) {
                    return result
                }
            }
        } ?: run {
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
        }

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        val times = section.get("times")
        if (times is Number) {
            this.times = times.toInt()
        } else if (times is String && times == "infinity") {
            isInfinity = true
        }
        actions = section.getConfigExecute("actions", actions)
        variableName = section.getString("variableName", variableName)!!

        val iterable = section.get("iterable")
        if (iterable is Iterable<*>) {
            this.iterable = iterable
        } else {
            if (iterable is String) {
                this.iterableVariable = iterable
            }
        }

        iterableVariableName = section.getString("iterableVariableName", iterableVariableName)!!
    }
}