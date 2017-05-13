package com.github.pgutkowski.kgraphql.schema.dsl

import kotlin.reflect.KClass


class UnionTypeDSL(block : UnionTypeDSL.() -> Unit) : ItemDSL() {

    init {
        block()
    }

    internal val possibleTypes = mutableListOf<KClass<*>>()

    fun <T : Any>type(kClass : KClass<T>){
        possibleTypes.add(kClass)
    }

    inline fun <reified T : Any>type(){
        type(T::class)
    }
}