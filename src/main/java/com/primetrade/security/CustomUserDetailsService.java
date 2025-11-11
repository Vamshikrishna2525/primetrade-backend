package com.primetrade.security;

import com.primetrade.model.User;
import com.primetrade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        String role = u.getRole() != null ? u.getRole().name() : "ROLE_USER";
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }
}