package com.Final_Project.Event_Booking.security;

import com.Final_Project.Event_Booking.exception.custom.UserNotFoundException;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username:"+username+" not found!"));

        return new CustomUserDetails(user);
    }
}
