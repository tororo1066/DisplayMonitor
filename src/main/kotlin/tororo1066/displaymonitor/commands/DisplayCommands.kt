package tororo1066.displaymonitor.commands

import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.YamlConfiguration
import tororo1066.commandapi.argumentType.StringArg
import tororo1066.displaymonitor.actions.ActionRunner
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.annotation.SCommandV2Body
import tororo1066.tororopluginapi.sCommand.v2.SCommandV2
import java.io.File

class DisplayCommands: SCommandV2("dmonitor") {

    @SCommandV2Body
    val testCommand = command {

        literal("test4") {
            argument("file", StringArg(StringArg.StringType.SINGLE_WORD)) {
                setPlayerFunctionExecutor { sender, _, args ->
                    try {
                        val fileString = args.getArgument("file", String::class.java)
                        val file = File(SJavaPlugin.plugin.dataFolder, fileString)
                        if (!file.exists()) {
                            sender.sendMessage(Component.text("File not found"))
                            return@setPlayerFunctionExecutor
                        }
                        val yaml = YamlConfiguration.loadConfiguration(file)
                        ActionRunner.run(yaml, sender)
                    } catch (e: Exception) {
                        sender.sendMessage(Component.text("An error occurred"))
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}