package au.org.ala.utils

import com.google.common.io.BaseEncoding
import org.apereo.cas.util.serialization.StringSerializer
import java.io.*

class JdkStringSerializer<T : Serializable> : StringSerializer<T> {

    companion object {
        private val base64 = BaseEncoding.base64()
    }

    override fun from(reader: Reader): T? =
        ObjectInputStream(base64.decodingStream(reader)).use(ObjectInputStream::readObject) as? T

    override fun from(stream: InputStream): T? = from(stream.reader())

    override fun from(file: File): T? = from(file.inputStream())

    override fun from(writer: Writer): T? = from(writer.toString())

    override fun from(data: String): T? = from(data.reader())

    override fun to(out: OutputStream, `object`: T) {
        to(out.writer(), `object`)
    }

    override fun to(out: Writer, `object`: T) {
        ObjectOutputStream(base64.encodingStream(out)).use { it.writeObject(`object`); it.flush() }
    }

    override fun to(out: File, `object`: T) {
        to(out.writer(), `object`)
    }

    override fun toString(`object`: T) =
        StringWriter().apply {
            to(this, `object`)
        }.toString()

}