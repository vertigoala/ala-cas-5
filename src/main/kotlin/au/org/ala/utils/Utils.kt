package au.org.ala.utils

import org.slf4j.LoggerFactory
import java.io.File
import java.io.Reader
import java.net.URI
import java.net.URL
import java.util.*

fun Reader.loadProperties() = this.use { r -> Properties().apply { load(r) } }
fun File.loadProperties() = this.reader().loadProperties()

fun URI.readText() = this.toURL().readText()

inline fun <reified T> logger() = LoggerFactory.getLogger(T::class.java)!!
fun logger(name: String) = LoggerFactory.getLogger(name)!!