package tororo1066.displaymonitor

import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.TranslationRegistry
import net.kyori.adventure.translation.Translator
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.displaymonitor.commands.DisplayCommands
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File
import java.util.PropertyResourceBundle
import java.util.jar.JarFile

class DisplayMonitor: SJavaPlugin(UseOption.SConfig) {

    override fun onStart() {
        registerBundle()
        DisplayCommands()
    }

    private fun registerBundle() {
        val registry = TranslationRegistry.create(Key.key("displaymonitor:translation"))
        val jarFile = JarFile(file)
        jarFile.use { jar ->
            val entry = jar.getEntry("config.yml")
            if (entry == null) {
                logger.warning("config.yml not found in jar")
                return@registerBundle
            }
            val stream = jar.getInputStream(entry)
            val config = YamlConfiguration.loadConfiguration(stream.reader())
            val version = config.getInt("translation.version")

            val serverTranslationVersion = config.getInt("translation.version")

            if (!config.getBoolean("translation.initialized") || version > serverTranslationVersion) {
                File(dataFolder, "translations").mkdirs()
                val streams = jar.entries().asSequence()
                    .filter { it.name.startsWith("translations/") && !it.isDirectory }
                    .map { it.name to jar.getInputStream(it) }
                    .toList()
                streams.forEach { (name, stream) ->
                    logger.info("Loading translation $name")
                    val file = File(dataFolder, name)
                    stream.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                config.set("translation.version", version)
                config.set("translation.initialized", true)
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
    }
}