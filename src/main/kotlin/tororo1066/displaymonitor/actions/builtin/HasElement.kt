package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.CheckAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "HasElement",
    description = "指定した名前のElementが存在するかを確認する。"
)
class HasElement: CheckAction() {

    @ParameterDoc(
        name = "name",
        description = "Elementの名前。"
    )
    var name: String = ""

    override fun isAllowed(context: IActionContext): Boolean {
        return context.publicContext.elements.containsKey(name)
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        super.prepare(section)
        name = section.getString("name", "")!!
    }
}