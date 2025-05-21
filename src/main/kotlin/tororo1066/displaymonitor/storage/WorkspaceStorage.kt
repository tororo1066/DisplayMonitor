package tororo1066.displaymonitor.storage

import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.Utils.mergeConfiguration
import tororo1066.displaymonitor.configuration.ActionConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitorapi.configuration.IActionConfiguration
import tororo1066.displaymonitorapi.storage.IWorkspaceStorage
import tororo1066.displaymonitorapi.workspace.IAbstractWorkspace
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File

object WorkspaceStorage: IWorkspaceStorage {

    private val workspaces = mutableMapOf<String, IAbstractWorkspace>(
        "DisplayMonitor" to DisplayMonitorWorkspace.instance
    )

    override fun getWorkspaces(): Map<String, IAbstractWorkspace> {
        return workspaces
    }

    override fun getWorkspace(name: String): IAbstractWorkspace? {
        return workspaces[name]
    }

    override fun registerWorkspace(workspace: IAbstractWorkspace) {
        workspaces[workspace.name] = workspace
    }

    internal class DisplayMonitorWorkspace: IAbstractWorkspace {

        companion object {
            val instance by lazy { DisplayMonitorWorkspace() }
        }

        val loadedConfigActions = mutableMapOf<String, ActionConfiguration>()

        init {
            loadDisplayMonitorActions()
        }

        override fun getName(): String {
            return "DisplayMonitor"
        }

        override fun getActionConfigurations(): Map<String, IActionConfiguration> {
            return loadedConfigActions
        }


        fun loadDisplayMonitorActions(configuration: AdvancedConfiguration) {
            configuration.getKeys(false).forEach { key ->
                val actionSection = configuration.getAdvancedConfigurationSection(key) ?: return@forEach
                loadedConfigActions[key] = ActionConfiguration(key, actionSection)
            }
        }

        fun loadDisplayMonitorActions(file: File) {
            if (!file.exists()) return
            if (file.isDirectory) {
                file.listFiles()?.forEach { loadDisplayMonitorActions(it) }
                return
            }

            val yaml = AdvancedConfiguration().mergeConfiguration(YamlConfiguration.loadConfiguration(file))
            loadDisplayMonitorActions(yaml)
        }

        fun loadDisplayMonitorActions() {
            loadedConfigActions.clear()
            val directory = File(SJavaPlugin.plugin.dataFolder, "actions")
            directory.mkdirs()
            loadDisplayMonitorActions(directory)
        }
    }
}