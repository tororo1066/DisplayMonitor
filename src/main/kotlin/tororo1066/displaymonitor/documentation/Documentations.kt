package tororo1066.displaymonitor.documentation

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitorapi.configuration.AsyncExecute
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import java.lang.reflect.Field
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class ClassDoc(
    val name: String,
    val description: String,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParameterDoc(
    val name: String = "",
    val description: String,
    val type: KClass<*> = Any::class,
    val default: String = "",
)

class StringList

val parameterTypeDocs = hashMapOf<KClass<*>, ParameterType>(
    String::class to ParameterType("String", "文字列", "\"example\""),
    StringList::class to ParameterType(
        "StringList",
        "文字列のリスト",
        """
            - example1
            - example2
        """.trimIndent()
    ),
    Boolean::class to ParameterType("Boolean", "真偽値", "true/false"),
    Number::class to ParameterType("Number", "数値", "1000"),
    Int::class to ParameterType("Int", "整数", "1000"),
    Long::class to ParameterType("Long", "整数", "1000"),
    Double::class to ParameterType("Double", "実数", "1000.0"),
    Float::class to ParameterType("Float", "実数", "1000.0"),
    Component::class to ParameterType("Component", "装飾可能な文字列", "\"<red>Hello!</red>\""),
    Color::class to ParameterType(
        "Color",
        "色(ARGBまたはRGB)",
        """
            #ff0000
            #00000000
        """.trimIndent()
    ),
    Location::class to ParameterType(
        "Location",
        "位置",
        """
            <world>,<x>,<y>,<z>
            <world>,<x>,<y>,<z>,<yaw>,<pitch>
            <x>,<y>,<z>
            <x>,<y>,<z>,<yaw>,<pitch>
        """.trimIndent()
    ),
    BlockData::class to ParameterType(
        "Block",
        "ブロック",
        """
            minecraft:oak_stairs[shape=straight]
        """.trimIndent()
    ),
    ItemStack::class to ParameterType(
        "Item",
        "アイテム",
        """
            minecraft:diamond_sword{Enchantments:[{id:"minecraft:sharpness",lvl:5}]}
        """.trimIndent()
    ),
    Vector::class to ParameterType(
        "Vector",
        "ベクトル",
        """
            <x>,<y>,<z>
        """.trimIndent()
    ),
    Vector3f::class to ParameterType(
        "Vector3f",
        "ベクトル",
        """
            <x>,<y>,<z>
        """.trimIndent()
    ),
    Quaternionf::class to ParameterType(
        "Rotation",
        "回転",
        """
            euler,<x>,<y>,<z>
            axis,<angle>,<x>,<y>,<z>
            quaternion,<x>,<y>,<z>,<w>
            euler_radians,<x>,<y>,<z>
            axis_radians,<angle>,<x>,<y>,<z>
            quaternion_radians,<x>,<y>,<z>,<w>
        """.trimIndent()
    ),
    Execute::class to ParameterType(
        "Actions",
        "Actionのリスト",
        """
            - class: Message
              message: "Hello!"
        """.trimIndent()
    ),
    AsyncExecute::class to ParameterType(
        "AsyncActions",
        "非同期Actionのリスト",
        """
            - class: AsyncMessage
              message: "Hello!"
        """.trimIndent()
    ),
    IAdvancedConfigurationSection::class to ParameterType(
        "AdvancedConfigurationSection",
        "セクション",
        """
            key: value
        """.trimIndent()
    ),
)

fun getParameterType(type: KClass<*>): ParameterType {
    println("getParameterType: $type")
    val doc = parameterTypeDocs[type]
    if (doc != null) {
        return doc
    }

    if (type.java.isEnum) {
        val newType = ParameterType(
            type.simpleName ?: "Enum",
            "選択可能な値のリスト",
            type.java.enumConstants.joinToString("\n") { it.toString() }
        )
        parameterTypeDocs[type] = newType
        return newType
    }

    return ParameterType(
        "Unknown",
        "不明な型",
        "未定義"
    )
}

fun getParameterType(field: Field): ParameterType {
    val annotation = field.getAnnotation(ParameterDoc::class.java)
    val type = if (annotation != null && annotation.type != Any::class) {
        annotation.type
    } else {
        field.type.kotlin
    }
    return getParameterType(type)
}

class ParameterType(
    val name: String,
    val description: String = "",
    val example: String = ""
) {
    override fun toString(): String {
        return "ParameterTypeDoc(name='$name', description='$description', example='$example')"
    }
}