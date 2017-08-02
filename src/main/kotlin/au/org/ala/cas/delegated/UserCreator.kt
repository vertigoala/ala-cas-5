package au.org.ala.cas.delegated

interface UserCreator {
    fun createUser(email: String, firstName: String, lastName: String): Long?
}