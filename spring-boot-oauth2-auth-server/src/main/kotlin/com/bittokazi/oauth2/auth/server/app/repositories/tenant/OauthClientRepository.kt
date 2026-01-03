package com.bittokazi.oauth2.auth.server.app.repositories.tenant

import com.bittokazi.oauth2.auth.server.app.models.tenant.OauthClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OauthClientRepository : JpaRepository<OauthClient, String> {
    @Query(value = "SELECT * FROM oauth_client_details WHERE id = :id", nativeQuery = true)
    fun findOneById(id: String?): Optional<OauthClient>

    @Query(value = "SELECT * FROM oauth_client_details WHERE client_id = :clientId", nativeQuery = true)
    fun findOneByClientId(clientId: String?): Optional<OauthClient>
}
