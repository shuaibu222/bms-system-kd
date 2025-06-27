package com.shuaibu.security;

import com.shuaibu.model.UserModel;
import com.shuaibu.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        if (!user.getIsActive().equals("true")) {
            throw new UsernameNotFoundException("User is not active");
        }

        // Convert single string role into a list of authorities
        Collection<GrantedAuthority> authorities = 
            user.getRole() != null ? 
            java.util.Arrays.stream(user.getRole().split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()) 
            : java.util.Collections.emptyList();

        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
