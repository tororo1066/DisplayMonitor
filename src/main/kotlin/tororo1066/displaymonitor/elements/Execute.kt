package tororo1066.displaymonitor.elements

import tororo1066.displaymonitor.actions.ActionContext

open class Execute(val execute: (context: ActionContext) -> Unit) {
    operator fun invoke(context: ActionContext) {
        execute(context)
    }

    companion object {
        fun empty(): Execute {
            return Execute {}
        }
    }
}