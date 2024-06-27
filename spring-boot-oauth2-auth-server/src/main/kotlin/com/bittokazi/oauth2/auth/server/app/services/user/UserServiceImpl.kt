package com.bittokazi.oauth2.auth.server.app.services.user

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.RoleRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.services.base.RestResponseGenerator.inputError
import com.bittokazi.oauth2.auth.server.app.services.base.RestResponseGenerator.notFound
import com.bittokazi.oauth2.auth.server.app.services.user.helpers.UserAddHelper
import com.bittokazi.oauth2.auth.server.app.services.user.helpers.UserHelpers
import com.bittokazi.oauth2.auth.server.app.services.user.helpers.UserUpdateHelper
import com.bittokazi.oauth2.auth.server.config.TenantContext.getCurrentDataTenant
import com.bittokazi.oauth2.auth.server.config.TenantContext.setCurrentDataTenant
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

/**
 * @author Bitto Kazi
 */
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val oauthClientRepository: OauthClientRepository
) : UserService {
    override fun addUser(
        user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse,
        self: Boolean,
        regApi: Boolean
    ): Any {
        var user = user
        val errors = UserAddHelper.validateUser(user, userRepository, oauthClientRepository)
        if (errors.size > 0) {
            return inputError(httpServletResponse!!, errors)
        }
        user = userRepository.save(UserAddHelper.addDefaultValues(user))
        return user
    }

    override fun getUser(
        id: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any {
        val userOptional = userRepository.findById(id)
        if (userOptional.isPresent) {
            return userOptional.get()
        }
        return notFound(httpServletResponse!!)
    }

    override fun getUsers(page: Int, count: Int): Any {
        return UserHelpers.getUsers(page, count, userRepository)
    }

    override fun updateUser(
        user: User, httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any? {
        val userOptional = userRepository.findById(
            user!!.id
        )
        if (userOptional.isPresent) {
            val errors = UserUpdateHelper.validateUser(user, userOptional, userRepository)
            if (errors.size > 0) {
                return inputError(httpServletResponse!!, errors)
            }
            return UserHelpers.setUserImage(
                userRepository.save(
                    UserUpdateHelper.setDefaultValues(
                        user,
                        userOptional,
                        false
                    )
                )
            )
        }
        return notFound(httpServletResponse!!)
    }

    override fun updateUserPassword(user: User, httpServletResponse: HttpServletResponse): Any {
        val userOptional = userRepository.findById(
            user!!.id
        )
        if (userOptional.isPresent) {
            val userDB = userOptional.get()
            userDB.password = BCryptPasswordEncoder().encode(user.newPassword)
            return ResponseEntity.ok(userRepository.save(userDB))
        }
        return ResponseEntity.status(404).build<Any>()
    }

    override fun whoAmI(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<*> {
        if (Objects.nonNull(httpServletRequest!!.userPrincipal)) {
            val host = httpServletRequest.getHeader("host").replace("www.", "")
            if (host == (System.getenv()["APPLICATION_BACKEND_URL"]
                    ?.replace("http://", "")
                    ?.replace("https://", "")
                ?.replace("www", "") ?: "")
            ) setCurrentDataTenant("public")
            val useOptional = userRepository.findOneByUsername(
                httpServletRequest.userPrincipal.name
            )
            if (useOptional.isPresent) {
                var user = useOptional.get()
                user = UserHelpers.setUserImage(user)!!
                user.adminTenantUser = getCurrentDataTenant() == "public"
                return ResponseEntity.ok(user)
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build<Any>()
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()
    }

    override fun updateMyProfile(user: User, httpServletRequest: HttpServletRequest): ResponseEntity<*> {
        val userOptional = userRepository.findOneByUsername(
            httpServletRequest!!.userPrincipal.name
        )
        if (userOptional.isPresent) {
            val errors = UserUpdateHelper.validateUser(user, userOptional, userRepository)
            if (errors.size > 0) {
                return ResponseEntity.badRequest().body(errors)
            }
            return ResponseEntity
                .ok(
                    UserHelpers.setUserImage(
                        userRepository.save(
                            UserUpdateHelper.setDefaultValues(
                                user,
                                userOptional,
                                true
                            )
                        )
                    )
                )
        }
        return ResponseEntity.status(404).build<Any>()
    }

    override fun updateMyPassword(user: User, httpServletRequest: HttpServletRequest): ResponseEntity<*> {
        val userOptional = userRepository.findOneByUsername(
            httpServletRequest!!.userPrincipal.name
        )
        if (userOptional.isPresent) {
            val errors = UserUpdateHelper.validatePassword(
                user, userOptional,
                userRepository
            )
            if (errors.size > 0) {
                return ResponseEntity.badRequest().body(errors)
            }
            val userDB = userOptional.get()
            userDB.password = BCryptPasswordEncoder().encode(user!!.newPassword)
            return ResponseEntity.ok(userRepository.save(userDB))
        }
        return ResponseEntity.status(404).build<Any>()
    }

    override fun updateMyPasswordFromClient(user: User, httpServletRequest: HttpServletRequest): ResponseEntity<*> {
        val userOptional = userRepository.findById(
            user!!.id
        )
        if (userOptional.isPresent) {
            val errors = UserUpdateHelper.validatePassword(
                user, userOptional,
                userRepository
            )
            if (errors.size > 0) {
                return ResponseEntity.badRequest().body(errors)
            }
            val userDB = userOptional.get()
            userDB.password = BCryptPasswordEncoder().encode(user.newPassword)
            return ResponseEntity.ok(userRepository.save(userDB))
        }
        return ResponseEntity.status(404).build<Any>()
    }

    override fun getByUsername(user: User): ResponseEntity<*> {
        val userOptional = userRepository.findOneByUsername(
            user.username
        )
        if (userOptional.isPresent) {
            return ResponseEntity.ok(userOptional.get())
        }
        return ResponseEntity.status(404).build<Any>()
    }

    override fun getByEmail(user: User): ResponseEntity<*> {
        val userOptional = userRepository.findOneByEmail(
            user.email
        )
        if (userOptional.isPresent) {
            return ResponseEntity.ok(userOptional.get())
        }
        return ResponseEntity.status(404).build<Any>()
    }

    override fun verifyEmailOfUser(
        user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any {
        val userOptional = userRepository.findById(
            user.id
        )
        if (userOptional.isPresent) {
            val userDb = userOptional.get()
            userDb.emailVerified = true
            return userRepository.save(userDb)
        }
        return notFound(httpServletResponse)
    }
}
