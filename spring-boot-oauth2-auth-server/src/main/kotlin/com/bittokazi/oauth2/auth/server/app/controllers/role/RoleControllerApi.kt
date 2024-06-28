package com.bittokazi.oauth2.auth.server.app.controllers.role

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

interface RoleControllerApi {

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(
                            schema = Schema(implementation = Role::class)
                        )
                    )
                ]
            )
        ]
    )
    fun getRoles(
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "count", required = false, defaultValue = "10") count: Int
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(
                            schema = Schema(implementation = Role::class)
                        )
                    )
                ]
            )
        ]
    )
    fun allRoles(): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Role::class)
                    )
                ]
            )
        ]
    )
    fun addRole(
        @RequestBody role: Role,
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
                        schema = Schema(implementation = Role::class)
                    )
                ]
            )
        ]
    )
    fun getRole(
        @PathVariable id: String
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Role::class)
                    )
                ]
            )
        ]
    )
    fun updateRole(
        @PathVariable id: String,
        @RequestBody role: Role,
        response: HttpServletResponse
    ): Any

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Role::class)
                    )
                ]
            )
        ]
    )
    fun getRoleByName(
        @RequestBody role: Role,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any
}