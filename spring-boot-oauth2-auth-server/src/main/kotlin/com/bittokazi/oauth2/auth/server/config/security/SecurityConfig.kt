package com.bittokazi.oauth2.auth.server.config.security

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.RegisteredClientRepositoryImpl
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.services.CustomUserDetailsService
import com.bittokazi.oauth2.auth.server.config.AppConfig
import com.bittokazi.oauth2.auth.server.config.TenantContext
import com.bittokazi.oauth2.auth.server.config.interceptors.TenantContextListener
import com.bittokazi.oauth2.auth.server.config.security.mfa.OtpFilter
import com.bittokazi.oauth2.auth.server.config.security.oauth2.CustomHttpStatusReturningLogoutSuccessHandler
import com.bittokazi.oauth2.auth.server.config.security.oauth2.CustomJdbcOAuth2AuthorizationConsentService
import com.bittokazi.oauth2.auth.server.config.security.oauth2.CustomJdbcOAuth2AuthorizationService
import com.bittokazi.oauth2.auth.server.database.MultiTenantConnectionProviderImpl
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationEndpointConfigurer
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OidcConfigurer
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OidcProviderConfigurationEndpointConfigurer
import org.springframework.security.oauth2.server.authorization.oidc.OidcProviderConfiguration
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.token.*
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.RememberMeTokenAlgorithm
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.ResourceTransactionManager
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
open class SecurityConfig(
    private val oauthClientRepository: OauthClientRepository,
    private val dataSource: DataSource,
    private val userRepository: UserRepository,
    private val multiTenantConnectionProviderImpl: MultiTenantConnectionProviderImpl,
    private val tenantRepository: TenantRepository,
    private val customHttpStatusReturningLogoutSuccessHandler: CustomHttpStatusReturningLogoutSuccessHandler,
    private val otpFilter: OtpFilter
) {

    @Bean
    @Order(1)
    @Throws(Exception::class)
    open fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .authorizationEndpoint { authorizationEndpoint: OAuth2AuthorizationEndpointConfigurer ->
                authorizationEndpoint.consentPage(
                    "/oauth2/consent"
                )
            }
            .oidc { oidcConfigurer: OidcConfigurer ->
                oidcConfigurer
                    .providerConfigurationEndpoint { providerConfigurationEndpoint: OidcProviderConfigurationEndpointConfigurer ->
                        providerConfigurationEndpoint
                            .providerConfigurationCustomizer { builder: OidcProviderConfiguration.Builder ->
                                builder
                                    .issuer(TenantContext.getCurrentIssuer())
                                    .authorizationEndpoint(TenantContext.getCurrentIssuer() + "/oauth2/authorize")
                                    .deviceAuthorizationEndpoint(TenantContext.getCurrentIssuer() + "/oauth2/device_authorization")
                                    .tokenEndpoint(TenantContext.getCurrentIssuer() + "/oauth2/token")
                                    .jwkSetUrl(TenantContext.getCurrentIssuer() + "/oauth2/jwks")
                                    .userInfoEndpoint(TenantContext.getCurrentIssuer() + "/userinfo")
                                    .endSessionEndpoint(TenantContext.getCurrentIssuer() + "/connect/logout")
                                    .tokenRevocationEndpoint(TenantContext.getCurrentIssuer() + "/oauth2/revoke")
                                    .tokenIntrospectionEndpoint(TenantContext.getCurrentIssuer() + "/oauth2/introspect")
                                    .build()
                            }
                    }
            }
        http
            .exceptionHandling { exceptions: ExceptionHandlingConfigurer<HttpSecurity?> ->
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        LoginUrlAuthenticationEntryPoint("/login"),
                        MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                    )
            }
            .oauth2ResourceServer { resourceServer: OAuth2ResourceServerConfigurer<HttpSecurity?> ->
                resourceServer
                    .jwt(Customizer.withDefaults())
            }
        return http.build()
    }

    @Bean
    @Order(2)
    @Throws(Exception::class)
    open fun defaultSecurityFilterChain(http: HttpSecurity, rememberMeServices: RememberMeServices?): SecurityFilterChain {
        http.csrf().disable()
        http
            .authorizeHttpRequests(
                Customizer { authorize ->
                    authorize
                        .requestMatchers(
                            "/oauth2/login", "/oauth2/refresh/token",
                            "/authorize_user", "/login", "/assets/**", "/otp-login",
                            "/app/**", "/public/api/tenants/info"
                        ).permitAll()
                        .anyRequest().authenticated()
                }
            )
            .addFilterBefore(otpFilter, UsernamePasswordAuthenticationFilter::class.java)
            .formLogin { form: FormLoginConfigurer<HttpSecurity?> ->
                form
                    .loginPage("/login")
                    .successHandler(LoginSuccessHandler())
            }
            .logout { logout: LogoutConfigurer<HttpSecurity?> ->
                logout.logoutSuccessHandler(
                    customHttpStatusReturningLogoutSuccessHandler
                )
            }
            .rememberMe { remember: RememberMeConfigurer<HttpSecurity?> ->
                remember
                    .rememberMeServices(rememberMeServices)
            }
            .oauth2ResourceServer { resourceServer: OAuth2ResourceServerConfigurer<HttpSecurity?> ->
                resourceServer
                    .jwt(Customizer.withDefaults())
            }

        return http.build()
    }

    @Bean
    open fun userDetailsService(): UserDetailsService {
        return CustomUserDetailsService(
            userRepository,
            oauthClientRepository,
            multiTenantConnectionProviderImpl,
            registeredClientRepository()
        )
    }

    @Bean
    open fun registeredClientRepository(): RegisteredClientRepository {
        return RegisteredClientRepositoryImpl(oauthClientRepository)
    }

    @Bean
    open fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = generateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(if (AppConfig.KID.isEmpty()) UUID.randomUUID().toString() else AppConfig.KID)
            .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    @Bean
    open fun jwtDecoder(jwkSource: JWKSource<SecurityContext?>?): JwtDecoder {
        val validator: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(JwtTimestampValidator(),
            JwtClaimValidator("tenant") { tenant: Any -> tenant == TenantContext.getCurrentTenant() })

        val jwtDecoder = OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource) as NimbusJwtDecoder
        jwtDecoder.setJwtValidator(validator)
        return jwtDecoder
    }

    @Bean
    open fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder()
            .build()
    }

    @Bean
    open fun authorizationService(): OAuth2AuthorizationService {
        return CustomJdbcOAuth2AuthorizationService(
            JdbcTemplate(dataSource),
            this.registeredClientRepository(),
            multiTenantConnectionProviderImpl
        )
    }

    @Bean
    open fun authorizationConsentService(): OAuth2AuthorizationConsentService {
        return CustomJdbcOAuth2AuthorizationConsentService(
            JdbcTemplate(dataSource),
            this.registeredClientRepository(),
            multiTenantConnectionProviderImpl
        )
    }

    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    open fun tokenGenerator(jwkSource: JWKSource<SecurityContext?>?): OAuth2TokenGenerator<*> {
        val jwtEncoder: JwtEncoder = NimbusJwtEncoder(jwkSource)
        val jwtGenerator = JwtGenerator(jwtEncoder)
        jwtGenerator.setJwtCustomizer(jwtCustomizer())
        val accessTokenGenerator = OAuth2AccessTokenGenerator()
        val refreshTokenGenerator = OAuth2RefreshTokenGenerator()
        return DelegatingOAuth2TokenGenerator(
            jwtGenerator, accessTokenGenerator, refreshTokenGenerator
        )
    }

    @Bean
    open fun jwtCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context: JwtEncodingContext ->
            val headers = context.jwsHeader
            if (OAuth2TokenType.ACCESS_TOKEN == context.tokenType) {
                context.claims.claims { claims: MutableMap<String?, Any> ->
                    claims["iss"] = TenantContext.getCurrentIssuer()!!
                    if (claims["sub"].toString() == context.registeredClient.clientId) {
                        val optionalOauthClient =
                            oauthClientRepository.findOneByClientId(context.registeredClient.clientId)
                        val scopes: MutableSet<String?> = if (optionalOauthClient.isPresent && Objects.nonNull(
                                (claims["scope"] as Set<String?>?)
                            )
                        ) (claims["scope"] as Set<String?>?)!!.stream().collect(Collectors.toSet()) else HashSet()
                        if (optionalOauthClient.isPresent) {
                            optionalOauthClient.get().scopeAsSet()!!.forEach(Consumer { s: String? ->
                                scopes.add(s)
                            })
                        }
                        claims["scope"] = scopes
                    } else {
                        val userOptional = userRepository.findOneByUsername(claims["sub"].toString())
                        val scopes: MutableSet<String?> = if (userOptional.isPresent && Objects.nonNull(
                                (claims["scope"] as Set<String?>?)
                            )
                        ) (claims["scope"] as Set<String?>?)!!.stream().collect(Collectors.toSet()) else HashSet()
                        if (userOptional.isPresent) {
                            userOptional.get().roles.forEach(Consumer { role: Role ->
                                scopes.add(role.name?.replace("ROLE_", ""))
                            })
                        }
                        claims["scope"] = scopes
                    }
                    claims["tenant"] = TenantContext.getCurrentTenant()!!
                }
            }
            if (OidcParameterNames.ID_TOKEN == context.tokenType.value) {
                context.claims.claims { claims: MutableMap<String?, Any?> ->
                    claims["iss"] = TenantContext.getCurrentIssuer()
                }
            }
        }
    }

    @Bean
    open fun tenantContextListener(): TenantContextListener {
        return TenantContextListener(this.tenantRepository)
    }

    @Bean
    @Primary
    open fun annotationDrivenTransactionManager(): PlatformTransactionManager {
        return object : ResourceTransactionManager {
            override fun getResourceFactory(): Any? {
                return null
            }

            @Throws(TransactionException::class)
            override fun getTransaction(definition: TransactionDefinition): TransactionStatus? {
                return null
            }

            @Throws(TransactionException::class)
            override fun commit(status: TransactionStatus) = Unit

            @Throws(TransactionException::class)
            override fun rollback(status: TransactionStatus) = Unit
        }
    }

    @Bean
    open fun rememberMeServices(userDetailsService: UserDetailsService?): RememberMeServices {
        val encodingAlgorithm = RememberMeTokenAlgorithm.SHA256
        val rememberMe = TokenBasedRememberMeServices(AppConfig.REMEMBER_ME_KEY, userDetailsService, encodingAlgorithm)
        rememberMe.setTokenValiditySeconds(3600 * 24 * 365)
        rememberMe.parameter = "remember-me"
        rememberMe.setMatchingAlgorithm(RememberMeTokenAlgorithm.MD5)
        return rememberMe
    }

    companion object {
        private fun generateRsaKey(): KeyPair {
            val keyPair: KeyPair
            try {
                if (AppConfig.CERT_PRIVATE_KEY_FILE.isEmpty() || AppConfig.CERT_PUBLIC_KEY_FILE.isEmpty()) {
                    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                    keyPairGenerator.initialize(2048)
                    keyPair = keyPairGenerator.generateKeyPair()
                } else {
                    var privateKeyContent = String(Files.readAllBytes(Paths.get(AppConfig.CERT_PRIVATE_KEY_FILE)))
                    var publicKeyContent = String(Files.readAllBytes(Paths.get(AppConfig.CERT_PUBLIC_KEY_FILE)))

                    privateKeyContent = privateKeyContent.replace("\\n".toRegex(), "")
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")

                    publicKeyContent = publicKeyContent.replace("\\n".toRegex(), "")
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")


                    val kf = KeyFactory.getInstance("RSA")

                    val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent))
                    val privKey = kf.generatePrivate(keySpecPKCS8)

                    val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
                    val pubKey = kf.generatePublic(keySpecX509) as RSAPublicKey

                    keyPair = KeyPair(pubKey, privKey)
                }
            } catch (ex: Exception) {
                throw IllegalStateException(ex)
            }
            return keyPair
        }
    }
}
