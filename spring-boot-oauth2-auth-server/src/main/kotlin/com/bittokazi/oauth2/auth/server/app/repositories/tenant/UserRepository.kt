package com.bittokazi.oauth2.auth.server.app.repositories.tenant

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findOneByEmail(email: String?): Optional<User?>

    fun findOneByUsernameIgnoreCase(name: String?): Optional<User?>

    fun findOneByUsername(name: String?): Optional<User?>

    fun findOneByImageName(name: String?): Optional<User?>
}
