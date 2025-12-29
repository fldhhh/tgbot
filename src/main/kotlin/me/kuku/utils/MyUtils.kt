@file:Suppress("unused")

package me.kuku.utils

import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.*
import java.util.jar.JarFile
import kotlin.collections.HashMap
import kotlin.random.Random

object MyUtils {

    private val regexMap = mutableMapOf<String, Regex>()

    @JvmStatic
    fun regex(regex: String, text: String): String? {
        val regexObj = regexMap.containsKey(regex)
                .takeIf { it }
                ?.let { regexMap[regex] }
                ?: Regex(regex).also { regexMap[regex] = it }
        return regexObj.find(text)?.value
    }

    @JvmStatic
    fun regexOrFail(regex: String, text: String): String {
        return regex(regex, text) ?: error("regex result not found")
    }

    @JvmStatic
    fun regexGroup(regex: String, text: String): List<String> {
        val regexObj = regexMap.containsKey(regex).takeIf { it }?.let { regexMap[regex] } ?: Regex(regex).also {
            regexMap[regex] = it
        }
        val result = regexObj.findAll(text)
        val list = mutableListOf<String>()
        result.forEach { list.add(it.value) }
        return list
    }

    @JvmStatic
    fun regex(first: String, last: String, text: String): String? {
        return regex("(?<=$first).*?(?=$last)", text)
    }

    private fun random(str: String, length: Int): String {
        val result = StringBuilder()
        for (i in 0 until length) {
            val at = Random.nextInt(str.length)
            result.append(str[at])
        }
        return result.toString()
    }

    @JvmStatic
    fun random(length: Int): String {
        return random("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789", length)
    }

    @JvmStatic
    fun randomNum(length: Int): String {
        return random("1234567890", length)
    }

    @JvmStatic
    fun randomLong(min: Long, max: Long): Long {
//        return (Math.random() * max).toLong() % (max - min + 1) + min
        return Random.nextLong(min, max)
    }

    @JvmStatic
    fun randomInt(min: Int, max: Int): Int {
//        return (Math.random() * max).toInt() % (max - min + 1) + min
        return Random.nextInt(min, max)
    }

    @JvmStatic
    fun randomLetter(length: Int): String {
        return random("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", length)
    }

    @JvmStatic
    fun randomLetterLower(length: Int): String {
        return random("abcdefghijklmnopqrstuvwxyz", length)
    }

    @JvmStatic
    fun randomLetterUpper(length: Int): String {
        return random("ABCDEFGHIJKLMNOPQRSTUVWXYZ", length)
    }

    @JvmStatic
    fun randomLetterLowerNum(length: Int): String {
        return random("1234567890abcdefghijklmnopqrstuvwxyz", length)
    }

    @JvmStatic
    fun randomLetterUpperNum(length: Int): String {
        return random("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ", length)
    }

    @JvmStatic
    fun randomLetterNum(length: Int): String {
        return random("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", length)
    }

    @JvmStatic
    fun getClasses(pn: String): Map<String, Class<*>> {
        var packageName = pn
        val classes = HashMap<String, Class<*>>()
        //List<Class<?>> classes = new ArrayList<>();
        val recursive = true
        val packageDirName = packageName.replace('.', '/')
        val dirs: Enumeration<URL>
        try {
            dirs = Thread.currentThread().contextClassLoader.getResources(packageDirName)
            while (dirs.hasMoreElements()) {
                val url = dirs.nextElement()

                val protocol = url.protocol
                if ("file" == protocol) {
                    val filePath = URLDecoder.decode(url.file, "UTF-8")
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes)
                } else if ("jar" == protocol) {
                    var jar: JarFile
                    try {
                        jar = (url.openConnection() as JarURLConnection).jarFile
                        val entries = jar.entries()
                        while (entries.hasMoreElements()) {
                            val entry = entries.nextElement()
                            var name = entry.name
                            if (name[0] == '/') {
                                name = name.substring(1)
                            }
                            if (name.startsWith(packageDirName)) {
                                val idx = name.lastIndexOf('/')
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.')
                                }
                                if (idx != -1 || recursive) {
                                    if (name.endsWith(".class") && !entry.isDirectory) {
                                        val className = name.substring(packageName.length + 1, name.length - 6)
                                        try {
                                            val clazz = Thread.currentThread().contextClassLoader.loadClass("$packageName.$className")
                                            classes.putIfAbsent(clazz.name, clazz)
                                        } catch (e: ClassNotFoundException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return classes
    }

    @JvmStatic
    fun findAndAddClassesInPackageByFile(packageName: String, packagePath: String, recursive: Boolean, classes: MutableMap<String, Class<*>>) {
        val dir = File(packagePath)
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        val dirFiles = dir.listFiles { file: File -> recursive && file.isDirectory || file.name.endsWith(".class") }
        if (dirFiles != null) {
            for (file in dirFiles) {
                if (file.isDirectory) {
                    findAndAddClassesInPackageByFile(
                        packageName + "." + file.name,
                        file.absolutePath,
                        recursive,
                        classes
                    )
                } else {
                    val className = file.name.substring(0, file.name.length - 6)
                    try {
                        val clazz = Class.forName("$packageName.$className")
                        classes.putIfAbsent(clazz.name, clazz)
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}
