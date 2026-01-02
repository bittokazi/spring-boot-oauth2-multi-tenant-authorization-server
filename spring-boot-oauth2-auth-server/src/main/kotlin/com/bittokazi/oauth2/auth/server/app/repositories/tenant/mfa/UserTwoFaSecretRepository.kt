package com.bittokazi.oauth2.auth.server.app.repositories.tenant.mfa

import com.bittokazi.oauth2.auth.server.app.models.tenant.mfa.UserTwoFaSecret
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserTwoFaSecretRepository : JpaRepository<UserTwoFaSecret, Long> {
    @Query(value = "SELECT * FROM user_two_fa_secret WHERE user_id = :userId", nativeQuery = true)
    fun findByUserId(@Param("userId") userId: String?): Optional<UserTwoFaSecret?>
}
