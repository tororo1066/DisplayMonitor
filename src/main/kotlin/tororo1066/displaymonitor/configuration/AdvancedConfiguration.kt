package tororo1066.displaymonitor.configuration

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationOptions
import org.bukkit.configuration.ConfigurationSection

class AdvancedConfiguration: AdvancedConfigurationSection(), Configuration {

    private inner class Options: ConfigurationOptions(this)

    private val options = Options()

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


}