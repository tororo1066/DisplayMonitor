package tororo1066.displaymonitor.config

import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.DisplayMonitor
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.jar.JarFile

object Config {

    var prefix: Component = Component.text("§b[§6Display§eMonitor§b] §r")
    var debug = false
    val subConfigs = ArrayList<AbstractConfig>()

    fun load() {
        SJavaPlugin.Companion.plugin.reloadConfig()
        val config = SJavaPlugin.Companion.plugin.config

        prefix = config.getRichMessage("prefix", prefix)!!
        debug = config.getBoolean("debug", false)

        val directory = File(SJavaPlugin.plugin.dataFolder, "config")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        subConfigs.clear()
        val classes = SJavaPlugin.plugin.javaClass.protectionDomain.codeSource.location.getClasses("tororo1066.displaymonitor.config.sub")
        classes.forEach {
            if (AbstractConfig::class.java.isAssignableFrom(it)) {
                val constructor = it.getConstructor()
                val configInstance = constructor.newInstance() as AbstractConfig
                val file = File(directory, "${configInstance.internalName}.yml")
                if (!file.exists()) {
                    file.createNewFile()
                    val yml = YamlConfiguration.loadConfiguration(file)
                    configInstance.saveDefaultConfig(yml)
                    yml.save(file)
                } else {
                    val yml = YamlConfiguration.loadConfiguration(file)
                    configInstance.loadConfig(yml)
                }

                subConfigs.add(configInstance)
            }
        }
    }

    inline fun <reified T: AbstractConfig> getConfig(): T? {
        return subConfigs.firstOrNull { it is T } as? T
    }

    private fun URL.getClasses(packageName: String): List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        val src = ArrayList<File>()
        val srcFile = try {
            File(toURI())
        } catch (e: IllegalArgumentException) {
            File((openConnection() as JarURLConnection).jarFileURL.toURI())
        } catch (e: URISyntaxException) {
            File(path)
        }

        src += srcFile

        src.forEach { s ->
            JarFile(s).stream().filter { it.name.endsWith(".class") }.forEach second@ {
                val name = it.name.replace('/', '.').substring(0, it.name.length - 6)
                if (!name.contains(packageName)) return@second

                kotlin.runCatching {
                    classes.add(Class.forName(name, false, DisplayMonitor::class.java.classLoader))
                }
            }
        }

        return classes
    }
}