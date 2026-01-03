package com.bittokazi.oauth2.auth.server.app.services.user.helpers

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

/**
 * @author Bitto Kazi
 */
object UserUpdateHelper {

    fun validateUser(
        user: User, userOptional: Optional<User>,
        userRepository: UserRepository
    ): Map<String, List<String>> {
        val errors: MutableMap<String, List<String>> = HashMap()
        if (userOptional.get().email != user.email
            && userRepository.findOneByEmail(user.email).isPresent
        ) {
            errors["email"] = mutableListOf("exist")
        }
        return errors
    }

    fun setDefaultValues(user: User, userOptional: Optional<User>, myProfile: Boolean): User {
        if (Objects.isNull(user.imageAbsolutePath) || user.imageAbsolutePath == "") {
            user.imageAbsolutePath = userOptional.get().imageAbsolutePath
            user.imageName = userOptional.get().imageName
        }
        user.password = userOptional.get().password
        user.username = userOptional.get().username
        user.emailVerified = userOptional.get().emailVerified
        if (Objects.isNull(user.twoFaEnabled)) {
            user.twoFaEnabled = userOptional.get().twoFaEnabled
        }
        if (myProfile) {
            user.enabled = userOptional.get().enabled
            user.roles = userOptional.get().roles
            user.changePassword = userOptional.get().changePassword
        } else {
            if (Objects.isNull(user.enabled)) {
                user.enabled = userOptional.get().enabled
            }
            if (Objects.isNull(user.changePassword)) {
                user.changePassword = userOptional.get().changePassword
            }
        }
        return user
    }

    fun validatePassword(
        user: User, userOptional: Optional<User>,
        userRepository: UserRepository?
    ): Map<String, List<String>> {
        val errors: MutableMap<String, List<String>> = HashMap()
        val bCryptPasswordEncoder = BCryptPasswordEncoder()
        if (!bCryptPasswordEncoder.matches(user.currentPassword, userOptional.get().password)) {
            errors["currentPassword"] = mutableListOf("currentWrong")
        }
        if (bCryptPasswordEncoder.matches(user.newPassword, userOptional.get().password)) {
            errors["newPassword"] = mutableListOf("sameToPrevious")
        }
        if (user.newPassword != user.newConfirmPassword) {
            errors["newConfirmPassword"] = mutableListOf("newDoNotMatch")
        }
        return errors
    }
}
