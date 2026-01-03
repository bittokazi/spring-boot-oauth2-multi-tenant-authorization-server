package com.bittokazi.oauth2.auth.server.app.services

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import com.bittokazi.oauth2.auth.server.app.models.tenant.security.RoleOauth
import com.bittokazi.oauth2.auth.server.app.models.tenant.security.UserOauth
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.stream.Collectors

/**
 * @author Bitto Kazi
 */
@Transactional
@Component
open class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        try {
            val user = userRepository.findOneByUsernameIgnoreCase(username)
            if (!user.isPresent) {
                throw UsernameNotFoundException("user not found")
            }
            val userDetails = UserOauth()
            userDetails.username = user.get().username
            userDetails.password = user.get().password
            userDetails.roles = user.get().roles.stream().map<RoleOauth> { role: Role ->
                val roleOauth = RoleOauth(role.id, role.name, role.title)
                roleOauth
            }.collect(Collectors.toSet())
            userDetails.email = user.get().email
            userDetails.authorities = user.get().getAuthorities()
            return userDetails
        } catch (e: Exception) {
            logger.error("ERROR During loading User ", e)
        }
        throw UsernameNotFoundException("User Not Found")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)
    }
}
