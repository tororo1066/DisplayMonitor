package tororo1066.displaymonitor.configuration

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationOptions
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConstructor
import org.bukkit.configuration.file.YamlRepresenter
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.comments.CommentLine
import org.yaml.snakeyaml.comments.CommentType
import org.yaml.snakeyaml.error.YAMLException
import org.yaml.snakeyaml.nodes.AnchorNode
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.SequenceNode
import org.yaml.snakeyaml.reader.UnicodeReader
import tororo1066.displaymonitor.configuration.expression.evalExpressionRecursive
import tororo1066.displaymonitor.storage.FunctionStorage
import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

class AdvancedConfiguration: AdvancedConfigurationSection(), IAdvancedConfiguration {

    private inner class Options: ConfigurationOptions(this) {
        override fun pathSeparator(): Char {
            return IAdvancedConfiguration.SEPARATOR
        }
    }

    private val options = Options()
    private var parameters: MutableMap<String, Any> = mutableMapOf()
    private var publicContext: IPublicActionContext? = null

    private val yamlDumperOptions: DumperOptions = DumperOptions()
    private val yamlLoaderOptions: LoaderOptions = LoaderOptions()
    private val constructor: YamlConstructor
    private val representer: YamlRepresenter
    private val yaml: Yaml

    init {
        yamlDumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        yamlLoaderOptions.maxAliasesForCollections = Int.MAX_VALUE
        yamlLoaderOptions.codePointLimit = Int.MAX_VALUE

        constructor = YamlConstructor(yamlLoaderOptions)
        representer = YamlRepresenter(yamlDumperOptions)
        representer.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK

        yaml = Yaml(constructor, representer, yamlDumperOptions, yamlLoaderOptions)
    }

    override fun getParent(): ConfigurationSection? {
        return null
    }

    override fun addDefaults(defaults: MutableMap<String, Any>) {
    }

    override fun addDefaults(defaults: Configuration) {
    }

    override fun setDefaults(defaults: Configuration) {
    }

    override fun getDefaults(): Configuration? {
        return null
    }

    override fun options(): ConfigurationOptions {
        return options
    }

    override fun evaluate(value: String): Any {
        return try {
            val parameters = this.parameters.toMutableMap().apply {
                publicContext?.getParameters()?.let { putAll(it) }
            }
            evalExpressionRecursive(value, parameters, FunctionStorage.functions)
        } catch (_: Exception) {
            value
        }
    }

    override fun clone(): AdvancedConfiguration {
        val clone = AdvancedConfiguration()
        clone.parameters = parameters.toMutableMap()
        getValues(true).forEach { (key, value) ->
            clone.set(key, value)
        }
        return clone
    }

    override fun getParameters(): MutableMap<String, Any> {
        return parameters
    }

    override fun setParameters(parameters: MutableMap<String, Any>) {
        this.parameters = parameters
    }

    override fun getPublicContext(): IPublicActionContext? {
        return publicContext
    }

    override fun setPublicContext(publicContext: IPublicActionContext?) {
        this.publicContext = publicContext
    }

    override fun load(file: File) {
        try {
            UnicodeReader(file.inputStream().buffered()).use { reader ->
                val contents = reader.readText()
                loadFromString(contents)
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException(e)
        }
    }

    override fun loadFromString(contents: String) {
//        yamlLoaderOptions.setProcessComments(options().parseComments())
//        yamlLoaderOptions.setCodePointLimit(options().codePointLimit()) // Paper

        val node: MappingNode?
        try {
            UnicodeReader(ByteArrayInputStream(contents.toByteArray(StandardCharsets.UTF_8))).use { reader ->
                val rawNode = yaml.compose(reader)
                try {
                    node = rawNode as MappingNode?
                } catch (_: ClassCastException) {
                    throw InvalidConfigurationException("Top level is not a Map.")
                }
            }
        } catch (e: YAMLException) {
            throw InvalidConfigurationException(e)
        } catch (e: IOException) {
            throw InvalidConfigurationException(e)
        } catch (e: ClassCastException) {
            throw InvalidConfigurationException(e)
        }

        this.map.clear()

        if (node != null) {
            adjustNodeComments(node)
//            options().setHeader(loadHeader(getCommentLines(node.getBlockComments())))
//            options().setFooter(getCommentLines(node.getEndComments()))
            fromNodeTree(node, this)
        }
    }

    private fun adjustNodeComments(node: MappingNode) {
        if (node.blockComments == null && !node.value.isEmpty()) {
            val firstNode = node.value[0].keyNode
            val lines = firstNode.blockComments
            if (lines != null) {
                var index = -1
                for (i in lines.indices) {
                    if (lines[i].commentType == CommentType.BLANK_LINE) {
                        index = i
                    }
                }
                if (index != -1) {
                    node.blockComments = lines.subList(0, index + 1)
                    firstNode.blockComments = lines.subList(index + 1, lines.size)
                }
            }
        }
    }

    private fun fromNodeTree(input: MappingNode, section: ConfigurationSection) {
        constructor.flattenMapping(input)
        for (nodeTuple in input.value) {
            val key = nodeTuple.keyNode
            val keyString = constructor.construct(key).toString()
            var value = nodeTuple.valueNode

            while (value is AnchorNode) {
                value = value.realNode
            }

            if (value is MappingNode && !hasSerializedTypeKey(value)) {
                fromNodeTree(value, section.createSection(keyString))
            } else {
                section.set(keyString, constructor.construct(value))
            }

            section.setComments(keyString, getCommentLines(key.blockComments))
            if (value is MappingNode || value is SequenceNode) {
                section.setInlineComments(keyString, getCommentLines(key.inLineComments))
            } else {
                section.setInlineComments(keyString, getCommentLines(value.inLineComments))
            }
        }
    }

    private fun hasSerializedTypeKey(node: MappingNode): Boolean {
        for (nodeTuple in node.value) {
            val keyNode = nodeTuple.keyNode
            if (keyNode !is ScalarNode) continue
            val key = keyNode.value
            if (key == ConfigurationSerialization.SERIALIZED_TYPE_KEY) {
                return true
            }
        }
        return false
    }

    private fun getCommentLines(comments: MutableList<CommentLine>?): MutableList<String?> {
        val lines = arrayListOf<String?>()
        if (comments != null) {
            for (comment in comments) {
                if (comment.commentType == CommentType.BLANK_LINE) {
                    lines.add(null)
                } else {
                    var line = comment.value
                    line = if (line.startsWith(" ")) line.substring(1) else line
                    lines.add(line)
                }
            }
        }
        return lines
    }

    companion object {
        fun loadFromString(contents: String): AdvancedConfiguration {
            val config = AdvancedConfiguration()
            config.loadFromString(contents)
            return config
        }
    }
}