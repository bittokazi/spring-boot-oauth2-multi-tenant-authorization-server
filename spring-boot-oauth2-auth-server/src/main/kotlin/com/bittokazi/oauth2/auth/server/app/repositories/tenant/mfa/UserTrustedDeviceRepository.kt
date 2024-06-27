package com.bittokazi.oauth2.auth.server.app.repositories.tenant.mfa

import com.bittokazi.oauth2.auth.server.app.models.tenant.mfa.UserTrustedDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional
@Repository
interface UserTrustedDeviceRepository : JpaRepository<UserTrustedDevice?, Long?> {
    @Query(value = "SELECT * FROM user_trusted_device WHERE user_id = :userId", nativeQuery = true)
    fun findAllByUserId(@Param("userId") userId: String?): List<UserTrustedDevice?>

    @Query(
        value = "SELECT * FROM user_trusted_device WHERE user_id = :userId AND instance_id = :instanceId",
        nativeQuery = true
    )
    fun findAllByUserIdandInstanceId(
        @Param("userId") userId: String?,
        @Param("instanceId") instanceId: String?
    ): List<UserTrustedDevice?>

    @Modifying
    @Query(
        value = "DELETE FROM user_trusted_device WHERE user_id = :userId AND instance_id = :instanceId",
        nativeQuery = true
    )
    fun deleteAllByUserIdandInstanceId(@Param("userId") userId: Long, @Param("instanceId") instanceId: String?)

    @Modifying
    @Query(value = "DELETE FROM user_trusted_device WHERE user_id = :userId", nativeQuery = true)
    fun deleteAllByUserId(@Param("userId") userId: String?)
}
