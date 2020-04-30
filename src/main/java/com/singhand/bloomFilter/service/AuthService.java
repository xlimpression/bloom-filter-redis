package com.singhand.bloomFilter.service;

import com.singhand.bloomFilter.model.User;

public interface AuthService {

    User register( User userToAdd );
    String login( String username, String password );
}
