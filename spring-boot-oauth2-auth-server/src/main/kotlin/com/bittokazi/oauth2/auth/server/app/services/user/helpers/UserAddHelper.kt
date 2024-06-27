package com.bittokazi.oauth2.auth.server.app.services.user.helpers

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * @author Bitto Kazi
 */
object UserAddHelper {
    fun validateUser(
        user: User,
        userRepository: UserRepository,
        oauthClientRepository: OauthClientRepository
    ): Map<String, List<String>> {
        val errors: MutableMap<String, List<String>> = HashMap()
        if (oauthClientRepository.findOneByClientId(user.username).isPresent) {
            errors["username"] = mutableListOf("notAllowed")
        }
        if (userRepository.findOneByUsername(user.username).isPresent) {
            errors["username"] = mutableListOf("exist")
        }
        if (userRepository.findOneByEmail(user.email).isPresent) {
            errors["email"] = mutableListOf("exist")
        }
        if (user.roles.size < 0) {
            errors["role"] = mutableListOf("empty")
        }
        return errors
    }

    fun addDefaultValues(user: User): User {
        user.password = BCryptPasswordEncoder().encode(user.newPassword)
        //		user.setEmailVerified(false);
        user.enabled = true
        user.twoFaEnabled = false
        return user
    }
}
