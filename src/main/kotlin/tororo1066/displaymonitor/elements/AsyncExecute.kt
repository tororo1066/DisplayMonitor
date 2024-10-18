package tororo1066.displaymonitor.elements

import tororo1066.displaymonitor.actions.ActionContext

class AsyncExecute(execute: (context: ActionContext) -> Unit): Execute(execute) {
    companion object {
        fun empty(): AsyncExecute {
            return AsyncExecute {}
        }
    }
}