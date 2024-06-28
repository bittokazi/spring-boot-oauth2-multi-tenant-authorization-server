package com.bittokazi.oauth2.auth.server.app.controllers.user

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.models.tenant.mfa.TwoFASecretPayload
import com.bittokazi.oauth2.auth.server.app.services.mfa.TwoFaService
import com.bittokazi.oauth2.auth.server.app.services.user.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * @author Bitto Kazi
 */
@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService, 
    private val twoFaService: TwoFaService
): UserControllerApi {

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/users")
    override fun getUsers(
        page: Int,
        count: Int
    ): Any = userService.getUsers(page, count)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PostMapping("/users")
    override fun addUser(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = userService.addUser(user, httpServletRequest, httpServletResponse, false, false)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/users/{id}")
    override fun getUser(
        @PathVariable id: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = userService.getUser(id, httpServletRequest, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PutMapping("/users/{id}")
    override fun updateUser(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any? = userService.updateUser(user, httpServletRequest, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PutMapping("/users/{id}/update/password")
    override fun updateUserPassword(
        @RequestBody user: User,
        httpServletResponse: HttpServletResponse
    ): Any = userService.updateUserPassword(user, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_profile') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/users/whoami")
    override fun whoAmI(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = userService.whoAmI(httpServletRequest, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PostMapping("/users/search/username")
    override fun getByUsername(
        @RequestBody user: User
    ): Any = userService.getByUsername(user)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PostMapping("/users/search/email")
    override fun getByEmail(
        @RequestBody user: User
    ): Any = userService.getByEmail(user)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @PutMapping("/users/whoami")
    override fun updateMyAccount(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest
    ): Any = userService.updateMyProfile(user, httpServletRequest)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @PutMapping("/users/whoami/password")
    override fun updateMyPassword(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest
    ): Any = userService.updateMyPassword(user, httpServletRequest)

    @PreAuthorize("hasAuthority('SCOPE_user:all')")
    @PutMapping("/users/whoami/password/client")
    override fun updateMyPasswordFromClient(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest
    ): Any = userService.updateMyPasswordFromClient(user, httpServletRequest)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @GetMapping("/users/whoami/mfa/otp/generate-secret")
    override fun userGenerateOtpSecret(
        httpServletRequest: HttpServletRequest
    ): Any = twoFaService.generateSecret(httpServletRequest!!)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @PostMapping("/users/whoami/mfa/otp/enable")
    override fun userEnableOtpSecret(
        @RequestBody twoFASecretPayload: TwoFASecretPayload,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = twoFaService.enable2FA(twoFASecretPayload!!, httpServletRequest!!, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @GetMapping("/users/whoami/mfa/otp/disable")
    override fun disable2FA(
        httpServletRequest: HttpServletRequest
    ): Any = twoFaService.disable2FA(httpServletRequest!!)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @GetMapping("/users/whoami/mfa/trusted-devices")
    override fun user2FaTrustedDeviceList(
        httpServletRequest: HttpServletRequest
    ): Any = twoFaService.selfGetAllTrustedDeviceOfUser(httpServletRequest!!)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @DeleteMapping("/users/whoami/mfa/trusted-devices/{id}")
    override fun user2FaTrustedDeviceDeleteByID(
        @PathVariable id: Long,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = twoFaService.selfDeleteTrustedDeviceById(id, httpServletRequest!!, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_profile')")
    @GetMapping("/users/whoami/mfa/generate-scratch-codes")
    override fun regenerateScratchCode(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = twoFaService.regenerateScratchCode(httpServletRequest!!, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PutMapping("/users/{id}/verify/email")
    override fun verifyEmailOfUser(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = userService.verifyEmailOfUser(user, httpServletRequest, httpServletResponse)
}

