package com.example.examplemod

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Constants {

    const val MOD_ID: String = "examplemod"

    const val MOD_NAME: String = "ExampleMod"

    @JvmField
    val LOG: Logger = LoggerFactory.getLogger(MOD_NAME)
}