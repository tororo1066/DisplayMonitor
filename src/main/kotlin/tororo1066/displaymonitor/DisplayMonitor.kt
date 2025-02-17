package tororo1066.displaymonitor

import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationRegistry
import net.kyori.adventure.translation.Translator
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.commands.DisplayCommands
import tororo1066.displaymonitor.elements.SettableProcessor
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.displaymonitor.storage.ElementStorage
import tororo1066.displaymonitorapi.IDisplayMonitor
import tororo1066.displaymonitorapi.IDisplayMonitor.DisplayMonitorInstance
import tororo1066.displaymonitorapi.elements.ISettableProcessor
import tororo1066.displaymonitorapi.storage.IActionStorage
import tororo1066.displaymonitorapi.storage.IElementStorage
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.jar.JarFile

class DisplayMonitor: SJavaPlugin(UseOption.SConfig), IDisplayMonitor {

    companion object {
        fun log(context: String, message: String) {
            plugin.logger.info("[$context] $message")
        }

        fun warn(context: String, message: String) {
            plugin.logger.warning("[$context] $message")
        }

        fun error(context: String, message: String) {
            plugin.logger.severe("[$context] $message")
        }

        fun translate(key: String, locale: Locale, vararg args: Any?): String {
            return GlobalTranslator.translator().translate(key, locale)?.format(args, StringBuffer(), null)?.toString() ?: key
        }

        fun translate(key: String, vararg args: Any?): String {
            return translate(key, Locale.getDefault(), *args)
        }
    }

    override fun onStart() {
        DisplayMonitorInstance.setInstance(this)

        Bukkit.getScheduler().runTaskLater(this, Runnable {
            ActionStorage

            Config.load()
            registerBundle()
            ElementStorage
            DisplayCommands()
        }, 1)
    }

    override fun onEnd() {
        ActionStorage.contextStorage.values.forEach {
            it.values.forEach { context ->
                context.publicContext.elements.values.forEach { element ->
                    element.remove()
                }
                context.publicContext.stop = true
            }
        }
    }

    private fun registerBundle() {

        val context = "RegisterTranslation"
        val registry = TranslationRegistry.create(Key.key("displaymonitor:translation"))
        GlobalTranslator.translator().removeSource(registry)
        registry.defaultLocale(Locale.getDefault())
        val jarFile = JarFile(file)
        jarFile.use { jar ->
            val entry = jar.getEntry("config.yml")
            if (entry == null) {
                warn(context, "config.yml not found in jar")
                return@registerBundle
            }
            val stream = jar.getInputStream(entry)
            val config = YamlConfiguration.loadConfiguration(stream.reader())
            val serverConfig = this.config
            val version = config.getInt("translation.version")

            val serverTranslationVersion = serverConfig.getInt("translation.version")

            if (!serverConfig.getBoolean("translation.initialized") || version > serverTranslationVersion) {
                log(context, "Initializing translations")
                File(dataFolder, "translations").mkdirs()
                val streams = jar.entries().asSequence()
                    .filter { it.name.startsWith("translations/") && !it.isDirectory }
                    .map { it.name to jar.getInputStream(it) }
                    .toList()
                streams.forEach { (name, stream) ->
                    log(context, "Registering $name")
                    val file = File(dataFolder, name)
                    stream.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                this.config.set("translation.version", version)
                this.config.set("translation.initialized", true)
                saveConfig()
            }
        }



        val files = File(dataFolder, "translations").listFiles()


        if (files != null) {
            for (file in files) {
                if (file.extension == "properties") {
                    val name = file.nameWithoutExtension
                    val locale = Translator.parseLocale(name) ?: continue
                    val bundle = PropertyResourceBundle(file.inputStream())
                    registry.registerAll(locale, bundle, true)
                }
            }
        }

        GlobalTranslator.translator().addSource(registry)
    }

    override fun getActionStorage(): IActionStorage {
        return ActionStorage
    }

    override fun getElementStorage(): IElementStorage {
        return ElementStorage
    }

    override fun getSettableProcessor(): ISettableProcessor {
        return SettableProcessor
    }
}