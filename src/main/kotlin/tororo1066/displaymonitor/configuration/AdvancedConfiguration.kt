package tororo1066.displaymonitor.configuration

import com.ezylang.evalex.Expression
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationOptions
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.Utils

class AdvancedConfiguration: AdvancedConfigurationSection(), Configuration, Cloneable {

    private inner class Options: ConfigurationOptions(this)

    private val options = Options()
    var parameters: MutableMap<String, Any> = mutableMapOf()

    override fun getParent(): ConfigurationSection? {
        return null
    }

    override fun addDefaults(defaults: MutableMap<String, Any>) {
    }

    override fun addDefaults(defaults: Configuration) {
    }

    override fun setDefaults(defaults: Configuration) {
    }

    override fun getDefaults(): Configuration? {
        return null
    }

    override fun options(): ConfigurationOptions {
        return options
    }

    fun evaluate(value: String): Any {
        //$[key] or $[key]$ or $[key]:default$

        val replace = value.replace(Regex("\\$[a-zA-Z0-9_.]+(:[a-zA-Z0-9_.]+)?\\$?")) {
            val replace = if (it.value.endsWith("$")) it.value.substring(1, it.value.length - 1) else it.value.substring(1)
            val split = replace.split(":")
            val key = split[0]
            val def = if (split.size == 2) split[1] else null
            (parameters[key] ?: def ?: it.value).toString()
        }

        return try {
            Expression(replace).evaluate().value
        } catch (e: Exception) {
            replace
        }
    }

    public override fun clone(): AdvancedConfiguration {
        val clone = AdvancedConfiguration()
        clone.parameters = parameters.toMutableMap()
        getValues(true).forEach { (key, value) ->
            clone.set(key, value)
        }
        return clone
    }
}