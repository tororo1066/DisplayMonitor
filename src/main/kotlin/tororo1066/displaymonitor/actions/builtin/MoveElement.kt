package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Location
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SInput

@ClassDoc(
    name = "MoveElement",
    description = "Elementを移動する。"
)
class MoveElement: AbstractAction() {

    @ParameterDoc(
        name = "element",
        description = "移動するElementの名前。",
        type = ParameterType.String
    )
    var element = ""
    @ParameterDoc(
        name = "location",
        description = "移動先の座標。",
        type = ParameterType.Location
    )
    var location: Location? = null

    override fun run(context: IActionContext): ActionResult {
        val location = location ?: return ActionResult.noParameters("Location is not set")
        val element = context.publicContext.elements[element] ?: return ActionResult.noParameters("Element not found")
        element.move(location)
        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        element = section.getString("element", "")!!
        location = SInput.modifyClassValue(Location::class.java, section.getString("location", "")!!).second
    }
}