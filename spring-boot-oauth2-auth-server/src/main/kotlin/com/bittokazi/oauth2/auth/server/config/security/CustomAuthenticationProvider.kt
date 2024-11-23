package com.bittokazi.oauth2.auth.server.config.security

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.services.CustomUserDetailsService
import com.bittokazi.oauth2.auth.server.app.services.mfa.TwoFaService
import jakarta.servlet.http.Cookie
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

@Component
class CustomAuthenticationProvider(
    private val customUserDetailsService: CustomUserDetailsService,
    private val userRepository: UserRepository,
    private val twoFaService: TwoFaService
): AuthenticationProvider {

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        val servletRequest = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val session = servletRequest.request.getSession(true)

        val username: String = authentication.name
        val password: String = authentication.credentials.toString()

        val userDetails: UserDetails = customUserDetailsService.loadUserByUsername(username)
            ?: throw UsernameNotFoundException("User not found")

        val bCryptPasswordEncoder = BCryptPasswordEncoder()
        if (!bCryptPasswordEncoder.matches(password,userDetails.password)) {
            throw object : AuthenticationException("Invalid credentials") {}
        }

        val user: Optional<User?> = userRepository.findOneByUsernameIgnoreCase(username)
        if(user.isPresent) {
            val cookies = servletRequest.request.cookies
            val deviceId = Arrays.stream(cookies).filter { cookie: Cookie -> cookie.name == "deviceId" }
                .findFirst()
            if (deviceId.isPresent) {
                if (Objects.nonNull(user.get().twoFaEnabled) &&
                    user.get().twoFaEnabled == true
                ) {
                    if(!twoFaService.isTrustedDevice(deviceId.get().value, user.get())) {
                        session.setAttribute("otp", true)
                    }
                }
                val authenticated: Authentication = UsernamePasswordAuthenticationToken(
                    userDetails, password, userDetails.authorities
                )
                return authenticated
            }
        }

        throw object : AuthenticationException("Invalid Device ID") {}
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}