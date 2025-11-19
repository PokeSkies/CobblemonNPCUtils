package com.pokeskies.cobblemonnpcutils.placeholders

interface ServerPlaceholder {
    fun handle(args: List<String>): GenericResult
    fun id(): String
}
