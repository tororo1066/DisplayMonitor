package tororo1066.displaymonitor

import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection

object Utils {
    fun AdvancedConfigurationSection.clone(): AdvancedConfigurationSection {
        val yaml = AdvancedConfigurationSection(this)
        getValues(true).forEach { (key, value) ->
            yaml.set(key, value)
        }
        return yaml
    }

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
}