package tororo1066.displaymonitor

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

object Utils {

    fun checkMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw IllegalStateException("This method must be called from the main thread")
        }
    }

    fun ConfigurationSection.getBukkitVector(key: String, def: Vector? = null): Vector? {
        val split = getString(key, "")?.split(",") ?: return def
        if (split.size != 3) return def
        return try {
            Vector(split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
        } catch (e: NumberFormatException) {
            def
        }
    }

    fun ConfigurationSection.getVector3f(key: String, def: Vector3f? = null): Vector3f? {
        val split = getString(key, "")?.split(",") ?: return def
        if (split.size != 3) return def
        return try {
            Vector3f(split[0].toFloat(), split[1].toFloat(), split[2].toFloat())
        } catch (e: NumberFormatException) {
            def
        }
    }

    fun ConfigurationSection.getQuaternion(key: String, def: Quaternionf? = null): Quaternionf? {
        val split = getString(key, "")?.split(",") ?: return def
        if (split.size != 4) return def
        return try {
            Quaternionf(split[0].toFloat(), split[1].toFloat(), split[2].toFloat(), split[3].toFloat())
        } catch (e: NumberFormatException) {
            def
        }
    }

    inline fun <reified T: Enum<T>> ConfigurationSection.getEnum(key: String, def: T? = null): T? {
        return UsefulUtility.sTry({
            enumValueOf<T>(getString(key, "")?.uppercase() ?: return@sTry def)
        }, { def })
    }

    fun ConfigurationSection.getStringItemStack(key: String, def: ItemStack? = null): ItemStack? {
        return UsefulUtility.sTry({
            Bukkit.getItemFactory().createItemStack(getString(key, "")!!)
        }, { def })
    }

    fun ConfigurationSection.getConfigurationSectionList(key: String, def: List<ConfigurationSection> = emptyList()): List<ConfigurationSection> {
        return getList(key)?.mapNotNull {
            if (it is ConfigurationSection) return@mapNotNull it
            if (it !is HashMap<*, *>) return@mapNotNull null
            YamlConfiguration().apply {
                it.forEach { (key, value) ->
                    set(key.toString(), value)
                }
            }
        } ?: def
    }

    fun ConfigurationSection.clone(): ConfigurationSection {
        val yaml = YamlConfiguration()
        getValues(true).forEach { (key, value) ->
            yaml.set(key, value)
        }
        return yaml
    }

    fun ConfigurationSection.mergeConfiguration(second: ConfigurationSection): ConfigurationSection {
        val yaml = this.clone()
        second.getValues(true).forEach { (key, value) ->
            yaml.set(key, value)
        }
        return yaml
    }

    fun AdvancedConfiguration.clone(): AdvancedConfiguration {
        val yaml = AdvancedConfiguration()
        getValues(true).forEach { (key, value) ->
            yaml.set(key, value)
        }
        return yaml
    }

    fun AdvancedConfiguration.mergeConfiguration(second: ConfigurationSection): AdvancedConfigurationSection {
        val yaml = this.clone()
        second.getValues(true).forEach { (key, value) ->
            yaml.set(key, value)
        }
        return yaml
    }

    fun hexToBukkitColor(hex: String): Color {
        val colorInt = Integer.parseInt(hex.removePrefix("#"), 16)
        return Color.fromRGB(colorInt)
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
            entityLocation.toVector().subtract(Vector(scale.x / 2, scale.y / 2, scale.z / 2)),
            entityLocation.toVector().add(Vector(scale.x / 2, scale.y / 2, scale.z / 2))
        )

        obb.modifyBy(p, entity)

        if (visible) {
            obb.showParticle(p)
        }

        return obb.rayTrace(
            playerLocation.toVector(),
            playerLocation.direction,
            maxDistance
        )
    }
}