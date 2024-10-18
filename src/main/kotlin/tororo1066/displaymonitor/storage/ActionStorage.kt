package tororo1066.displaymonitor.storage

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import tororo1066.displaymonitor.Utils.mergeConfiguration
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.*
import tororo1066.displaymonitor.actions.builtin.*
import tororo1066.displaymonitor.configuration.ActionConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.events.TriggerEvent
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.util.UUID

object ActionStorage {
    val actions = mutableMapOf<String, Class<out AbstractAction>>()
//    val contextStorage = mutableMapOf<UUID, ActionContext>()
    val contextStorage = mutableMapOf<UUID, MutableMap<UUID, ActionContext>>()

    val loadedConfigActions = mutableMapOf<String, ActionConfiguration>()

    init {
        actions["SummonElement"] = SummonElement::class.java
        actions["EditElement"] = EditElement::class.java
        actions["Message"] = MessageAction::class.java
        actions["Delay"] = DelayAction::class.java
        actions["Command"] = CommandAction::class.java
        actions["RemoveAllElement"] = RemoveAllElement::class.java
        actions["RemoveElement"] = RemoveElement::class.java
        actions["Stop"] = StopAction::class.java
        actions["CheckExpression"] = CheckExpressionAction::class.java
        actions["Repeat"] = RepeatAction::class.java
        actions["Asynchronous"] = AsynchronousAction::class.java
        actions["UpdateParameters"] = UpdateParametersAction::class.java

        loadActions()
    }

    fun registerAction(name: String, action: Class<out AbstractAction>) {
        actions[name] = action
    }

    fun registerAction(action: Class<out AbstractAction>) {
        actions[action.simpleName] = action
    }

    fun loadActions(configuration: AdvancedConfiguration) {
        configuration.getKeys(false).forEach { key ->
            val actionSection = configuration.getAdvancedConfigurationSection(key) ?: return@forEach
            loadedConfigActions[key] = ActionConfiguration(configuration, actionSection)
        }
    }

    fun loadActions(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach { loadActions(it) }
            return
        }

        val yaml = AdvancedConfiguration().mergeConfiguration(YamlConfiguration.loadConfiguration(file))
        loadActions(yaml)
    }

    fun loadActions() {
        loadedConfigActions.clear()
        val directory = File(SJavaPlugin.plugin.dataFolder, "actions")
        directory.mkdirs()
        loadActions(directory)
    }

    fun trigger(name: String, p: Player) {
        loadedConfigActions.filter { it.value.triggers.contains(name) }.forEach { it.value.run(p) }
        Bukkit.getPluginManager().callEvent(TriggerEvent(name, p))
    }
}