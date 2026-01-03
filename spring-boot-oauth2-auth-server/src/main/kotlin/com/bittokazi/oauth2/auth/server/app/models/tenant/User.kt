package com.bittokazi.oauth2.auth.server.app.models.tenant

import com.bittokazi.oauth2.auth.server.app.models.base.BaseModel
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.UuidGenerator
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.io.Serializable
import java.util.*

/**
 * @author Bitto Kazi
 */

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "users")
class User : BaseModel(), Serializable {
    @Id
    @GeneratedValue(generator = "uuid")
    @UuidGenerator
    var id: String? = null

    @Column(name = "first_name", length = 128, nullable = false)
    var firstName: String? = null

    @Column(name = "last_name", length = 128, nullable = false)
    var lastName: String? = null

    @Column(name = "email", unique = true, length = 128, nullable = false)
    var email: String? = null

    @Column(name = "user_name", length = 128, unique = true, nullable = false)
    var username: String? = null

    @Column(length = 128, nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String? = null

    @Column
    var enabled: Boolean? = null

    @Column(name = "change_password")
    var changePassword: Boolean? = null

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.DETACH], targetEntity = Role::class)
    @Fetch(value = FetchMode.SELECT)
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: Set<Role> = mutableSetOf()

    @Column(name = "contact_number")
    var contactNumber: String? = null

    @Column(name = "dob")
    var dob: Date? = null

    @Column(name = "image_name")
    var imageName: String? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "image_absolute_path")
    var imageAbsolutePath: String? = null

    @Column(name = "last_login")
    var lastLogin: Date? = null

    @Column(name = "email_verified")
    var emailVerified: Boolean? = null

    var address: String? = null

    var gender: String? = null

    @Column(name = "two_fa_enabled")
    var twoFaEnabled: Boolean? = null

    @Transient
    var newPassword: String? = null

    @Transient
    var newConfirmPassword: String? = null

    @Transient
    var authorities: List<GrantedAuthority>? = null

    @Transient
    var avatarImage: String? = null

    @Transient
    var adminTenantUser = false

    @Transient
    var currentPassword: String? = null

    fun getAuthorities(): Collection<GrantedAuthority> {
        val rls: MutableList<GrantedAuthority> = ArrayList()
        for (role in roles) {
            rls.add(SimpleGrantedAuthority(role.name!!))
        }
        return rls
    }

    companion object {
        /**
         *
         */
        const val serialVersionUID = 1L
    }
}

