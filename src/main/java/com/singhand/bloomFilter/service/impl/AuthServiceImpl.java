package com.singhand.bloomFilter.service.impl;

import com.singhand.bloomFilter.model.User;
import com.singhand.bloomFilter.repository.UserRepository;
import com.singhand.bloomFilter.service.AuthService;
import com.singhand.bloomFilter.util.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private JwtTokenUtil jwtTokenUtil;
    private UserRepository userRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                           JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Override
    public String login( String username, String password ) {

        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken( username, password );

        final Authentication authentication = authenticationManager.authenticate(upToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = userDetailsService.loadUserByUsername( username );
        final String token = jwtTokenUtil.generateToken(userDetails);
        return token;
    }

    @Override
    public User register(User userToAdd ) {

        final String username = userToAdd.getUsername();
        if( userRepository.findByUsername(username)!=null ) {
            return null;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        final String rawPassword = userToAdd.getPassword();
        userToAdd.setPassword( encoder.encode(rawPassword) );
        return userRepository.save(userToAdd);
    }
}