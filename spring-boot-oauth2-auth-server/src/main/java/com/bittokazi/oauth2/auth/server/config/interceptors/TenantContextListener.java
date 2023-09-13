package com.bittokazi.oauth2.auth.server.config.interceptors;

import com.bittokazi.oauth2.auth.server.app.models.master.Tenant;
import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository;
import com.bittokazi.oauth2.auth.server.config.TenantContext;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;
import java.util.Optional;

public class TenantContextListener implements ServletRequestListener {

    private TenantRepository tenantRepository;

    public TenantContextListener(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
        String host = request.getHeader("host").replace("www.", "");
        Optional<Tenant> tenantOptional = Optional.empty();
        Optional<Tenant> dataTenantOptional = Optional.empty();
        if(Objects.nonNull(host)) {
            tenantOptional = tenantRepository.findOneByDomain(host);
        }
        if(!tenantOptional.isPresent()) {
            String header = request.getHeader("X-DATA-TENANT");
            if(Objects.nonNull(header)) {
                dataTenantOptional = tenantRepository.findOneByCompanyKey(header);
            }
        }

        if(tenantOptional.isPresent()) {
            TenantContext.setCurrentTenant(tenantOptional.get().getCompanyKey());
            TenantContext.setCurrentDataTenant(tenantOptional.get().getCompanyKey());
        } else {
            TenantContext.setCurrentTenant("public");
            if(dataTenantOptional.isPresent()) {
                TenantContext.setCurrentDataTenant(dataTenantOptional.get().getCompanyKey());
            } else {
                TenantContext.setCurrentDataTenant("public");
            }
        }
        TenantContext.setCurrentIssuerUri(request.getScheme()+request.getHeader("host"));
//        System.out.println(
//                ">>>>>>>>>>>>>>>>>>>>>>TTTTTTTTTTTTTTTTTTTTTTEEEEEEEEENNNNNNNNNEEEEEEEEEETTTTTTTTTT>>>>>>>>>>>>>>>"
//                        + TenantContext.getCurrentTenant());
    }
}

