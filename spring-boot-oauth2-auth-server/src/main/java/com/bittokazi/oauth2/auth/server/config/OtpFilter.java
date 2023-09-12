package com.bittokazi.oauth2.auth.server.config;

import com.bittokazi.oauth2.auth.server.app.models.tenant.User;
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

//@Component
public class OtpFilter implements Filter {

//    private static final Logger logger = LoggerFactory.getLogger(Filter.class);

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private TwoFaService twoFaService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub
        System.out.println(">>>");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (request != null && request.getContentType() != null
                && Objects.equals(httpServletRequest.getServletPath(), "/login")) {
//			logger.info("User IP -> " + HttpReqRespUtils.getClientIpAddressIfServletRequestExist(httpServletRequest));
//			logger.info("User Agent -> " + HttpReqRespUtils.getUserAgent(httpServletRequest));
//
//			logger.info("Username -> " + httpServletRequest.getParameter("username"));
//			logger.info("GrantType -> " + httpServletRequest.getParameter("grant_type"));
//			logger.info("ClientInstanceId -> " + httpServletRequest.getParameter("client_instance_id"));

//            if ((httpServletRequest.getParameter("grant_type").equals("password")
//                    && !httpServletRequest.getParameterMap().containsKey("client_instance_id"))
//                    || (httpServletRequest.getParameter("grant_type").equals("password")
//                    && httpServletRequest.getParameterMap().containsKey("client_instance_id")
//                    && httpServletRequest.getParameter("client_instance_id").length() < 10)) {
//                RestResponseGenerator.instanceIdRequired(httpServletResponse);
//                return;
//            }

            Optional<User> user = userRepository
                    .findOneByUsernameIgnoreCase(httpServletRequest.getParameter("username"));
            if (user.isPresent()) {
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                if (bCryptPasswordEncoder.matches(httpServletRequest.getParameter("password"),
                        user.get().getPassword())) {
//                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
//                    httpServletResponse.getOutputStream().print(new ObjectMapper().writeValueAsString("OTP Required"));
//                    httpServletResponse.flushBuffer();
//                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
