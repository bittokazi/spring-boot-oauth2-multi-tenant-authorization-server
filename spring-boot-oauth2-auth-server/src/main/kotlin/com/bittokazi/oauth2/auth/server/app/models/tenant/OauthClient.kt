package com.bittokazi.oauth2.auth.server.app.models.tenant

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.nimbusds.jose.shaded.gson.Gson
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.*

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "oauth_client_details")
class OauthClient: Serializable {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    val id: String? = null

    @Column(name = "client_id")
    var clientId: String? = null

    @Column(name = "resource_ids")
    var resourceIds: String? = null

    @Column(name = "client_secret")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var clientSecret: String? = null

    @JsonIgnore
    @Column(name = "scope")
    var scope: String? = null

    @Column(name = "client_authentication_method")
    var clientAuthenticationMethod: String? = null

    @JsonIgnore
    @Column(name = "authorized_grant_types")
    var authorizedGrantTypes: String? = null

    @JsonIgnore
    @Column(name = "web_server_redirect_uri")
    var webServerRedirectUri: String? = null

    @Column(name = "authorities")
    var authorities: String? = null

    @Column(name = "access_token_validity")
    var accessTokenValidity: Int? = null

    @Column(name = "refresh_token_validity")
    var refreshTokenValidity: Int? = null

    @JsonIgnore
    @Column(name = "additional_information")
    var additionalInformation: String? = null

    @Column(name = "require_consent")
    var requireConsent: Boolean? = null

    @Column(name = "post_logout_url")
    var postLogoutUrl: String? = null

    @Column(name = "token_type")
    var tokenType: String? = null

    @Transient
    val generateSecret = false

    @Transient
    var newSecret: String? = null

    fun scopeAsSet(): MutableSet<String> {
        try {
            val scopes = scope!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return mutableSetOf(*scopes)
        } catch (e: Exception) {
            return mutableSetOf()
        }
    }

    @JsonGetter("scope")
    fun getterScope() = scopeAsSet()

    @JsonSetter("scope")
    fun setterScope(data: MutableSet<String>) {
        scope = data.joinToString(",")
    }

    fun authorizedGrantTypesAsSet(): MutableSet<String> {
        try {
            val grantTypes =
                authorizedGrantTypes!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return mutableSetOf(*grantTypes)
        } catch (e: Exception) {
            return mutableSetOf()
        }
    }

    @JsonGetter("authorizedGrantTypes")
    fun getterAuthorizedGrantTypes() = authorizedGrantTypesAsSet()

    @JsonSetter("authorizedGrantTypes")
    fun setterAuthorizedGrantTypes(data: MutableSet<String>) {
        authorizedGrantTypes = data.joinToString(",")
    }

    fun additionalInformationMap(): Map<*, *> = Gson().fromJson(additionalInformation, HashMap::class.java)

    @JsonGetter("additionalInformation")
    fun getterAdditionalInformation() = additionalInformation

    @JsonSetter("additionalInformation")
    fun setterAdditionalInformation(data: String) {
        additionalInformation = data
    }

    fun webServerRedirectUriAsSet(): MutableSet<String>  {
        try {
            val webServerRedirectUris = webServerRedirectUri!!.split(",".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
            return when(webServerRedirectUris.size) {
                0 -> mutableSetOf("")
                else -> mutableSetOf(*webServerRedirectUris)
            }
        } catch (e: Exception) {
            return mutableSetOf("")
        }
    }

    @JsonGetter("webServerRedirectUri")
    fun getterWebServerRedirectUri() = webServerRedirectUriAsSet()

    @JsonSetter("webServerRedirectUri")
    fun setterWebServerRedirectUri(data: MutableSet<String>) {
        webServerRedirectUri = data.joinToString(",")
    }

    companion object {
        const val serialVersionUID = 1L
    }
}

