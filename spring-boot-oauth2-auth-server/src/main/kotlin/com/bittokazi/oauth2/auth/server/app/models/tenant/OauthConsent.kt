package com.bittokazi.oauth2.auth.server.app.models.tenant

import jakarta.persistence.Column
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "oauth2_authorization_consent")
class OauthConsent {
    @Column(name = "registered_client_id")
    var registeredClientId: String? = null

    @Column(name = "principal_name")
    var principalName: String? = null

    @Column(name = "authorities")
    var authorities: String? = null
}
