package tororo1066.displaymonitor.elements

import net.kyori.adventure.text.Component
import org.apache.commons.lang3.reflect.FieldUtils
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Display
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitorapi.configuration.AsyncExecute
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.ISettableProcessor
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import java.lang.reflect.Field
import java.util.IdentityHashMap
import java.util.function.BiFunction
import java.util.function.Function

object SettableProcessor: ISettableProcessor {

    private val fieldCache = IdentityHashMap<Class<*>, List<Field>>()
    private val customVariableProcessors = mutableMapOf<Class<*>, Function<Any, Map<String, Any>>>()
    private val customValueProcessors = mutableMapOf<Class<*>, BiFunction<IAdvancedConfigurationSection, String, Any>>()

    override fun getCustomVariableProcessors(): MutableMap<Class<*>, Function<Any, Map<String, Any>>> {
        return customVariableProcessors
    }

    override fun getCustomValueProcessors(): MutableMap<Class<*>, BiFunction<IAdvancedConfigurationSection, String, Any>> {
        return customValueProcessors
    }

    override fun processVariable(value: Any?): Map<String, Any> {
        if (value == null) return emptyMap()
        customVariableProcessors[value::class.java]?.let {
            return it.apply(value)
        }
        when(value) {
            is Vector -> {
                return mapOf(
                    "base.x" to value.x,
                    "base.y" to value.y,
                    "base.z" to value.z
                )
            }
            is Vector3f -> {
                return mapOf(
                    "base.x" to value.x,
                    "base.y" to value.y,
                    "base.z" to value.z
                )
            }
            is Quaternionf -> {
                return mapOf(
                    "base.quaternion.x" to value.x,
                    "base.quaternion.y" to value.y,
                    "base.quaternion.z" to value.z,
                    "base.quaternion.w" to value.w,
                    "base.euler.x" to value.getEulerAnglesXYZ(Vector3f()).x,
                    "base.euler.y" to value.getEulerAnglesXYZ(Vector3f()).y,
                    "base.euler.z" to value.getEulerAnglesXYZ(Vector3f()).z,
                    "base.axis.angle" to value.angle(),
                    "base.axis.x" to value.x,
                    "base.axis.y" to value.y,
                    "base.axis.z" to value.z
                )
            }
//            is Location -> {
//                return mapOf(
//                    "base.world" to (value.world?.name ?: ""),
//                    "base.x" to value.x,
//                    "base.y" to value.y,
//                    "base.z" to value.z,
//                    "base.yaw" to value.yaw,
//                    "base.pitch" to value.pitch
//                )
//            }
            else -> {
                return mapOf(
                    "base" to value.toString()
                )
            }
        }
    }

    override fun <T : Any> processValue(
        configuration: IAdvancedConfigurationSection,
        key: String,
        clazz: Class<T>
    ): T? {
        return configuration.processValue(key, clazz)
    }

    @JvmName("processValue2")
    @Suppress("UNCHECKED_CAST")
    fun <Type: Any> IAdvancedConfigurationSection.processValue(key: String, clazz: Class<Type>): Type? {
        customValueProcessors[clazz]?.let {
            return it.apply(this, key) as? Type
        }

        if (clazz.isEnum) {
            fun getEnum(key: String, clazz: Class<Enum<*>>): Any? {
                return this.getString(key)?.let {
                    UsefulUtility.sTry({
                        clazz.enumConstants.first { enum -> enum.name.equals(it, true) }
                    }, { null })
                }
            }
            return getEnum(key, clazz as Class<Enum<*>>) as? Type
        }

        when(clazz) {
            Vector::class.java -> {
                return this.getBukkitVector(key) as? Type
            }

            Vector3f::class.java -> {
                return this.getVector3f(key) as? Type
            }

            Quaternionf::class.java -> {
                return this.getRotation(key) as? Type
            }

            Execute::class.java -> {
                return this.getConfigExecute(key) as? Type
            }

            AsyncExecute::class.java -> {
                return this.getAsyncConfigExecute(key) as? Type
            }

            ItemStack::class.java -> {
                return this.getStringItemStack(key) as? Type
            }

            BlockData::class.java -> {
                return UsefulUtility.sTry({
                    Bukkit.createBlockData(getString(key, "")!!)
                }, { null }) as? Type
            }

            Component::class.java -> {
                return this.getRichMessage(key) as? Type
            }

            Color::class.java -> {
                return this.getString(key)?.let { Utils.hexToBukkitColor(it) } as? Type
            }

            Display.Brightness::class.java -> {
                val section = this.getAdvancedConfigurationSection(key) ?: return null
                return Display.Brightness(
                    section.getInt("block", 0),
                    section.getInt("sky", 0)
                ) as? Type
            }

            Location::class.java -> {
                return this.getLocation(key) as? Type
            }

            else -> {
                return this.get(key) as? Type
            }
        }
    }

    @JvmName("getSettableFields2")
    fun Class<*>.getSettableFields(): List<Field> {
        return fieldCache.getOrPut(this) {
            FieldUtils.getAllFieldsList(this)
        }.filter {
            it.isAnnotationPresent(Settable::class.java)
        }
    }

    override fun getSettableFields(clazz: Class<*>): List<Field> {
        return clazz.getSettableFields()
    }
}