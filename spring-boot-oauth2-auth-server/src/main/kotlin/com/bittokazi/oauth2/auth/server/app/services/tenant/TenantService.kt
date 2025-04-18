package com.bittokazi.oauth2.auth.server.app.services.tenant

import com.bittokazi.oauth2.auth.server.app.models.base.FileInput
import com.bittokazi.oauth2.auth.server.app.models.base.UploadObject
import com.bittokazi.oauth2.auth.server.app.models.master.Tenant
import com.bittokazi.oauth2.auth.server.app.models.master.TenantInfo
import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository
import com.bittokazi.oauth2.auth.server.app.services.file.FileStorageProvider
import com.bittokazi.oauth2.auth.server.config.AppConfig
import com.bittokazi.oauth2.auth.server.config.TenantContext.getCurrentDataTenant
import com.bittokazi.oauth2.auth.server.database.MultiTenantConnectionProviderImpl
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile


@Service
class TenantService(
    private val tenantRepository: TenantRepository,
    private val multiTenantConnectionProvider: MultiTenantConnectionProviderImpl,
    private val oauthClientRepository: OauthClientRepository,
    private val fileStorageProvider: FileStorageProvider
) {
    fun addTenant(tenant: Tenant): ResponseEntity<*> {
        var tenant = tenant
        if (getCurrentDataTenant() != "public") return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()

        val errors = validate(tenant, null)

        if (!errors.isEmpty()) {
            return ResponseEntity.status(400).body(errors)
        }

        tenant = tenantRepository.save(tenant)
        multiTenantConnectionProvider.singleTenantCreation(tenant)
        return ResponseEntity.ok(tenant)
    }

    fun allTenants(): ResponseEntity<*> {
        if (getCurrentDataTenant() != "public") return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()
        return ResponseEntity.ok(tenantRepository.findAll())
    }

    fun getTenantInfo(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse?
    ): ResponseEntity<*> {
        if (getCurrentDataTenant() != "public") return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()
        val host = httpServletRequest.getHeader("host").replace("www.", "")
        if (System.getenv()["DOMAIN"] == host) {
            return ResponseEntity.ok("{\"cpanel\": \"true\"}")
        } else {
            val tenant = tenantRepository.findOneByCompanyKey(host)
            if (tenant.isPresent) return ResponseEntity.ok(tenant.get())
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}")
    }

    fun getTenant(
        httpServletRequest: HttpServletRequest?, httpServletResponse: HttpServletResponse?,
        id: String
    ): ResponseEntity<*> {
        if (getCurrentDataTenant() != "public") return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()
        val optional = tenantRepository.findById(id)
        return ResponseEntity.ok(optional.get())
    }

    fun updateTenant(
        tenant: Tenant, httpServletRequest: HttpServletRequest?,
        httpServletResponse: HttpServletResponse?
    ): ResponseEntity<*> {
        if (getCurrentDataTenant() != "public") return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()

        val dbTenantOptional = tenantRepository.findById(tenant.id!!)

        if (dbTenantOptional.isEmpty) {
            return ResponseEntity.status(404).build<Any>()
        }

        val errors = validate(tenant, dbTenantOptional.get())

        if (!errors.isEmpty()) {
            return ResponseEntity.status(400).body(errors)
        }

        return ResponseEntity.ok(tenantRepository.save(tenant))
    }

    fun info(): ResponseEntity<*> = when(getCurrentDataTenant()) {
        "public" -> ResponseEntity.ok(
            TenantInfo(
                cpanel = true,
                enabledConfigPanel = true,
                name = AppConfig.DEFAULT_APP_NAME
            )
        )
        else -> {
            tenantRepository.findOneByCompanyKey(getCurrentDataTenant()!!).run {
                when(this.isEmpty) {
                    true -> ResponseEntity.notFound().build<Any>()
                    false -> ResponseEntity.ok(
                        TenantInfo(
                            cpanel = false,
                            enabledConfigPanel = get().enableConfigPanel ?: run { false },
                            name = get().name ?: run { "" }
                        )
                    )
                }
            }
        }
    }

    fun addTemplate(file: MultipartFile?, uploadObject: UploadObject): ResponseEntity<Any> {
        val absoluteFilePath: String = AppConfig.TEMPLATE_FOLDER_BASE
        return file?.let {
            FileInput(
                file = it,
                folder = absoluteFilePath,
                fileName = uploadObject.filename
            )
        }?.let {
            fileStorageProvider.upload(
                it
            )?.let {
                ResponseEntity.ok(
                    it
                )
            } ?: run {
                ResponseEntity.badRequest().build<Any>()
            }
        }?: run {
            ResponseEntity.badRequest().build()
        }
    }

    fun validate(tenant: Tenant, dbTenant: Tenant?): MutableMap<String, MutableList<String>> {
        val errors: MutableMap<String, MutableList<String>> = mutableMapOf()

        if (dbTenant == null || tenant.companyKey != dbTenant.companyKey) {
            if (tenantRepository.findOneByCompanyKey(tenant.companyKey!!).isPresent) {
                errors.put("key", mutableListOf("exist"))
            }
        }

        if (dbTenant == null || tenant.domain != dbTenant.domain) {
            if (tenantRepository.findOneByDomain(tenant.domain!!).isPresent) {
                errors.put("domain", mutableListOf("exist"))
            }
        }

        if (dbTenant == null || tenant.name != dbTenant.name) {
            if (tenantRepository.findOneByName(tenant.name!!).isPresent) {
                errors.put("name", mutableListOf("exist"))
            }
        }

        return errors
    }
}
