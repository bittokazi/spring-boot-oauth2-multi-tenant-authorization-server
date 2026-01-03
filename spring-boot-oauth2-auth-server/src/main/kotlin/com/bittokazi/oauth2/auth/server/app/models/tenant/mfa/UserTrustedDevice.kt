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
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_trusted_device")
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
    var id: Long? = null

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

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    var createdDate: LocalDateTime? = LocalDateTime.now()

    @LastModifiedDate
    @Column(name = "updated_date", nullable = true)
    var updatedDate: LocalDateTime? = LocalDateTime.now()

    companion object {
        const val serialVersionUID = 1L
    }
}
