package com.bittokazi.oauth2.auth.server.app.services.mfa

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.models.tenant.mfa.TwoFASecretPayload
import com.bittokazi.oauth2.auth.server.app.models.tenant.mfa.UserTrustedDevice
import com.bittokazi.oauth2.auth.server.app.models.tenant.mfa.UserTwoFaSecret
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.mfa.UserTrustedDeviceRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.mfa.UserTwoFaSecretRepository
import com.bittokazi.oauth2.auth.server.app.services.base.RestResponseGenerator.accessDenied
import com.bittokazi.oauth2.auth.server.app.services.base.RestResponseGenerator.notFound
import com.bittokazi.oauth2.auth.server.config.TenantContext.getCurrentTenant
import com.bittokazi.oauth2.auth.server.utils.Utils.randomNumberGenerator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.warrenstrange.googleauth.GoogleAuthenticator
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service
@Transactional
open class TwoFaService(
    private val userRepository: UserRepository,
    private val userTwoFaSecretRepository: UserTwoFaSecretRepository,
    private val userTrustedDeviceRepository: UserTrustedDeviceRepository
) {

    fun generateSecret(httpServletRequest: HttpServletRequest): Any {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.userPrincipal.name)
        val faSecret = userTwoFaSecretRepository!!.findByUserId(userOptional.get().id)
        if (faSecret.isPresent) {
            return ResponseEntity.status(403).build<Any>()
        }
        val scratchCodes: MutableList<String> = ArrayList()
        val gAuth = GoogleAuthenticator()
        val key = gAuth.createCredentials()
        for (i in 0..4) {
            scratchCodes.add(randomNumberGenerator(20))
        }
        return TwoFASecretPayload(
            secret = key.key,
            enabled = userOptional.get().twoFaEnabled,
            tenantName = getCurrentTenant(),
            scratchCodes = scratchCodes
        )
    }

    fun regenerateScratchCode(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse?
    ): Any {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.userPrincipal.name)
        val faSecret = userTwoFaSecretRepository!!.findByUserId(userOptional.get().id)
        if (faSecret.isPresent) {
            val scratchCodes: MutableList<String> = ArrayList()
            for (i in 0..4) {
                scratchCodes.add(randomNumberGenerator(20))
            }
            var userTwoFaSecret = faSecret.get()
            userTwoFaSecret.scratchCodes =
                Gson().toJson(scratchCodes.stream().map { code: String? -> BCrypt.hashpw(code, BCrypt.gensalt()) }
                    .toList())

            userTwoFaSecret = userTwoFaSecretRepository.save(userTwoFaSecret)
            return scratchCodes.also { TwoFASecretPayload().scratchCodes = it }
        }
        return notFound(httpServletResponse!!)
    }

    fun enable2FA(
        twoFASecretPayload: TwoFASecretPayload, httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse?
    ): Any {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.userPrincipal.name)

        if (userOptional.get().twoFaEnabled != null && userOptional.get().twoFaEnabled!!) return ResponseEntity.status(403).build<Any>()

        val userTwoFaSecret = UserTwoFaSecret()
        userTwoFaSecret.user = userOptional.get()
        userTwoFaSecret.secret = twoFASecretPayload.secret
        userTwoFaSecret.scratchCodes = Gson().toJson(
            twoFASecretPayload.scratchCodes.stream().map { code: String? -> BCrypt.hashpw(code, BCrypt.gensalt()) }
                .toList())
        val user = userOptional.get()
        if (validate2FA(twoFASecretPayload.code!!, userTwoFaSecret.secret)) {
            userTwoFaSecretRepository!!.save(userTwoFaSecret)
            user.twoFaEnabled = true
            userRepository.save(user)
        }
        return user
    }

    @Transactional
    open fun disable2FA(httpServletRequest: HttpServletRequest): Any {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.userPrincipal.name)
        val faSecret = userTwoFaSecretRepository!!.findByUserId(userOptional.get().id)
        val user = userOptional.get()
        user.twoFaEnabled = false
        if (faSecret.isPresent) {
            userTwoFaSecretRepository.delete(faSecret.get())
        }
        userTrustedDeviceRepository!!.deleteAllByUserId(user.id)
        return userRepository.save(user)
    }

    fun validate2FA(
        code: Int?, httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse?
    ): Boolean {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.getParameter("username"))
        if (userOptional.isPresent && code != null) {
            val faSecret = userTwoFaSecretRepository!!.findByUserId(userOptional.get().id)
            return validate2FA(code, faSecret.get().secret)
        }
        return false
    }

    fun validate2FAScratchCode(code: String?, httpServletRequest: HttpServletRequest): Boolean {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.getParameter("username"))
        if (userOptional.isPresent) {
            val faSecret = userTwoFaSecretRepository!!.findByUserId(userOptional.get().id)
            if (faSecret.isPresent) {
                val listType = object : TypeToken<ArrayList<String?>?>() {
                }.type
                val scratchCodes: MutableList<String>
                if (faSecret.get().scratchCodes != null) {
                    scratchCodes = Gson().fromJson(faSecret.get().scratchCodes, listType)

                    val bCryptPasswordEncoder = BCryptPasswordEncoder()
                    val tmp = scratchCodes.stream()
                        .filter { scratchCode: String? -> bCryptPasswordEncoder.matches(code, scratchCode) }
                        .collect(Collectors.toList())
                    if (tmp.size > 0) {
                        scratchCodes.remove(tmp[0])
                        val userTwoFaSecret = faSecret.get()
                        userTwoFaSecret.scratchCodes = Gson().toJson(scratchCodes)
                        userTwoFaSecretRepository.save(userTwoFaSecret)
                        return true
                    }
                }
            }
        }
        return false
    }

    fun validate2FA(code: Int, secret: String?): Boolean {
        val gAuth = GoogleAuthenticator()
        return gAuth.authorize(secret, code)
    }

    fun isTrustedDevice(deviceId: String?, user: User): Boolean {
        return userTrustedDeviceRepository!!.findAllByUserIdandInstanceId(user.id, deviceId).size > 0
    }

    fun saveTrustedDevice(deviceId: String?, user: User, userAgent: String?, ip: String?) {
        if (userTrustedDeviceRepository!!.findAllByUserIdandInstanceId(user.id, deviceId).size < 1) {
            val userTrustedDevice = UserTrustedDevice()
            userTrustedDevice.instanceId = deviceId
            userTrustedDevice.user = user
            userTrustedDevice.userAgent = userAgent
            userTrustedDevice.deviceIp = ip
            userTrustedDeviceRepository.save(userTrustedDevice)
        }
    }

    fun selfGetAllTrustedDeviceOfUser(httpServletRequest: HttpServletRequest): Any {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.userPrincipal.name)
        return userTrustedDeviceRepository!!.findAllByUserId(userOptional.get().id)
    }

    fun selfDeleteTrustedDeviceById(
        id: Long, httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse?
    ): Any {
        val userOptional = userRepository!!.findOneByUsername(httpServletRequest.userPrincipal.name)
        val userTrustedDeviceOptional = userTrustedDeviceRepository!!.findById(id)
        if (!userTrustedDeviceOptional.isPresent) {
            return notFound(httpServletResponse!!)
        }
        if (userTrustedDeviceOptional.get().user!!.id != userOptional.get().id) {
            return accessDenied(httpServletResponse!!)
        }
        userTrustedDeviceRepository.deleteById(id)
        return userTrustedDeviceOptional.get()
    }
}

