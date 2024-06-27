package com.bittokazi.oauth2.auth.server.app.models.tenant.security

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable
import java.util.*

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class UserOauth : UserDetails, Serializable {
    val id: String? = null

    var email: String? = null

    @JvmField
    var username: String? = null

    @JvmField
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String? = null

    val enabled = false

    var roles: Set<RoleOauth> = HashSet()

    @JvmField
    var authorities: Collection<GrantedAuthority>? = null

    val accountNonLocked = true

    val accountNonExpired = true

    val credentialsNonExpired = true


    override fun getAuthorities(): Collection<GrantedAuthority> {
        if (Objects.nonNull(authorities)) return authorities!!
        val rls: MutableList<GrantedAuthority> = ArrayList()
        for (role in roles) {
            rls.add(SimpleGrantedAuthority(role.name))
        }
        return rls
    }

    override fun getPassword(): String? {
        return password
    }

    override fun getUsername(): String? {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
