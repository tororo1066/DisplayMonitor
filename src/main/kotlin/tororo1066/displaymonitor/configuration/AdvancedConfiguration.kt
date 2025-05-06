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
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration

class AdvancedConfiguration: AdvancedConfigurationSection(), IAdvancedConfiguration {

    private inner class Options: ConfigurationOptions(this) {
        override fun pathSeparator(): Char {
            return IAdvancedConfiguration.SEPARATOR
        }
    }

    private val options = Options()
    private var parameters: MutableMap<String, Any> = mutableMapOf()

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

    override fun evaluate(value: String): Any {
        //$[key] or $[key]$ or $[key]:default$
//        Bukkit.getLogger().info("=== Evaluate ===")
//        Bukkit.getLogger().info("parameters: $parameters")
//        Bukkit.getLogger().info("value: $value")
        val replace = value.replace(Regex("\\$[a-zA-Z0-9_.]+(:[a-zA-Z0-9_.]+)?\\$?")) {
            val replace = if (it.value.endsWith("$")) it.value.substring(1, it.value.length - 1) else it.value.substring(1)
//            Bukkit.getLogger().info("Found replace: $replace")
            val split = replace.split(":")
            val key = split[0]
            val def = if (split.size == 2) split[1] else null
//            Bukkit.getLogger().info("Key $key")
//            Bukkit.getLogger().info("Value ${(parameters[key] ?: def ?: it.value)}")
            (parameters[key] ?: def ?: it.value).toString()
        }
//        Bukkit.getLogger().info("Replace: $replace")
//        Bukkit.getLogger().info("=== End ===")

        return try {
            Expression(replace).evaluate().value
        } catch (_: Exception) {
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

    override fun getParameters(): MutableMap<String, Any> {
        return parameters
    }

    override fun setParameters(parameters: MutableMap<String, Any>) {
        this.parameters = parameters
    }
}