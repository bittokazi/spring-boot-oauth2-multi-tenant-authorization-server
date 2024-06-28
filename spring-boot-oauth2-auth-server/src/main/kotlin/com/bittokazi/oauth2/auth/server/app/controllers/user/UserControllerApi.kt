package com.bittokazi.oauth2.auth.server.app.controllers.user

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.models.tenant.mfa.TwoFASecretPayload
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

interface UserControllerApi {

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(
                            schema = Schema(implementation = User::class)
                        )
                    )
                ]
            )
        ]
    )
    fun getUsers(
        page: Int,
        count: Int
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun addUser(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun getUser(
        @PathVariable id: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun updateUser(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any?

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun updateUserPassword(
        @RequestBody user: User,
        httpServletResponse: HttpServletResponse
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun whoAmI(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun getByUsername(
        @RequestBody user: User
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun getByEmail(
        @RequestBody user: User
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun updateMyAccount(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun updateMyPassword(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun updateMyPasswordFromClient(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest
    ): Any

    fun userGenerateOtpSecret(
        httpServletRequest: HttpServletRequest
    ): Any

    fun userEnableOtpSecret(
        @RequestBody twoFASecretPayload: TwoFASecretPayload,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any

    fun disable2FA(
        httpServletRequest: HttpServletRequest
    ): Any

    fun user2FaTrustedDeviceList(
        httpServletRequest: HttpServletRequest
    ): Any

    fun user2FaTrustedDeviceDeleteByID(
        @PathVariable id: Long,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any

    fun regenerateScratchCode(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = User::class)
                    )
                ]
            )
        ]
    )
    fun verifyEmailOfUser(
        @RequestBody user: User,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any
}