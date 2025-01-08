package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Location
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.ActionResult
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.tororopluginapi.SInput

class MoveElement: AbstractAction() {

    var element = ""
    var location: Location? = null

    override fun run(context: ActionContext): ActionResult {
        location ?: return ActionResult.noParameters("Location is not set")
        val element = context.publicContext.elements[element] ?: return ActionResult.noParameters("Element not found")
        element.move(location!!)
        return ActionResult.success()
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        element = section.getString("element", "")!!
        location = SInput.modifyClassValue(Location::class.java, section.getString("location", "")!!).second
    }
}