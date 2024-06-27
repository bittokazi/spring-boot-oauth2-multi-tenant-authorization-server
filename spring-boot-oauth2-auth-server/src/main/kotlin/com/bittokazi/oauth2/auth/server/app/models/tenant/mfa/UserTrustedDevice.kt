package com.bittokazi.oauth2.auth.server.app.models.tenant.mfa

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Proxy
import java.io.Serializable

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_trusted_device")
@Proxy(lazy = false)
@JsonIgnoreProperties(ignoreUnknown = true)
class UserTrustedDevice : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_trusted_device_generator")
    @SequenceGenerator(
        name = "user_trusted_device_generator",
        sequenceName = "user_trusted_device_sequence",
        initialValue = 1,
        allocationSize = 1
    )
    val id: Long? = null

    @OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.DETACH])
    @Fetch(value = FetchMode.SELECT)
    @JoinColumn(name = "user_id", nullable = true, insertable = true, updatable = false)
    var user: User? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "instance_id")
    var instanceId: String? = null

    @Column(name = "device_ip")
    var deviceIp: String? = null

    @Column(name = "user_agent")
    var userAgent: String? = null

    companion object {
        const val serialVersionUID = 1L
    }
}
