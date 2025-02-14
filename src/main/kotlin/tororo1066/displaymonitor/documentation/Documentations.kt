package tororo1066.displaymonitor.documentation

@Target(AnnotationTarget.CLASS)
annotation class ClassDoc(
    val name: String,
    val description: String,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParameterDoc(
    val name: String,
    val description: String,
    val type: ParameterType,
    val default: String = "",
)

@Target(AnnotationTarget.PROPERTY)
annotation class ParameterTypeDoc(
    val name: String,
)

enum class ParameterType {
    @ParameterTypeDoc("文字列")
    String {
        override val example = "\"example\""
    },
    @ParameterTypeDoc("文字列のリスト")
    StringList {
        override val example = """
            - example1
            - example2
        """.trimIndent()
    },
    @ParameterTypeDoc("真偽値")
    Boolean {
        override val example = "true"
    },
    @ParameterTypeDoc("整数")
    Int {
        override val example = "1000"
    },
    @ParameterTypeDoc("整数")
    Long {
        override val example = "1000"
    },
    @ParameterTypeDoc("実数")
    Double {
        override val example = "1000.0"
    },
    @ParameterTypeDoc("実数")
    Float {
        override val example = "1000.0"
    },
    @ParameterTypeDoc("装飾可能な文字列")
    Component {
        override val example = "\"<red>Hello!</red>\""
    },
    @ParameterTypeDoc("色")
    Color {
        override val example = """
            #ff0000
        """.trimIndent()
    },
    @ParameterTypeDoc("位置")
    Location {
        override val example = """
            <world>,<x>,<y>,<z>
            <world>,<x>,<y>,<z>,<yaw>,<pitch>
            <x>,<y>,<z>
            <x>,<y>,<z>,<yaw>,<pitch>
        """.trimIndent()
    },
    @ParameterTypeDoc("ブロック")
    Block {
        override val example = """
            minecraft:oak_stairs[shape=straight]
        """.trimIndent()
    },
    @ParameterTypeDoc("アイテム")
    Item {
        override val example = """
            minecraft:diamond_sword{Enchantments:[{id:"minecraft:sharpness",lvl:5}]}
        """.trimIndent()
    },
    @ParameterTypeDoc("ベクトル")
    Vector {
        override val example = """
            <x>,<y>,<z>
        """.trimIndent()
    },
    @ParameterTypeDoc("ベクトル")
    Vector3f {
        override val example = """
            <x>,<y>,<z>
        """.trimIndent()
    },
    @ParameterTypeDoc("回転")
    Rotation {
        override val example = """
            <yaw>,<pitch>
        """.trimIndent()
    },
    @ParameterTypeDoc("Actionのリスト")
    Actions {
        override val example = """
            - class: Message
              message: "Hello!"
        """.trimIndent()
    },
    @ParameterTypeDoc("セクション")
    AdvancedConfigurationSection {
        override val example = """
            key: value
        """.trimIndent()
    },

    @ParameterTypeDoc("ビルボード")
    Billboard {
        override val example = """
            選択
            FIXED
            VERTICAL
            HORIZONTAL
            CENTER
        """.trimIndent()
    },
    @ParameterTypeDoc("輝き")
    Brightness {
        override val example = """
            block: <0~15>
            sky: <0~15>
        """.trimIndent()
    },
    @ParameterTypeDoc("アイテム表示")
    ItemDisplayTransform {
        override val example = """
            選択
            NONE
            THIRDPERSON_LEFTHAND
            THIRDPERSON_RIGHTHAND
            FIRSTPERSON_LEFTHAND
            FIRSTPERSON_RIGHTHAND
            HEAD
            GUI
            GROUND
            FIXED
        """.trimIndent()
    },
    @ParameterTypeDoc("テキストの位置")
    TextAlignment {
        override val example = """
            選択
            LEFT
            CENTER
            RIGHT
        """.trimIndent()
    },
    ;

    abstract val example: kotlin.String
}