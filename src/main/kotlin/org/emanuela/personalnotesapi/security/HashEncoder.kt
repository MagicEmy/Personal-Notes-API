package org.emanuela.personalnotesapi.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {

    private val bcrypt = BCryptPasswordEncoder()

    fun encode(raw:String): String = bcrypt.encode(raw)

    fun matches(
        rawPassword: String, hashedPassword: String
    ): Boolean = bcrypt.matches(rawPassword, hashedPassword)

}