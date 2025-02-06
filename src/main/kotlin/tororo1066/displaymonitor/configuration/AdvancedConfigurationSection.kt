package tororo1066.displaymonitor.configuration

import org.apache.commons.lang3.Validate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sItem.SItem
import java.util.function.Function

open class AdvancedConfigurationSection: MemorySection, IAdvancedConfigurationSection {

    constructor(): super()

    constructor(parent: AdvancedConfigurationSection, path: String): super(parent, path)

    constructor(copy: ConfigurationSection): super(copy.parent!!, copy.name)

    companion object {
    }

    protected fun createAdvancedSection(key: String): AdvancedConfigurationSection {
        return AdvancedConfigurationSection(this, key)
    }

    private fun mapToSection(path: String, map: Map<*, *>): ConfigurationSection {
        val section = createAdvancedSection(path)
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
            val result = createAdvancedSection(key)
            super.set(key, result)
            return result
        }
        return section.createSection(key)
    }

    override fun getConfigurationSection(path: String): ConfigurationSection? {
        var value = get(path, null)
        if (value != null) {
            if (value is Map<*, *>) {
                return createAdvancedSection(path).apply {
                    (value as Map<*, *>).forEach { (key, value) ->
                        set(key.toString(), value)
                    }
                }
            }
            return value as? ConfigurationSection
        }

        value = get(path, getDefault(path))
        return if (value is ConfigurationSection) createSection(path) else null
    }

    override fun isConfigurationSection(path: String): Boolean {
        val value = get(path)
        return value is ConfigurationSection || value is Map<*, *>
    }

    override fun getAdvancedConfigurationSection(path: String): AdvancedConfigurationSection? {
        return getConfigurationSection(path) as? AdvancedConfigurationSection
    }

    override fun getAdvancedConfigurationSectionList(path: String): List<AdvancedConfigurationSection> {
        return getMapList(path).map {
            toAdvancedConfigurationSection(it)
        }
    }

    private fun toAdvancedConfigurationSection(map: Map<*, *>, path: String = ""): AdvancedConfigurationSection {
        val section = createAdvancedSection(path)
        map.forEach { (key, value) ->
            section.set(key.toString(), value)
        }
        return section
    }

    override fun set(path: String, value: Any?) {
        if (value is Map<*, *>) {
            super.set(path, mapToSection(path, value))
            return
        }

        if (value is ConfigurationSection) {
            super.set(path, createAdvancedSection(path).apply {
                value.getValues(true).forEach { (key, value) ->
                    set(key, value)
                }
            })
            return
        }

        super.set(path, value)
    }

    override fun get(path: String, def: Any?): Any? {
        val value = super.get(path, def)
        if (value !is String) return value

        val root = root ?: return value
        if (root is AdvancedConfiguration) {
            return root.evaluate(value)
        }

        return value
    }

    override fun getBukkitVector(path: String): Vector? {
        val value = getString(path) ?: return null
        val root = root as? AdvancedConfiguration
        val split = value.split(",").map { root?.evaluate(it)?.toString() ?: it }
        if (split.size != 3) return null
        return try {
            Vector(split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getBukkitVector(path: String, def: Vector): Vector {
        return getBukkitVector(path) ?: def
    }

    override fun getStringLocation(path: String): Location? {
        val value = getString(path) ?: return null
        val root = root as? AdvancedConfiguration
        val split = value.split(",").map { root?.evaluate(it)?.toString() ?: it }
        when (split.size) {
            3 -> {
                val x = split[0].toDouble()
                val y = split[1].toDouble()
                val z = split[2].toDouble()
                return Location(null, x, y, z)
            }
            4 -> {
                val world = Bukkit.getWorld(split[0]) ?: return null
                val x = split[1].toDouble()
                val y = split[2].toDouble()
                val z = split[3].toDouble()
                return Location(world, x, y, z)
            }
            6 -> {
                val world = Bukkit.getWorld(split[0]) ?: return null
                val x = split[1].toDouble()
                val y = split[2].toDouble()
                val z = split[3].toDouble()
                val yaw = split[4].toFloat()
                val pitch = split[5].toFloat()
                return Location(world, x, y, z, yaw, pitch)
            }
            else -> return null
        }
    }

    override fun getStringLocation(path: String, def: Location): Location {
        return getStringLocation(path) ?: def
    }

    override fun getVector3f(path: String): Vector3f? {
        val value = getString(path) ?: return null
        val root = root as? AdvancedConfiguration
        val split = value.split(",").map { root?.evaluate(it)?.toString() ?: it }
        return try {
            Vector3f(split[0].toFloat(), split[1].toFloat(), split[2].toFloat())
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getVector3f(path: String, def: Vector3f): Vector3f {
        return getVector3f(path) ?: def
    }

    override fun <T : Enum<T>> getEnum(path: String, clazz: Class<T>): T? {
        return clazz.enumConstants.firstOrNull { it.name == getString(path) }
    }

    override fun <T : Enum<T>> getEnum(path: String, clazz: Class<T>, def: T): T {
        return getEnum(path, clazz) ?: def
    }

    inline fun <reified T: Enum<T>> getEnum(key: String, def: T? = null): T? {
        return UsefulUtility.sTry({
            enumValueOf<T>(getString(key, "")?.uppercase() ?: return@sTry def)
        }, { def })
    }

    override fun getStringItemStack(path: String): ItemStack? {
        val string = getString(path, "") ?: return null

        if (string.startsWith("base64:")) {
            return SItem.fromBase64(string.substring(7))?.build()
        }

        return UsefulUtility.sTry({
            Bukkit.getItemFactory().createItemStack(string)
        }, { null })
    }

    override fun getStringItemStack(path: String, def: ItemStack): ItemStack {
        return getStringItemStack(path) ?: def
    }

    override fun getConfigExecute(path: String): tororo1066.displaymonitorapi.configuration.Execute? {
        val list = getAdvancedConfigurationSectionList(path)
        if (list.isEmpty()) return null
        return tororo1066.displaymonitorapi.configuration.Execute {
            ActionRunner.run(root as AdvancedConfiguration, list, it, async = false, disableAutoStop = false)
        }
    }

    override fun getConfigExecute(path: String, def: tororo1066.displaymonitorapi.configuration.Execute): tororo1066.displaymonitorapi.configuration.Execute {
        return getConfigExecute(path) ?: def
    }

    override fun getAsyncConfigExecute(path: String): tororo1066.displaymonitorapi.configuration.AsyncExecute? {
        val list = getAdvancedConfigurationSectionList(path)
        if (list.isEmpty()) return null
        return tororo1066.displaymonitorapi.configuration.AsyncExecute {
            ActionRunner.run(root as AdvancedConfiguration, list, it, async = true, disableAutoStop = false)
        }
    }

    override fun getAsyncConfigExecute(path: String, def: tororo1066.displaymonitorapi.configuration.AsyncExecute): tororo1066.displaymonitorapi.configuration.AsyncExecute {
        return getAsyncConfigExecute(path) ?: def
    }

//    open fun getAnyConfigExecute(key: String, def: Execute? = null): Execute? {
//        val root = root as? AdvancedConfiguration ?: return def
//        if (isList("${key}_async")) {
//            val list = getAdvancedConfigurationSectionList("${key}_async")
//            if (list.isNotEmpty()) {
//                return AsyncExecute {
//                    ActionRunner.run(root, list, it, async = true, disableAutoStop = true)
//                }
//            }
//        } else {
//            val list = getAdvancedConfigurationSectionList(key)
//            if (list.isNotEmpty()) {
//                return Execute {
//                    ActionRunner.run(root, list, it)
//                }
//            }
//        }
//
//        return def
//    }

    override fun getRotation(path: String): Quaternionf? {
        val value = getString(path) ?: return null
        val root = root as? AdvancedConfiguration
        val split = value.split(",").map { root?.evaluate(it)?.toString() ?: it }
        if (split.isEmpty()) return null
        val type = split[0].lowercase()
        try {
            when(type) {
                "euler" -> {
                    if (split.size != 4) return null
                    val x = split[1].toFloat()
                    val y = split[2].toFloat()
                    val z = split[3].toFloat()
                    return Quaternionf().rotationXYZ(x, y, z)
                }
                "axis" -> {
                    if (split.size != 5) return null
                    val angle = split[1].toFloat()
                    val x = split[2].toFloat()
                    val y = split[3].toFloat()
                    val z = split[4].toFloat()
                    return Quaternionf().rotationAxis(angle, x, y, z)
                }
                else -> {
                    if (split.size != 4 && split.size != 5) return null
                    val minus = if (split.size == 4) 1 else 0
                    val x = split[1-minus].toFloat()
                    val y = split[2-minus].toFloat()
                    val z = split[3-minus].toFloat()
                    val w = split[4-minus].toFloat()
                    return Quaternionf(x, y, z, w)
                }
            }
        } catch (e: NumberFormatException) {
            return null
        }
    }

    override fun getRotation(path: String, def: Quaternionf): Quaternionf {
        return getRotation(path) ?: def
    }

    override fun <T : Any> withParameters(
        parameters: Map<String, Any>,
        function: Function<IAdvancedConfigurationSection, T?>
    ): T? {
        val root = root as? AdvancedConfiguration
        val value: T?
        if (root != null) {
            val old = root.parameters
            root.parameters = old.toMutableMap().apply {
                putAll(parameters)
            }
            value = function.apply(this)
            root.parameters = old
        } else {
            value = function.apply(this)
        }

        return value
    }

    override fun toString(): String {
        return "AdvancedConfigurationSection(${getValues(true)})"
    }
}