package com.bittokazi.oauth2.auth.server.app.services.user

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.services.base.BaseService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

/**
 * @author Bitto Kazi
 */
interface UserService : BaseService {
    fun addUser(
        user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse,
        self: Boolean,
        regApi: Boolean
    ): Any

    fun updateUser(
        user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any?

    fun getUsers(page: Int, count: Int): Any

    fun getUser(id: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Any

    fun whoAmI(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Any

    fun updateUserPassword(user: User, httpServletResponse: HttpServletResponse): Any

    fun updateMyProfile(user: User, httpServletRequest: HttpServletRequest): ResponseEntity<*>

    fun updateMyPassword(user: User, httpServletRequest: HttpServletRequest): ResponseEntity<*>

    fun updateMyPasswordFromClient(user: User, httpServletRequest: HttpServletRequest): ResponseEntity<*>

    fun getByUsername(user: User): ResponseEntity<*>

    fun getByEmail(user: User): ResponseEntity<*>

    fun verifyEmailOfUser(
        user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any
}
