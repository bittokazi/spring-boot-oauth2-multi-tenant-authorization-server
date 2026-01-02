package com.bittokazi.oauth2.auth.server.app.models.tenant.mfa

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.io.Serializable

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_two_fa_secret")
@JsonIgnoreProperties(ignoreUnknown = true)
class UserTwoFaSecret : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_two_fa_secret_generator")
    @SequenceGenerator(
        name = "user_two_fa_secret_generator",
        sequenceName = "user_two_fa_secret_sequence",
        initialValue = 1,
        allocationSize = 1
    )
    var id: Long? = null

    @OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.DETACH])
    @Fetch(value = FetchMode.SELECT)
    @JoinColumn(name = "user_id", nullable = true, insertable = true, updatable = false)
    var user: User? = null

    var secret: String? = null

    @Column(name = "scratch_codes")
    var scratchCodes: String? = null

    @Transient
    var tenantName: String? = null

    companion object {
        const val serialVersionUID = 1L
    }
}

