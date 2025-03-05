package tororo1066.displaymonitor.storage

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import tororo1066.displaymonitor.Utils.mergeConfiguration
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.PublicActionContext
import tororo1066.displaymonitor.actions.builtin.*
import tororo1066.displaymonitor.configuration.ActionConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitorapi.actions.IAbstractAction
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.events.ActionRegisteringEvent
import tororo1066.displaymonitorapi.storage.IActionStorage
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.util.UUID
import java.util.function.Function

object ActionStorage: IActionStorage {
    val actions = mutableMapOf<String, Class<out IAbstractAction>>()
    val contextStorage = mutableMapOf<UUID, MutableMap<UUID, IActionContext>>()
    val contextByName = mutableMapOf<String, UUID>()

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
        actions["End"] = EndAction::class.java
        actions["If"] = IfAction::class.java
        actions["Repeat"] = RepeatAction::class.java
        actions["Asynchronous"] = AsynchronousAction::class.java
        actions["UpdateParameters"] = UpdateParametersAction::class.java
        actions["WaitCommand"] = WaitCommandAction::class.java
        actions["ModifyVariable"] = ModifyVariableAction::class.java
        actions["MoveElement"] = MoveElement::class.java
        actions["RemoveEntity"] = RemoveEntityAction::class.java
        actions["CheckEntity"] = CheckEntityAction::class.java
        actions["SetBlock"] = SetBlockAction::class.java
        actions["Target"] = TargetAction::class.java
        actions["StoreData"] = StoreDataAction::class.java
        actions["RestoreData"] = RestoreDataAction::class.java
        actions["Lightning"] = LightningAction::class.java
        actions["AccessAction"] = AccessAction::class.java
        actions["RunAction"] = RunAction::class.java

        ActionRegisteringEvent().callEvent()

        loadActions()
    }

    override fun registerAction(name: String, action: Class<out IAbstractAction>) {
        actions[name] = action
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

    override fun createPublicContext(): IPublicActionContext {
        return PublicActionContext()
    }

    override fun createActionContext(publicContext: IPublicActionContext): IActionContext {
        return ActionContext(publicContext)
    }

    override fun trigger(
        name: String,
        context: IActionContext,
        condition: Function<IAdvancedConfigurationSection, Boolean>?
    ) {
        loadedConfigActions.filter { it.value.triggers.contains(name) }.forEach {
            val triggerSection = it.value.triggers[name] ?: return@forEach
            if (condition == null || condition.apply(triggerSection)) {
                it.value.run(context)
            }
        }
    }
}