package com.bittokazi.oauth2.auth.server.app.models.tenant

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Proxy
import java.io.Serializable

/**
 * @author Bitto Kazi
 */
@Entity
class Role : Serializable {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    var id: String? = null

    @Column(unique = true, nullable = false, length = 128)
    var name: String? = null

    @Column(nullable = false, length = 128)
    var title: String? = null

    @Column(length = 64)
    var description: String? = null

    companion object {
        /**
         *
         */
        const val serialVersionUID = 1L
    }
}
