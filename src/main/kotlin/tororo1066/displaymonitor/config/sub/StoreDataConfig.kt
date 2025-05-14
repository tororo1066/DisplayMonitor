package tororo1066.displaymonitor.config.sub

import org.bukkit.configuration.ConfigurationSection
import tororo1066.displaymonitor.config.AbstractConfig
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.database.SDatabase

class StoreDataConfig: AbstractConfig() {
    override val internalName = "StoreData"

    var database: SDatabase? = null
    var tableName: String = "StoreData"

    override fun loadConfig(config: ConfigurationSection) {
        tableName = config.getString("tableName", "StoreData")!!
        try {
            database = SDatabase.newInstance(
                SJavaPlugin.plugin,
                "config/$internalName.yml",
                null,
            )
            database?.backGroundCreateTable(
                tableName,
                mapOf(
                    "id" to SDBVariable(SDBVariable.Int, autoIncrement = true),
                    "uuid" to SDBVariable(SDBVariable.VarChar, length = 36, nullable = false, index = SDBVariable.Index.UNIQUE),
                    "name" to SDBVariable(SDBVariable.VarChar, length = 16),
                    "data" to SDBVariable(SDBVariable.Text)
                )
            )
        } catch (_: Exception) {}
    }

    override fun saveDefaultConfig(config: ConfigurationSection) {
        val section = config.createSection("database")
        section.set("type", "sqlite")
        section.set("db", "db/StoreData")
    }

}