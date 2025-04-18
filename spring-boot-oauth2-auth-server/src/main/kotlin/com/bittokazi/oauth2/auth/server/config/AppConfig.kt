package com.bittokazi.oauth2.auth.server.config

object AppConfig {
    val APPLICATION_BACKEND_URL: String = System.getenv().getOrDefault(
        "APPLICATION_BACKEND_URL",
        "http://localhost:5020"
    )
    @JvmField
    var HTTP_SCHEMA: String = System.getenv().getOrDefault("HTTP_SCHEMA", "http://")
    var KID: String = System.getenv().getOrDefault("KID", "")
    var CERT_PRIVATE_KEY_FILE: String = System.getenv().getOrDefault("CERT_PRIVATE_KEY_FILE", "")
    var CERT_PUBLIC_KEY_FILE: String = System.getenv().getOrDefault("CERT_PUBLIC_KEY_FILE", "")
    var REMEMBER_ME_KEY: String = System.getenv().getOrDefault(
        "REMEMBER_ME_KEY",
        "changeThisSecretInEnv"
    )
    @JvmField
    var USE_X_AUTH_TENANT: Boolean = System.getenv().getOrDefault(
        "USE_X_AUTH_TENANT",
        "false"
    ).toBoolean()
    @JvmField
    var DEFAULT_APP_NAME: String = System.getenv()
        .getOrDefault("DEFAULT_APP_NAME", "AuthKit IDP")
    var DEFAULT_RESET_PASSWORD_LINK: String = System.getenv()
        .getOrDefault("DEFAULT_RESET_PASSWORD_LINK", "")
    var TEMPLATE_FOLDER_BASE: String = System.getenv()
        .getOrDefault("TEMPLATE_FOLDER_BASE", "/template-assets")
    var CERT_FOLDER_BASE: String = System.getenv().getOrDefault("CERT_FOLDER_BASE", "/certs")
    var VERSION_FILE: String = System.getenv()
        .getOrDefault("VERSION_FILE", "info.json")
    var VERSION: String = ""
}
