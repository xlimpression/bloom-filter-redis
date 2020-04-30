package com.singhand.bloomFilter.service;

import com.singhand.bloomFilter.model.User;
import com.singhand.bloomFilter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(s);
        if (user == null) {
            throw new UsernameNotFoundException("User does not exist!");
        }
        return user;
    }

}
