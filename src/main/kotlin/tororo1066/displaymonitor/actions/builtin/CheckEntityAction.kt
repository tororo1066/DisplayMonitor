package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.CheckAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "CheckEntity",
    description = "対象が指定したエンティティであるかを確認する。"
)
class CheckEntityAction: CheckAction() {

    @ParameterDoc(
        name = "allowedEntities",
        description = "許可するエンティティのリスト。",
        type = ParameterType.StringList
    )
    var allowedEntities = ArrayList<String>()
    @ParameterDoc(
        name = "disallowedEntities",
        description = "許可しないエンティティのリスト。",
        type = ParameterType.StringList
    )
    var disallowedEntities = ArrayList<String>()

    override fun isAllowed(context: IActionContext): Boolean {
        val entity = context.target ?: return false
        val entityType = entity.type.name.lowercase()
        return (allowedEntities.isEmpty() || allowedEntities.contains(entityType)) && !disallowedEntities.contains(entityType)
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        super.prepare(section)
        allowedEntities = ArrayList(section.getStringList("allowedEntities").map { it.lowercase() })
        disallowedEntities = ArrayList(section.getStringList("disallowedEntities").map { it.lowercase() })
        section.getString("allowedEntities")?.let { allowedEntities.add(it.lowercase()) }
        section.getString("disallowedEntities")?.let { disallowedEntities.add(it.lowercase()) }
    }
}