package au.org.ala.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.Reader
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.util.*

fun Reader.loadProperties() = this.use { r -> Properties().apply { load(r) } }
fun File.loadProperties() = this.reader().loadProperties()

fun URI.readText() = this.toURL().readText()
fun URI.readText(connectTimeoutMs: Int, readTimeoutMs: Int, charSet: Charset = Charsets.UTF_8) = this.toURL().readText(connectTimeoutMs, readTimeoutMs, charSet)

fun URL.readText(connectTimeoutMs: Int, readTimeoutMs: Int, charSet: Charset = Charsets.UTF_8) = this.openConnection().apply { connectTimeout = connectTimeoutMs; readTimeout = readTimeoutMs; connect() }.getInputStream().readBytes().toString(charSet)

inline fun <reified T> T.logger(): Logger {
    val baseClass = T::class.java
    val logClass = if (baseClass.simpleName == "Companion") baseClass.enclosingClass ?: baseClass else baseClass
    return LoggerFactory.getLogger(logClass)!!
}
fun logger(name: String) = LoggerFactory.getLogger(name)!!