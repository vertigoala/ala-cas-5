package au.org.ala.cas

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {

    @Test
    fun setSingleAttributeValue() {
        val map = mutableMapOf<String, Any?>("a" to "b", "c" to mutableListOf(""), "d" to mutableSetOf<String>())
        map.setSingleAttributeValue("a", "c")
        map.setSingleAttributeValue("b", "b")
        map.setSingleAttributeValue("c", "d")
        map.setSingleAttributeValue("d", "e")

        assertEquals("c", map["a"])
        assertEquals(listOf("b"), map["b"])
        assertEquals(listOf("d"), map["c"])
        assertEquals(setOf("e"), map["d"])
    }

    @Test
    fun getSingleStringAttribute() {
        val map = mutableMapOf<String, Any?>("a" to "b", "c" to mutableListOf(""), "d" to mutableSetOf<String>())
        map.setSingleAttributeValue("a", "c")
        map.setSingleAttributeValue("b", "b")
        map.setSingleAttributeValue("c", "d")
        map.setSingleAttributeValue("d", "e")

        assertEquals("c", singleStringAttributeValue(map["a"]))
        assertEquals("b", singleStringAttributeValue(map["b"]))
        assertEquals("d", singleStringAttributeValue(map["c"]))
        assertEquals("e", singleStringAttributeValue(map["d"]))
    }
}