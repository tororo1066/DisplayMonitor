package tororo1066.displaymonitorapi.actions;

public record ActionResult(
        ActionResultType resultType,
        String message
) {

    public static ActionResult success() {
        return new ActionResult(ActionResultType.SUCCESS, "");
    }

    public static ActionResult success(String message) {
        return new ActionResult(ActionResultType.SUCCESS, message);
    }

    public static ActionResult failed() {
        return new ActionResult(ActionResultType.FAILED, "");
    }

    public static ActionResult failed(String message) {
        return new ActionResult(ActionResultType.FAILED, message);
    }

    public static ActionResult noParameters() {
        return new ActionResult(ActionResultType.NO_PARAMETERS, "");
    }

    public static ActionResult noParameters(String message) {
        return new ActionResult(ActionResultType.NO_PARAMETERS, message);
    }

    public static ActionResult casterRequired() {
        return new ActionResult(ActionResultType.CASTER_REQUIRED, "");
    }

    public static ActionResult casterRequired(String message) {
        return new ActionResult(ActionResultType.CASTER_REQUIRED, message);
    }

    public static ActionResult targetRequired() {
        return new ActionResult(ActionResultType.TARGET_REQUIRED, "");
    }

    public static ActionResult targetRequired(String message) {
        return new ActionResult(ActionResultType.TARGET_REQUIRED, message);
    }

    public static ActionResult locationRequired() {
        return new ActionResult(ActionResultType.LOCATION_REQUIRED, "");
    }

    public static ActionResult locationRequired(String message) {
        return new ActionResult(ActionResultType.LOCATION_REQUIRED, message);
    }




    public enum ActionResultType {
        SUCCESS,
        FAILED,
        NO_PARAMETERS,
        CASTER_REQUIRED,
        TARGET_REQUIRED,
        LOCATION_REQUIRED,
    }
}
