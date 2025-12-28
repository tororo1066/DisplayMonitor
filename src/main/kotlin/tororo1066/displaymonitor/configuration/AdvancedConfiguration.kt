package tororo1066.displaymonitor.configuration

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationOptions
import org.bukkit.configuration.ConfigurationSection
import tororo1066.displaymonitor.configuration.expression.evalExpressionRecursive
import tororo1066.displaymonitor.storage.FunctionStorage
import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration

class AdvancedConfiguration: AdvancedConfigurationSection(), IAdvancedConfiguration {

    private inner class Options: ConfigurationOptions(this) {
        override fun pathSeparator(): Char {
            return IAdvancedConfiguration.SEPARATOR
        }
    }

    private val options = Options()
    private var parameters: MutableMap<String, Any> = mutableMapOf()
    private var publicContext: IPublicActionContext? = null

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
        return try {
//            SJavaPlugin.plugin.logger.info("evaluating: $value")
            val parameters = this.parameters.toMutableMap().apply {
                publicContext?.getParameters()?.let { putAll(it) }
            }
            val v = evalExpressionRecursive(value, parameters, FunctionStorage.functions)
//            SJavaPlugin.plugin.logger.info("evaluate: $value -> $v")
            v
        } catch (_: Exception) {
            value
        }
    }

    override fun clone(): AdvancedConfiguration {
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

    override fun getPublicContext(): IPublicActionContext? {
        return publicContext
    }

    override fun setPublicContext(publicContext: IPublicActionContext?) {
        this.publicContext = publicContext
    }
}