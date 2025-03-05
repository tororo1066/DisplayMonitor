package tororo1066.displaymonitor.commands

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import tororo1066.commandapi.CommandArguments
import tororo1066.commandapi.argumentType.EntityArg
import tororo1066.commandapi.argumentType.StringArg
import tororo1066.displaymonitor.Config
import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.PublicActionContext
import tororo1066.displaymonitor.storage.ActionStorage
import tororo1066.tororopluginapi.SInput
import tororo1066.tororopluginapi.annotation.SCommandV2Body
import tororo1066.tororopluginapi.sCommand.v2.SCommandV2

class DisplayCommands: SCommandV2("dmonitor") {

    val gson = Gson()

    init {
        root.setPermission("dmonitor.op")
    }

    private fun jsonToMap(json: JsonObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        json.entrySet().forEach {
            if (it.value.isJsonObject) {
                map[it.key] = jsonToMap(it.value.asJsonObject)
            } else {
                map[it.key] = it.value.asString
            }
        }
        return map
    }

    @SCommandV2Body
    val displayCommand = command {

        fun runLoaded(sender: CommandSender, args: CommandArguments) {
            try {
                val actionName = args.getNullableArgument("actionName", String::class.java)
                if (ActionStorage.contextByName.containsKey(actionName)) {
                    sender.sendMessage(Component.text("Action is already running"))
                    return
                }

                val name = args.getArgument("name", String::class.java)
                val configuration = ActionStorage.loadedConfigActions[name] ?: run {
                    sender.sendMessage(Component.text("Configuration not found"))
                    return
                }
                val context = ActionContext(PublicActionContext())
                val caster = args.getEntities("caster").firstOrNull()
                val target = args.getEntities("target").firstOrNull()
                val location = args.getNullableArgument("location", String::class.java)
                    ?.let { SInput.modifyClassValue(Location::class.java, it).second }
                if (caster == null && target == null) {
                    if (sender is Entity) {
                        context.caster = sender
                        context.target = sender
                    } else {
                        sender.sendMessage(Component.text("Caster or target is required"))
                        return
                    }
                } else {
                    if (caster != null && target == null) {
                        context.caster = caster
                        context.target = caster
                    } else {
                        context.caster = caster
                        context.target = target
                    }
                }
                if (location != null) {
                    context.location = location
                } else {
                    context.target?.let {
                        context.location = it.location
                    } ?: run {
                        context.caster?.let {
                            context.location = it.location
                        } ?: run {
                            sender.sendMessage(Component.text("Location is required"))
                        }
                        return
                    }
                }

                val parameters = args.getNullableArgument("parameters", String::class.java)
                    ?.let { gson.fromJson(it, JsonObject::class.java) }
                    ?.let { jsonToMap(it) }
                if (parameters != null) {
                    context.prepareParameters.putAll(parameters)
                }

                configuration.run(context, async = true, actionName)
            } catch (e: Exception) {
                sender.sendMessage(Component.text("An error occurred"))
                e.printStackTrace()
            }
        }

        literal("runLoaded") {

            argument("name", StringArg(StringArg.StringType.SINGLE_WORD)) {

                suggest { _, _, _ ->
                    ActionStorage.loadedConfigActions.keys.map { it toolTip null }
                }

                setPlayerFunctionExecutor { sender, _, args ->
                    runLoaded(sender, args)
                }

                argument("caster", EntityArg(singleTarget = true, playersOnly = false)) {
                    setPlayerFunctionExecutor { sender, _, args ->
                        runLoaded(sender, args)
                    }

                    argument("target", EntityArg(singleTarget = true, playersOnly = false)) {
                        setPlayerFunctionExecutor { sender, _, args ->
                            runLoaded(sender, args)
                        }

                        argument("location", StringArg(StringArg.StringType.QUOTABLE_PHRASE)) {
                            setPlayerFunctionExecutor { sender, _, args ->
                                runLoaded(sender, args)
                            }

                            argument("parameters", StringArg(StringArg.StringType.QUOTABLE_PHRASE)) {
                                setPlayerFunctionExecutor { sender, _, args ->
                                    runLoaded(sender, args)
                                }

                                argument("actionName", StringArg(StringArg.StringType.SINGLE_WORD)) {
                                    setPlayerFunctionExecutor { sender, _, args ->
                                        runLoaded(sender, args)
                                    }
                                }
                            }
                        }
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