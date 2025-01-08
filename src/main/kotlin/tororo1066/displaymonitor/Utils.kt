package tororo1066.displaymonitor

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.Execute
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

object Utils {

    fun checkMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw IllegalStateException("This method must be called from the main thread")
        }
    }

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
            val colorInt = Integer.parseInt(hex.removePrefix("#"), 16)
            return Color.fromRGB(colorInt)
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