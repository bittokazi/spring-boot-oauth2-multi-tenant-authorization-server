package com.bittokazi.oauth2.auth.server.app.models.tenant

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class UserDetailsImpl : UserDetails, Serializable {
    private val username: String? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private val password: String? = null

    val authorities: List<GrantedAuthority>? = null

    val enabled = false

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities!!
    }

    override fun getPassword(): String {
        return password!!
    }

    override fun getUsername(): String {
        return username!!
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
