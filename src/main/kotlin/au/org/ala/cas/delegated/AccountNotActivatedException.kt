package au.org.ala.cas.delegated

import java.io.Serializable
import javax.security.auth.login.AccountException

class AccountNotActivatedException(s: String) : AccountException(s), Serializable {
    companion object {
        private const val serialVersionUID = 1234567890123456789L
    }
}