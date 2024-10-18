package tororo1066.displaymonitor

import net.kyori.adventure.text.Component
import tororo1066.tororopluginapi.SJavaPlugin

object Config {

    var prefix: Component = Component.text("§b[§6Display§eMonitor§b] §r")

    fun load() {
        SJavaPlugin.plugin.reloadConfig()
        val config = SJavaPlugin.plugin.config

        prefix = config.getRichMessage("prefix", prefix)!!
    }
}