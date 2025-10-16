package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "MoveElement",
    description = "Elementを移動する。"
)
class MoveElement: AbstractAction() {

    @ParameterDoc(
        name = "name",
        description = "移動するElementの名前。"
    )
    var name = ""

    override fun run(context: IActionContext): ActionResult {
        val location = context.location ?: return ActionResult.noParameters("Location is not set")
        val element = context.publicContext.elements[name] ?: return ActionResult.noParameters("Element not found")
        element.move(location)
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        name = section.getString("name", "")!!
    }
}