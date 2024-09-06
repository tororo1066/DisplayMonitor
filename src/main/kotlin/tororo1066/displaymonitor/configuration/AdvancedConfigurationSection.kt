package tororo1066.displaymonitor.configuration

import org.apache.commons.lang3.Validate
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

open class AdvancedConfigurationSection: MemorySection {

    constructor(): super()

    constructor(parent: ConfigurationSection, path: String): super(parent, path)

    companion object {
    }

    private fun createSection(section: ConfigurationSection, key: String): ConfigurationSection {
        return AdvancedConfigurationSection(section, key)
    }

    private fun mapToSection(path: String, map: Map<*, *>): ConfigurationSection {
        val section = createSection(this, path)
        map.forEach { (key, value) ->
            section.set(key.toString(), value)
        }
        return section
    }

    override fun createSection(path: String): ConfigurationSection {
        Validate.notEmpty(path, "Cannot create section at empty path")
        val root = root ?: throw IllegalStateException("Cannot create section without a root")

        val separator = root.options().pathSeparator()

        var i1 = -1
        var i2: Int
        var section: ConfigurationSection = this
        while ((path.indexOf(separator, (i1 + 1).also { i2 = it }).also { i1 = it }) != -1) {
            val node = path.substring(i2, i1)
            val subSection = section.getConfigurationSection(node)
            section = subSection ?: section.createSection(node)
        }

        val key = path.substring(i2)
        if (section === this) {
            val result = createSection(this, key)
            super.set(key, result)
            return result
        }
        return section.createSection(key)
    }

    override fun getConfigurationSection(path: String): ConfigurationSection? {
        var value = get(path)
        if (value is Map<*, *>) {
            return createSection(this, path).apply {
                (value as Map<*, *>).forEach { (key, value) ->
                    set(key.toString(), value)
                }
            }
        }

        value = get(path, getDefault(path))
        return if (value is ConfigurationSection) value else null
    }

    override fun isConfigurationSection(path: String): Boolean {
        val value = get(path)
        return value is ConfigurationSection || value is Map<*, *>
    }

    override fun set(path: String, value: Any?) {
        if (value is Map<*, *>) {
            super.set(path, mapToSection(path, value))
        } else {
            super.set(path, value)
        }
    }

    fun getBukkitVector(key: String, def: Vector? = null): Vector? {
        val split = getString(key, "")?.split(",") ?: return def
        if (split.size != 3) return def
        return try {
            Vector(split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
        } catch (e: NumberFormatException) {
            def
        }
    }

    fun getVector3f(key: String, def: Vector3f? = null): Vector3f? {
        val split = getString(key, "")?.split(",") ?: return def
        if (split.size != 3) return def
        return try {
            Vector3f(split[0].toFloat(), split[1].toFloat(), split[2].toFloat())
        } catch (e: NumberFormatException) {
            def
        }
    }

    fun getQuaternion(key: String, def: Quaternionf? = null): Quaternionf? {
        val split = getString(key, "")?.split(",") ?: return def
        if (split.size != 4) return def
        return try {
            Quaternionf(split[0].toFloat(), split[1].toFloat(), split[2].toFloat(), split[3].toFloat())
        } catch (e: NumberFormatException) {
            def
        }
    }

    inline fun <reified T: Enum<T>> getEnum(key: String, def: T? = null): T? {
        return UsefulUtility.sTry({
            enumValueOf<T>(getString(key, "")?.uppercase() ?: return@sTry def)
        }, { def })
    }

    fun getStringItemStack(key: String, def: ItemStack? = null): ItemStack? {
        return UsefulUtility.sTry({
            Bukkit.getItemFactory().createItemStack(getString(key, "")!!)
        }, { def })
    }
}