package com.bittokazi.oauth2.auth.server.app.models.master

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Proxy
import java.io.Serializable

@Entity
@Table(name = "tenant")
@Proxy(lazy = false)
@Setter
@Getter
class Tenant : Serializable {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    val id: String? = null

    @Column(name = "company_key", length = 255, unique = true, nullable = false)
    var companyKey: String? = null

    @Column
    var enabled = false

    @Column(unique = true, nullable = false, length = 255)
    var name: String? = null

    @Column
    var domain: String? = null

    @Column
    var logo: String? = null

    @Column(name = "logo_absolute_path")
    var logoAbsolutePath: String? = null

    @Column(name = "signin_btn_color")
    var signInBtnColor: String? = null

    @Column(name = "reset_password_link")
    var resetPasswordLink: String? = null

    @Column(name = "create_account_link")
    var createAccountLink: String? = null

    @Column(name = "default_redirect_url")
    var defaultRedirectUrl: String? = null

    @Column(name = "enable_config_panel")
    var enableConfigPanel: Boolean? = null
}
