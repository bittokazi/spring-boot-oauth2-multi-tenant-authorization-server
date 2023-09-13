package com.bittokazi.oauth2.auth.server.app.controllers;

import com.bittokazi.oauth2.auth.server.app.models.tenant.User;
import com.bittokazi.oauth2.auth.server.app.services.user.UserService;
import com.bittokazi.oauth2.auth.server.app.services.user.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Bitto Kazi
 */

@RestController
@RequestMapping("/api")
public class UserController {

    private UserService userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/users")
    public Object getUsers(int page, int count) {
        return userService.getUsers(page, count);
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PostMapping("/users")
    public Object addUser(@RequestBody User user, HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) {
        return userService.addUser(user, httpServletRequest, httpServletResponse, false, false);
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/users/{id}")
    public Object getUser(@PathVariable String id, HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) {
        return userService.getUser(id, httpServletRequest, httpServletResponse);
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PutMapping("/users/{id}")
    public Object updateUser(@RequestBody User user, HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse) {
        return userService.updateUser(user, httpServletRequest, httpServletResponse);
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PutMapping("/users/{id}/update/password")
    public Object updateUserPassword(@RequestBody User user, HttpServletResponse httpServletResponse) {
        return userService.updateUserPassword(user, httpServletResponse);
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_user:read:whoami') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/users/whoami")
    public Object whoAmI(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return userService.whoAmI(httpServletRequest, httpServletResponse);
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PostMapping("/users/search/username")
    public Object getByUsername(@RequestBody User user) {
        return userService.getByUsername(user);
    }

    @PreAuthorize("hasAuthority('SCOPE_user:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    public Object getByEmail(@RequestBody User user) {
        return userService.getByEmail(user);
    }

}

