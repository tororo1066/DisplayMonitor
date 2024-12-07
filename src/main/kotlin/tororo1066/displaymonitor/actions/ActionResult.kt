package tororo1066.displaymonitor.actions

class ActionResult(var actionResultType: ActionResultType, var message: String = "") {
    companion object {
        fun success(message: String = "") = ActionResult(ActionResultType.SUCCESS, message)
        fun noParameters(message: String = "") = ActionResult(ActionResultType.NO_PARAMETERS, message)
        fun playerRequired(message: String = "") = ActionResult(ActionResultType.PLAYER_REQUIRED, message)
        fun locationRequired(message: String = "") = ActionResult(ActionResultType.LOCATION_REQUIRED, message)
    }

    enum class ActionResultType {
        SUCCESS,
        NO_PARAMETERS,
        PLAYER_REQUIRED,
        LOCATION_REQUIRED,
    }
}