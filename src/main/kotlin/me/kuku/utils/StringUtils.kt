@file:Suppress("unused")

package me.kuku.utils

object StringUtils {
    @JvmStatic
    fun isEmpty(str: String?): Boolean {
        return str?.isEmpty() ?: true
    }

    @JvmStatic
    fun isNotEmpty(str: String?): Boolean {
        return str?.isNotEmpty() ?: false
    }

    @JvmStatic
    fun join(collection: Collection<*>, separator: String): String {
        return collection.joinToString(separator)
    }

    @JvmStatic
    fun join(collection: Collection<*>, separator: Char): String {
        return collection.joinToString(separator.toString())
    }

    @JvmStatic
    fun join(iterable: Iterable<*>, separator: String): String {
        return iterable.joinToString(separator)
    }

    @JvmStatic
    fun join(iterable: Iterable<*>, separator: Char): String {
        return iterable.joinToString(separator.toString())
    }

}