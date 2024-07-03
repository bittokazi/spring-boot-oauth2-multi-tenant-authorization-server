package com.bittokazi.oauth2.auth.server.config.thymeleaf

import com.bittokazi.oauth2.auth.server.config.AppConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.FileTemplateResolver

@Configuration
class ThymeleafConfig {

    @Bean
    fun secondaryTemplateResolver(): FileTemplateResolver {
        val fileTemplateResolver = FileTemplateResolver()
        fileTemplateResolver.prefix = AppConfig.LAYOUT_FOLDER_BASE
        fileTemplateResolver.suffix = ".html"
        fileTemplateResolver.templateMode = TemplateMode.HTML
        fileTemplateResolver.characterEncoding = "UTF-8"
        fileTemplateResolver.order = 1
        fileTemplateResolver.checkExistence = true
        fileTemplateResolver.isCacheable = false

        return fileTemplateResolver
    }
}
