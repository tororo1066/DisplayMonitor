package tororo1066.displaymonitor

import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitorapi.IDisplayUtils
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.jar.JarFile

object Utils: IDisplayUtils {

    fun AdvancedConfiguration.mergeConfiguration(second: ConfigurationSection): AdvancedConfiguration {
        val yaml = this.clone()
        second.getValues(true).forEach { (key, value) ->
            yaml.set(key, value)
        }
        return yaml
    }

    fun hexToBukkitColor(hex: String): Color? {
        try {
            val removePrefix = hex.removePrefix("#")
            val colorInt = Integer.parseInt(removePrefix, 16)
            val isArgb = removePrefix.length == 8
            return if (isArgb) {
                Color.fromARGB(colorInt)
            } else {
                Color.fromRGB(colorInt)
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun isPointInsideRotatedRect(
        p: Player,
        entity: Display,
        scale: Vector,
        maxDistance: Double,
        visible: Boolean = false
    ): Boolean {
        val playerLocation = p.eyeLocation
        val entityLocation = entity.location

        val obb = OBB(
            entityLocation.toVector().subtract(Vector(scale.x / 2, scale.y / 2, scale.z / 2)).toVector3f(),
            entityLocation.toVector().add(Vector(scale.x / 2, scale.y / 2, scale.z / 2)).toVector3f()
        )

        obb.modifyBy(p, entity)

        if (visible) {
            obb.showParticle(p.world, p)
        }

        return obb.rayTrace(
            playerLocation.toVector().toVector3f(),
            playerLocation.direction.toVector3f(),
            maxDistance
        )
    }

    fun URL.getClasses(packageName: String): List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        val src = ArrayList<File>()
        val srcFile = try {
            File(toURI())
        } catch (_: IllegalArgumentException) {
            File((openConnection() as JarURLConnection).jarFileURL.toURI())
        } catch (_: URISyntaxException) {
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

    override fun toAdvancedConfiguration(config: YamlConfiguration): IAdvancedConfiguration {
        val advancedConfig = AdvancedConfiguration()
        config.getValues(true).forEach { (key, value) ->
            advancedConfig.set(key, value)
        }

        return advancedConfig
    }
}