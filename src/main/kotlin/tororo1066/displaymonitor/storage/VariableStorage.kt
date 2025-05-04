package tororo1066.displaymonitor.storage

import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File

object VariableStorage {

    val files = mutableMapOf<String, YamlConfiguration>()

    init {
        load()
    }

    fun load() {
        val folder = File(SJavaPlugin.plugin.dataFolder, "variables")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        folder.listFiles()?.forEach { file ->
            if (file.extension == "yml") {
                load(file)
            }
        }
    }

    fun load(file: File) {
        val config = YamlConfiguration()
        config.load(file)
        files[file.nameWithoutExtension] = config
    }

    fun getVariableFile(name: String): YamlConfiguration? {
        return files[name]
    }
}