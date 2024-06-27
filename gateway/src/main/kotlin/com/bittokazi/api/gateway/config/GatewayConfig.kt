package com.bittokazi.api.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * @author Bitto Kazi
 */
@Component
class GatewayConfig {
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route { r: PredicateSpec ->
                r.path("/app/**")
                    .filters { f: GatewayFilterSpec -> f.preserveHostHeader() }
                    .uri(System.getenv().getOrDefault("GATEWAY_FRONTEND_SERVICE", "http://127.0.0.1:4200"))
            }
            .route { r: PredicateSpec ->
                r.path("/**")
                    .filters { f: GatewayFilterSpec -> f.preserveHostHeader() }
                    .uri(System.getenv().getOrDefault("GATEWAY_BACKEND_SERVICE", "http://127.0.0.1:5010"))
            }.build()
    }
}
