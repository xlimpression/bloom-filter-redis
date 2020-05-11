package com.singhand.bloomFilter.controller;

import static com.google.common.base.Preconditions.*;
import com.singhand.bloomFilter.model.User;
import com.singhand.bloomFilter.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sun.swing.StringUIClientPropertyKey;

@RestController
public class JwtAuthController {

    private AuthService authService;

    public JwtAuthController(AuthService authService) {
        this.authService = authService;
    }

    // login
    @RequestMapping(value = "/authentication/login", method = RequestMethod.POST)
    public String createToken( String username,String password ) throws AuthenticationException {
        return authService.login( username, password );
    }

    // register
    @RequestMapping(value = "/authentication/register", method = RequestMethod.POST)
    public User register(
            @RequestBody
                    User addedUser )
            throws AuthenticationException {
        checkNotNull(addedUser);
        checkArgument(StringUtils.isNotBlank(addedUser.getUsername()));
        checkArgument(StringUtils.isNotBlank(addedUser.getPassword()));
        return authService.register(addedUser);
    }

}
