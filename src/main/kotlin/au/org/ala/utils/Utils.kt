package au.org.ala.utils

import org.slf4j.LoggerFactory
import java.io.File
import java.io.Reader
import java.util.*

fun String?.orNull() = if (this == null || this.isBlank()) null else this

fun Reader.loadProperties() = this.use { r -> Properties().apply{ load(r) } }
fun File.loadProperties() = this.reader().loadProperties()

inline fun <reified T> logger() = LoggerFactory.getLogger(T::class.java)!!
inline fun logger(name: String) = LoggerFactory.getLogger(name)
