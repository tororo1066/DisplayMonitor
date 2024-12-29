package tororo1066.displaymonitor.commands

import net.kyori.adventure.text.Component
import tororo1066.commandapi.argumentType.StringArg
import tororo1066.displaymonitor.Config
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.tororopluginapi.annotation.SCommandV2Body
import tororo1066.tororopluginapi.sCommand.v2.SCommandV2

class DisplayCommands: SCommandV2("dmonitor") {

    init {
        root.setPermission("dmonitor.op")
    }

    @SCommandV2Body
    val displayCommand = command {

        literal("runLoaded") {
            argument("name", StringArg(StringArg.StringType.SINGLE_WORD)) {

                suggest { _, _, _ ->
                    ActionStorage.loadedConfigActions.keys.map { it toolTip null }
                }

                setPlayerFunctionExecutor { sender, _, args ->
                    try {
                        val name = args.getArgument("name", String::class.java)
                        val configuration = ActionStorage.loadedConfigActions[name] ?: run {
                            sender.sendMessage(Component.text("Configuration not found"))
                            return@setPlayerFunctionExecutor
                        }
                        configuration.run(sender)
                    } catch (e: Exception) {
                        sender.sendMessage(Component.text("An error occurred"))
                        e.printStackTrace()
                    }
                }
            }
        }

        literal("reload") {
            setPlayerFunctionExecutor { sender, _, _ ->
                try {
                    Config.load()
                    ActionStorage.loadActions()
                    sender.sendMessage(Component.text("Reloaded"))
                } catch (e: Exception) {
                    sender.sendMessage(Component.text("An error occurred"))
                    e.printStackTrace()
                }
            }
        }
    }
}