package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.repository.FmUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FmUserDetailsService implements UserDetailsService {

    @Autowired
    private FmUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .filter(user -> user.getIsActive().equalsIgnoreCase("Y"))
                .orElseThrow(() -> new UsernameNotFoundException("User not found or inactive: " + username));
    }
}
