package me.kuku.utils

import java.util.*

object Lists {
    private val random = Random()

    @JvmStatic
    fun <T> newArrayList(vararg t: T): List<T> {
        return ArrayList(listOf(*t))
    }

    @JvmStatic
    fun <T> random(list: List<T>): T? {
        val size = list.size
        return if (size == 0) null else list[random.nextInt(size)]
    }

    @JvmStatic
    fun <E> of(vararg e: E): MutableList<E> {
        return mutableListOf(*e)
    }

    @JvmStatic
    fun <E> of(e1: E): MutableList<E> {
        return mutableListOf(e1)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E): MutableList<E> {
        return mutableListOf(e1, e2)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E): MutableList<E> {
        return mutableListOf(e1, e2, e3)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7, e8)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E, e9: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7, e8, e9)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E, e9: E, e10: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10)
    }
}

object Maps {
    @JvmStatic
    fun <T, K> of(k1: T, v1: K): MutableMap<T, K> {
        return mutableMapOf(k1 to v1)
    }

    @JvmStatic
    fun <T, K> of(k1: T, v1: K, k2: T, v2: K): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2)
    }

    @JvmStatic
    fun <T, K> of(k1: T, v1: K, k2: T, v2: K, k3: T, v3: K): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3)
    }

    @JvmStatic
    fun <T, K> of(k1: T, v1: K, k2: T, v2: K, k3: T, v3: K, k4: T, v4: K): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4)
    }

    @JvmStatic
    fun <T, K> of(k1: T, v1: K, k2: T, v2: K, k3: T, v3: K, k4: T, v4: K, k5: T, v5: K): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5)
    }

    @JvmStatic
    fun <T, K> of(k1: T, v1: K, k2: T, v2: K, k3: T, v3: K, k4: T, v4: K, k5: T, v5: K, k6: T, v6: K): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5, k6 to v6)
    }

    @JvmStatic
    fun <T, K> of(
        k1: T,
        v1: K,
        k2: T,
        v2: K,
        k3: T,
        v3: K,
        k4: T,
        v4: K,
        k5: T,
        v5: K,
        k6: T,
        v6: K,
        k7: T,
        v7: K
    ): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5, k6 to v6, k7 to v7)
    }

    @JvmStatic
    fun <T, K> of(
        k1: T,
        v1: K,
        k2: T,
        v2: K,
        k3: T,
        v3: K,
        k4: T,
        v4: K,
        k5: T,
        v5: K,
        k6: T,
        v6: K,
        k7: T,
        v7: K,
        k8: T,
        v8: K
    ): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5, k6 to v6, k7 to v7, k8 to v8)
    }

    @JvmStatic
    fun <T, K> of(
        k1: T,
        v1: K,
        k2: T,
        v2: K,
        k3: T,
        v3: K,
        k4: T,
        v4: K,
        k5: T,
        v5: K,
        k6: T,
        v6: K,
        k7: T,
        v7: K,
        k8: T,
        v8: K,
        k9: T,
        v9: K
    ): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5, k6 to v6, k7 to v7, k8 to v8, k9 to v9)
    }

    @JvmStatic
    fun <T, K> of(
        k1: T,
        v1: K,
        k2: T,
        v2: K,
        k3: T,
        v3: K,
        k4: T,
        v4: K,
        k5: T,
        v5: K,
        k6: T,
        v6: K,
        k7: T,
        v7: K,
        k8: T,
        v8: K,
        k9: T,
        v9: K,
        k10: T,
        v10: K
    ): MutableMap<T, K> {
        return mutableMapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5, k6 to v6, k7 to v7, k8 to v8, k9 to v9, k10 to v10)
    }
}
