package tororo1066.displaymonitor.configuration

import org.apache.commons.lang3.Validate
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.displaymonitor.elements.AsyncExecute
import tororo1066.displaymonitor.elements.Execute
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

open class AdvancedConfigurationSection: MemorySection {

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

    fun getAdvancedConfigurationSection(path: String): AdvancedConfigurationSection? {
        return getConfigurationSection(path) as? AdvancedConfigurationSection
    }

    fun getAdvancedConfigurationSectionList(path: String): List<AdvancedConfigurationSection> {
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

    open fun getBukkitVector(key: String, def: Vector? = null): Vector? {
        val value = getString(key, "") ?: return def
        val root = root as? AdvancedConfiguration
        val split = value.split(",").map { root?.evaluate(it)?.toString() ?: it }
        if (split.size != 3) return def
        return try {
            Vector(split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
        } catch (e: NumberFormatException) {
            def
        }
    }

    open fun getVector3f(key: String, def: Vector3f? = null): Vector3f? {
        val value = getString(key) ?: return def
        val root = root as? AdvancedConfiguration
        val split = value.split(",").map { root?.evaluate(it)?.toString() ?: it }
        return try {
            Vector3f(split[0].toFloat(), split[1].toFloat(), split[2].toFloat())
        } catch (e: NumberFormatException) {
            def
        }
    }

    inline fun <reified T: Enum<T>> getEnum(key: String, def: T? = null): T? {
        return UsefulUtility.sTry({
            enumValueOf<T>(getString(key, "")?.uppercase() ?: return@sTry def)
        }, { def })
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> getEnum(key: String, clazz: Class<T>, def: T? = null): T? {
        if (!clazz.isEnum) return def
        return UsefulUtility.sTry({
            val cast = clazz as Class<Enum<*>>
            cast.enumConstants.firstOrNull { it.name == getString(key, "")?.uppercase() } as? T
        }, { def })
    }

    open fun getStringItemStack(key: String, def: ItemStack? = null): ItemStack? {
        return UsefulUtility.sTry({
            Bukkit.getItemFactory().createItemStack(getString(key, "")!!)
        }, { def })
    }

    open fun getConfigExecute(key: String, def: Execute? = null): Execute? {
        val root = root as? AdvancedConfiguration ?: return def
        val list = getAdvancedConfigurationSectionList(key)
        if (list.isEmpty()) return def
        return Execute {
            ActionRunner.run(root, list, it.caster, it)
        }
    }

    open fun getAsyncConfigExecute(key: String, def: AsyncExecute? = null): AsyncExecute? {
        val root = root as? AdvancedConfiguration ?: return def
        val list = getAdvancedConfigurationSectionList(key)
        if (list.isEmpty()) return def
        return AsyncExecute {
            ActionRunner.run(root, list, it.caster, it, true)
        }
    }

    open fun getRotation(key: String, def: Quaternionf? = null): Quaternionf? {
        val value = getString(key) ?: return def
        val root = root as? AdvancedConfiguration
        val split = value.split(",").map { root?.evaluate(it)?.toString() ?: it }
        if (split.isEmpty()) return def
        val type = split[0].lowercase()
        try {
            when(type) {
                "euler" -> {
                    if (split.size != 4) return def
                    val x = split[1].toFloat()
                    val y = split[2].toFloat()
                    val z = split[3].toFloat()
                    return Quaternionf().rotationXYZ(x, y, z)
                }
                "axis" -> {
                    if (split.size != 5) return def
                    val angle = split[1].toFloat()
                    val x = split[2].toFloat()
                    val y = split[3].toFloat()
                    val z = split[4].toFloat()
                    return Quaternionf().rotationAxis(angle, x, y, z)
                }
                else -> {
                    if (split.size != 4 && split.size != 5) return def
                    val minus = if (split.size == 4) 1 else 0
                    val x = split[1-minus].toFloat()
                    val y = split[2-minus].toFloat()
                    val z = split[3-minus].toFloat()
                    val w = split[4-minus].toFloat()
                    return Quaternionf(x, y, z, w)
                }
            }
        } catch (e: NumberFormatException) {
            return def
        }
    }

    open fun <T> withParameters(parameters: Map<String, Any>, action: AdvancedConfigurationSection.() -> T): T {
        val root = root as? AdvancedConfiguration
        val value: T
        if (root != null) {
            val old = root.parameters
            root.parameters = parameters.toMutableMap()
            value = action.invoke(this)
            root.parameters = old
        } else {
            value = action.invoke(this)
        }

        return value
    }

    override fun toString(): String {
        return "AdvancedConfigurationSection(${getValues(true)})"
    }
}