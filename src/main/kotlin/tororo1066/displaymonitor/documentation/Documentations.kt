package tororo1066.displaymonitor.documentation

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Display
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitorapi.configuration.AsyncExecute
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.CustomSettable
import java.util.Collections
import java.util.UUID
import java.lang.reflect.Field
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class ClassDoc(
    val name: String,
    val description: String,
)

@Retention(AnnotationRetention.RUNTIME)
annotation class ParameterItemDoc(
    val name: String,
    val description: String = "",
    val type: KClass<*> = Any::class,
    val required: Boolean = false,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParameterDoc(
    val name: String = "",
    val description: String,
    val type: KClass<*> = Any::class,
    val default: String = "",
    val additionalTypes: Array<KClass<*>> = [],
    val allowExpression: Boolean = true,
    val items: Array<ParameterItemDoc> = [],
    val itemRequiredAnyOf: Array<String> = [],
)

class StringList
class ObjectList

private val builtInParameterTypeDocs = linkedMapOf(
    Any::class to ParameterType(
        "Any",
        "型を限定しない値",
        "文字列、数値、真偽値、リスト、セクションなど",
        ConfigurationType.Any,
    ),
    String::class to ParameterType("String", "文字列", "\"example\"", ConfigurationType.String),
    StringList::class to ParameterType(
        "StringList",
        "文字列のリスト",
        """
            - example1
            - example2
        """.trimIndent(),
        ConfigurationType.Array(ConfigurationType.String, acceptInlineString = true),
    ),
    ObjectList::class to ParameterType(
        "ObjectList",
        "オブジェクトのリスト",
        """
            - key: example
              value: 1
        """.trimIndent(),
        ConfigurationType.Array(ConfigurationType.Object),
    ),
    Display.Brightness::class to ParameterType(
        "Brightness",
        "Display Entity のブロック光と空の明るさ。",
        """
            block: 15
            sky: 15
        """.trimIndent(),
        ConfigurationType.Record(
            properties = listOf(
                ConfigurationProperty(
                    name = "block",
                    type = ConfigurationType.Expression(ConfigurationType.Integer),
                    description = "ブロック光源の明るさ。",
                ),
                ConfigurationProperty(
                    name = "sky",
                    type = ConfigurationType.Expression(ConfigurationType.Integer),
                    description = "空の明るさ。",
                ),
            )
        ),
    ),
    UUID::class to ParameterType(
        "UUID",
        "UUID",
        "00000000-0000-0000-0000-000000000000",
        ConfigurationType.String,
    ),
    Boolean::class to ParameterType(
        "Boolean", "真偽値", "true/false", ConfigurationType.Expression(ConfigurationType.Boolean)
    ),
    Number::class to ParameterType(
        "Number", "数値", "1000", ConfigurationType.Expression(ConfigurationType.Number)
    ),
    Int::class to ParameterType(
        "Int", "整数", "1000", ConfigurationType.Expression(ConfigurationType.Integer)
    ),
    Long::class to ParameterType(
        "Long", "整数", "1000", ConfigurationType.Expression(ConfigurationType.Integer)
    ),
    Double::class to ParameterType(
        "Double", "実数", "1000.0", ConfigurationType.Expression(ConfigurationType.Number)
    ),
    Float::class to ParameterType(
        "Float", "実数", "1000.0", ConfigurationType.Expression(ConfigurationType.Number)
    ),
    Component::class to ParameterType(
        "Component", "装飾可能な文字列(MiniMessage)", "\"<red>Hello!</red>\"", ConfigurationType.String
    ),
    Color::class to ParameterType(
        "Color",
        "色(ARGBまたはRGB)",
        """
            #ff0000
            #00000000
        """.trimIndent(),
        ConfigurationType.String,
    ),
    Location::class to ParameterType(
        "Location",
        "位置",
        """
            <world>,<x>,<y>,<z>
            <world>,<x>,<y>,<z>,<yaw>,<pitch>
            <x>,<y>,<z>
            <x>,<y>,<z>,<yaw>,<pitch>
        """.trimIndent(),
        ConfigurationType.String,
    ),
    BlockData::class to ParameterType(
        "Block",
        "ブロック",
        """
            minecraft:oak_stairs[shape=straight]
        """.trimIndent(),
        ConfigurationType.String,
    ),
    ItemStack::class to ParameterType(
        "ItemStack",
        "アイテム",
        """
            minecraft:diamond_sword{Enchantments:[{id:"minecraft:sharpness",lvl:5}]}
            minecraft:player_head[profile="tororo_1066"] (1.20.5以降)
        """.trimIndent(),
        ConfigurationType.String,
    ),
    Vector::class to ParameterType(
        "Vector",
        "ベクトル",
        """
            <x>,<y>,<z>
        """.trimIndent(),
        ConfigurationType.String,
    ),
    Vector3f::class to ParameterType(
        "Vector3f",
        "ベクトル",
        """
            <x>,<y>,<z>
        """.trimIndent(),
        ConfigurationType.String,
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
        """.trimIndent(),
        ConfigurationType.String,
    ),
    Execute::class to ParameterType(
        "Actions",
        "Actionのリスト",
        """
            - class: Message
              message: "Hello!"
        """.trimIndent(),
        ConfigurationType.Array(
            ConfigurationType.Ref(DocumentationRef.ACTION_ITEM)
        ),
    ),
    AsyncExecute::class to ParameterType(
        "AsyncActions",
        "非同期Actionのリスト",
        """
            - class: Message
              message: "Hello!"
        """.trimIndent(),
        ConfigurationType.Array(
            ConfigurationType.Ref(DocumentationRef.ACTION_ITEM)
        ),
    ),
    IAdvancedConfigurationSection::class to ParameterType(
        "AdvancedConfigurationSection",
        "セクション",
        """
            key: value
        """.trimIndent(),
        ConfigurationType.Object,
    ),
)

val parameterTypeDocs: Map<KClass<*>, ParameterType> =
    Collections.unmodifiableMap(builtInParameterTypeDocs)

fun getParameterType(type: KClass<*>): ParameterType {
    val doc = parameterTypeDocs[type]
    if (doc != null) {
        return doc
    }

    if (type.java.isEnum) {
        return ParameterType(
            type.simpleName ?: "Enum",
            "選択可能な値のリスト",
            type.java.enumConstants.joinToString("\n") { it.toString() },
            ConfigurationType.Expression(
                ConfigurationType.Enum(type.java.enumConstants.map { (it as Enum<*>).name })
            ),
        )
    }

    if (CustomSettable::class.java.isAssignableFrom(type.java)) {
        return ParameterType(
            type.simpleName ?: "CustomSettable",
            "設定項目を持つセクション",
            "key: value",
            ConfigurationType.Object,
        )
    }

    if (Iterable::class.java.isAssignableFrom(type.java) || type.java.isArray) {
        return ParameterType(
            "Array",
            "リスト",
            "[]",
            ConfigurationType.Array(ConfigurationType.Any),
        )
    }

    if (Map::class.java.isAssignableFrom(type.java)) {
        return ParameterType(
            "Object",
            "セクション",
            "key: value",
            ConfigurationType.Object,
        )
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
    val typeDoc = getParameterType(type)
    var configurationType = typeDoc.configurationType

    if (annotation?.allowExpression == false && type.java.isEnum) {
        configurationType = ConfigurationType.Enum(
            type.java.enumConstants
                .map { (it as Enum<*>).name }
        )
    }

    if (annotation?.items?.isNotEmpty() == true) {
        val properties = annotation.items.map { item ->
            ConfigurationProperty(
                name = item.name,
                description = item.description,
                type = getParameterType(item.type).configurationType,
                required = item.required,
            )
        }
        configurationType = ConfigurationType.Array(
            ConfigurationType.Record(
                properties = properties,
                requiredAnyOf = annotation.itemRequiredAnyOf.toList(),
            )
        )
    }

    val additionalTypes = annotation?.additionalTypes.orEmpty()
        .map { getParameterType(it).configurationType }
    if (additionalTypes.isNotEmpty()) {
        configurationType = (listOf(configurationType) + additionalTypes)
        .flatMap(ConfigurationType::alternatives)
        .distinct()
            .let(ConfigurationType::AnyOf)
    }

    return ParameterType(typeDoc.name, typeDoc.description, typeDoc.example, configurationType)
}

class ParameterType(
    val name: String,
    val description: String = "",
    val example: String = "",
    val configurationType: ConfigurationType = ConfigurationType.Any
) {
    override fun toString(): String {
        return "ParameterTypeDoc(name='$name', description='$description', example='$example')"
    }
}

sealed interface ConfigurationType {
    data object Any : ConfigurationType
    data object String : ConfigurationType
    data object Boolean : ConfigurationType
    data object Integer : ConfigurationType
    data object Number : ConfigurationType
    data object Object : ConfigurationType

    data class AnyOf(val alternatives: List<ConfigurationType>) : ConfigurationType

    data class Record(
        val properties: List<ConfigurationProperty>,
        val requiredAnyOf: List<kotlin.String> = emptyList(),
        val additionalProperties: kotlin.Boolean = true,
    ) : ConfigurationType

    data class Enum(val values: List<kotlin.String>) : ConfigurationType

    data class Expression(val value: ConfigurationType) : ConfigurationType

    data class Array(
        val item: ConfigurationType,
        val acceptInlineString: kotlin.Boolean = false
    ) : ConfigurationType

    data class Ref(val target: DocumentationRef) : ConfigurationType
}

data class ConfigurationProperty(
    val name: String,
    val type: ConfigurationType,
    val description: String = "",
    val required: Boolean = false,
)

private fun ConfigurationType.alternatives(): List<ConfigurationType> = when (this) {
    is ConfigurationType.AnyOf -> alternatives.flatMap(ConfigurationType::alternatives)
    else -> listOf(this)
}
