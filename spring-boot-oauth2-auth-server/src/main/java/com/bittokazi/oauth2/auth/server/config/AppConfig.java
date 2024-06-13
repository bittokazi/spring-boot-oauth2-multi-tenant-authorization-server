package com.bittokazi.oauth2.auth.server.config;

public class AppConfig {

    public static String HTTP_SCHEMA = System.getenv().getOrDefault("HTTP_SCHEMA", "http://");
    public static String KID = System.getenv().getOrDefault("KID", "");
    public static String CERT_PRIVATE_KEY_FILE = System.getenv().getOrDefault("CERT_PRIVATE_KEY_FILE", "");
    public static String CERT_PUBLIC_KEY_FILE = System.getenv().getOrDefault("CERT_PUBLIC_KEY_FILE", "");
    public static String REMEMBER_ME_KEY = System.getenv().getOrDefault(
            "REMEMBER_ME_KEY",
            "changeThisSecretInEnv"
    );
}