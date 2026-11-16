package tororo1066.displaymonitor.documentation.generation

import java.io.File
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.Files
import java.util.jar.JarFile

class ClassScanner(
    private val classLoader: ClassLoader =
        Thread.currentThread().contextClassLoader ?: ClassScanner::class.java.classLoader,
) {
    fun scan(packageName: String): List<Class<*>> {
        require(packageName.isNotBlank()) { "Package name must not be blank" }
        val classNames = sortedSetOf<String>()
        val packagePath = packageName.replace('.', '/')

        classPathRoots().forEach { root ->
            when {
                root.isDirectory -> scanDirectory(root, packageName, classNames)
                root.isFile && root.extension.equals("jar", ignoreCase = true) ->
                    scanJar(root, packagePath, classNames)
            }
        }

        runCatching { classLoader.getResources(packagePath) }.getOrNull()?.let { resources ->
            while (resources.hasMoreElements()) {
                scanResource(resources.nextElement(), packageName, packagePath, classNames)
            }
        }

        return classNames.mapNotNull { className ->
            runCatching { Class.forName(className, false, classLoader) }
                .onFailure { error ->
                    System.err.println("GenDocs: could not load $className: ${error.message}")
                }
                .getOrNull()
        }
    }

    private fun classPathRoots(): Set<File> = buildSet {
        System.getProperty("java.class.path")
            ?.split(File.pathSeparatorChar)
            ?.filter(String::isNotBlank)
            ?.mapTo(this, ::File)

        runCatching {
            File(ClassScanner::class.java.protectionDomain.codeSource.location.toURI())
        }.getOrNull()?.let(::add)
    }

    private fun scanDirectory(root: File, packageName: String, classNames: MutableSet<String>) {
        val packageDirectory = File(root, packageName.replace('.', File.separatorChar))
        if (!packageDirectory.isDirectory) return

        Files.walk(packageDirectory.toPath()).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".class") }
                .forEach { classFile ->
                    val relativeName = packageDirectory.toPath().relativize(classFile)
                        .toString()
                        .removeSuffix(".class")
                        .replace(File.separatorChar, '.')
                    addClassName("$packageName.$relativeName", classNames)
                }
        }
    }

    private fun scanJar(jarFile: File, packagePath: String, classNames: MutableSet<String>) {
        val prefix = "$packagePath/"
        runCatching {
            JarFile(jarFile).use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entryName = entries.nextElement().name
                    if (!entryName.startsWith(prefix) || !entryName.endsWith(".class")) continue
                    addClassName(entryName.removeSuffix(".class").replace('/', '.'), classNames)
                }
            }
        }.onFailure { error ->
            System.err.println("GenDocs: could not scan ${jarFile.path}: ${error.message}")
        }
    }

    private fun scanResource(
        resource: URL,
        packageName: String,
        packagePath: String,
        classNames: MutableSet<String>,
    ) {
        when (resource.protocol) {
            "file" -> runCatching { File(resource.toURI()) }
                .getOrNull()
                ?.takeIf(File::isDirectory)
                ?.let { packageDirectory ->
                    Files.walk(packageDirectory.toPath()).use { paths ->
                        paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".class") }
                            .forEach { classFile ->
                                val relativeName = packageDirectory.toPath().relativize(classFile)
                                    .toString()
                                    .removeSuffix(".class")
                                    .replace(File.separatorChar, '.')
                                addClassName("$packageName.$relativeName", classNames)
                            }
                    }
                }

            "jar" -> runCatching { resource.openConnection() as JarURLConnection }
                .getOrNull()
                ?.let { connection ->
                    connection.useCaches = false
                    val prefix = "$packagePath/"
                    runCatching {
                        connection.jarFile.use { jar ->
                            val entries = jar.entries()
                            while (entries.hasMoreElements()) {
                                val entryName = entries.nextElement().name
                                if (entryName.startsWith(prefix) && entryName.endsWith(".class")) {
                                    addClassName(entryName.removeSuffix(".class").replace('/', '.'), classNames)
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun addClassName(name: String, classNames: MutableSet<String>) {
        if (name.contains('$') || name.endsWith(".package-info") || name == "module-info") return
        classNames += name
    }
}
