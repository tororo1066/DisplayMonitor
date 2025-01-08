package tororo1066.displaymonitor.actions

class ActionResult(var actionResultType: ActionResultType, var message: String = "") {
    companion object {
        fun success(message: String = "") = ActionResult(ActionResultType.SUCCESS, message)
        fun failed(message: String = "") = ActionResult(ActionResultType.FAILED, message)
        fun noParameters(message: String = "") = ActionResult(ActionResultType.NO_PARAMETERS, message)
        fun casterRequired(message: String = "") = ActionResult(ActionResultType.CASTER_REQUIRED, message)
        fun targetRequired(message: String = "") = ActionResult(ActionResultType.TARGET_REQUIRED, message)
        fun locationRequired(message: String = "") = ActionResult(ActionResultType.LOCATION_REQUIRED, message)
    }

    enum class ActionResultType {
        SUCCESS,
        FAILED,
        NO_PARAMETERS,
        CASTER_REQUIRED,
        TARGET_REQUIRED,
        LOCATION_REQUIRED,
    }
}