package com.bittokazi.oauth2.auth.server.app.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import com.bittokazi.oauth2.auth.server.app.models.tenant.OauthClient;
import com.bittokazi.oauth2.auth.server.app.models.tenant.User;
import com.bittokazi.oauth2.auth.server.app.models.tenant.security.RoleOauth;
import com.bittokazi.oauth2.auth.server.app.models.tenant.security.UserOauth;
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository;
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository;
import com.bittokazi.oauth2.auth.server.config.CustomJdbcOAuth2AuthorizationConsentService;
import com.bittokazi.oauth2.auth.server.config.TenantContext;
import com.bittokazi.oauth2.auth.server.database.MultiTenantConnectionProviderImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Bitto Kazi
 */

@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    private OauthClientRepository oauthClientRepository;

    private MultiTenantConnectionProviderImpl multiTenantConnectionProviderImpl;

    private RegisteredClientRepository registeredClientRepository;

    public CustomUserDetailsService(UserRepository userRepository, OauthClientRepository oauthClientRepository,
                                    MultiTenantConnectionProviderImpl multiTenantConnectionProviderImpl,
                                    RegisteredClientRepository registeredClientRepository) {
        this.userRepository = userRepository;
        this.oauthClientRepository = oauthClientRepository;
        this.multiTenantConnectionProviderImpl = multiTenantConnectionProviderImpl;
        this.registeredClientRepository = registeredClientRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) {
        try {
            Optional<User> user = userRepository.findOneByUsernameIgnoreCase(username);
            if (!user.isPresent()) {
                throw new UsernameNotFoundException("user not found");
            }
            UserOauth userDetails = new UserOauth();
            userDetails.setUsername(user.get().getUsername());
            userDetails.setPassword(user.get().getPassword());
            userDetails.setRoles(user.get().getRoles().stream().map(role -> {
                RoleOauth roleOauth = new RoleOauth(role.getId(), role.getName(), role.getTitle());
                return roleOauth;
            }).collect(Collectors.toSet()));
            userDetails.setEmail(user.get().getEmail());
            userDetails.setAuthorities(user.get().getAuthorities());
            return userDetails;
        } catch (Exception e) {
            logger.error("ERROR During loading User ", e);
        }
        return null;
    }

}
