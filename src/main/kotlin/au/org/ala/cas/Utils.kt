package au.org.ala.cas

import org.apereo.cas.authentication.Authentication

fun Authentication.alaUserId(): Long? = alaUserId(this.principal?.attributes?.get("userid"))

fun alaUserId(value: Any?): Long? = when(value) {
    is Int -> value.toLong()
    is Long -> value
    is String -> value.toLong()
    else -> null
}