package tororo1066.displaymonitor.actions.builtin

import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.CheckAction
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

class CheckEntityAction: CheckAction() {

    var allowedEntities = emptyList<String>()
    var disallowedEntities = emptyList<String>()

    override fun isAllowed(context: ActionContext): Boolean {
        val entity = context.target ?: return false
        val entityType = entity.type.name.lowercase()
        return (allowedEntities.isEmpty() || allowedEntities.contains(entityType)) && !disallowedEntities.contains(entityType)
    }

    override fun prepare(section: AdvancedConfigurationSection) {
        super.prepare(section)
        allowedEntities = section.getStringList("allowedEntities").map { it.lowercase() }
        disallowedEntities = section.getStringList("disallowedEntities").map { it.lowercase() }
    }
}