package tororo1066.displaymonitor.config

import org.bukkit.configuration.ConfigurationSection

abstract class AbstractConfig {

    abstract val internalName: String

    abstract fun loadConfig(config: ConfigurationSection)

    abstract fun saveDefaultConfig(config: ConfigurationSection)
}