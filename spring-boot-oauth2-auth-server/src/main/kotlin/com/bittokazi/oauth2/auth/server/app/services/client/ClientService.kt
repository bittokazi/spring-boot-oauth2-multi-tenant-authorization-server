package com.bittokazi.oauth2.auth.server.app.services.client

import com.bittokazi.oauth2.auth.server.app.models.tenant.OauthClient
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository
import com.bittokazi.oauth2.auth.server.config.TenantContext
import com.bittokazi.oauth2.auth.server.utils.Utils
import com.nimbusds.jose.shaded.gson.Gson
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class ClientService(
    private val oauthClientRepository: OauthClientRepository
) {

    fun saveOauthClient(oauthClient: OauthClient, httpServletResponse: HttpServletResponse): ResponseEntity<*> {
        var oauthClient = oauthClient
        oauthClient.additionalInformation = "{ \"client\": \"user_generated\" }"
        oauthClient.clientId = UUID.randomUUID().toString()
        val newSecret = Utils.randomNumberGenerator(32)
        oauthClient.clientSecret = newSecret
        oauthClient.newSecret = newSecret
        if (TenantContext.getCurrentTenant() != "public") oauthClient.scope = oauthClient
            .scopeAsSet()?.filter { s: String ->
                !(s == "tenant:read" || s == "tenant:write")
            }?.joinToString(",")
        oauthClient = oauthClientRepository.save(oauthClient)
        oauthClient.newSecret = newSecret
        return ResponseEntity.ok(oauthClient)
    }

    fun allOauthClients(): ResponseEntity<*> = ResponseEntity.ok(oauthClientRepository.findAll())

    fun getOauthClient(id: String, httpServletResponse: HttpServletResponse): ResponseEntity<*> {
        val clientEntityOptional = oauthClientRepository.findById(id)
        if (clientEntityOptional.isPresent) {
            return ResponseEntity.ok(clientEntityOptional.get())
        }
        return ResponseEntity.status(404).build<Any>()
    }

    fun updateOauthClient(oauthClient: OauthClient, httpServletResponse: HttpServletResponse): ResponseEntity<*> {
        var oauthClient = oauthClient
        val clientEntityOptional = oauthClientRepository.findById(oauthClient.id!!)

        if (clientEntityOptional.isPresent) {
            val clientEntityToUpdate = clientEntityOptional.get()

            if (clientEntityToUpdate.additionalInformationMap() != null && clientEntityToUpdate.additionalInformationMap()
                    .containsKey("client")
                && (clientEntityToUpdate.additionalInformationMap()["client"]
                        == "default")
            ) {
                oauthClient.additionalInformation = Gson().toJson(clientEntityToUpdate.additionalInformationMap())
            } else {
                oauthClient.additionalInformation = "{ \"client\": \"user_generated\" }"
            }
            if (TenantContext.getCurrentDataTenant() != "public") oauthClient.scope = oauthClient
                .scopeAsSet()?.filter { s: String ->
                    !(s == "tenant:read" || s == "tenant:write")
                }?.joinToString(",")
            var newSecret = ""
            if (oauthClient.generateSecret) {
                newSecret = Utils.randomNumberGenerator(32)
                oauthClient.clientSecret = newSecret
            } else {
                oauthClient.clientSecret = clientEntityToUpdate.clientSecret
            }
            oauthClient = oauthClientRepository.save(oauthClient)
            if (newSecret != "") oauthClient.newSecret = newSecret
            return ResponseEntity.ok(oauthClient)
        }
        return ResponseEntity.status(404).build<Any>()
    }

    fun deleteOauthClient(id: String, httpServletResponse: HttpServletResponse): ResponseEntity<*> {
        val res: MutableMap<String, Any> = mutableMapOf()
        val clientEntityOptional = oauthClientRepository.findById(id)
        if (clientEntityOptional.isPresent) {
            if (clientEntityOptional.get().additionalInformationMap() != null &&
                clientEntityOptional.get().additionalInformationMap()
                    .containsKey("client")
                && (clientEntityOptional.get().additionalInformationMap()["client"] == "default")
            ) {
                res["message"] = "Delete Not Permitted"
                return ResponseEntity.status(403).body(res)
            } else {
                oauthClientRepository.deleteById(id)
                return ResponseEntity.ok(clientEntityOptional.get())
            }
        }
        return ResponseEntity.status(404).build<Any>()
    }
}
