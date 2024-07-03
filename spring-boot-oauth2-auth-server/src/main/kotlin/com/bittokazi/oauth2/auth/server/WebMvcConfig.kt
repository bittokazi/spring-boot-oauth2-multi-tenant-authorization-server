package com.bittokazi.oauth2.auth.server

import com.bittokazi.oauth2.auth.server.config.AppConfig
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebMvcConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        if (!registry.hasMappingForPattern("/assets/**")) {
            registry
                .addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/assets/")
        }
        if(!registry.hasMappingForPattern("/tenant-assets/**")) {
            registry
                .addResourceHandler("/tenant-assets/**")
                .addResourceLocations("file:///${AppConfig.TEMPLATE_FOLDER_BASE}")
                .setCachePeriod(0)
        }
    }
}
