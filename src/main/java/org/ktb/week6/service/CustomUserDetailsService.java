package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.CustomUserDetails;
import org.ktb.week6.entity.User;
import org.ktb.week6.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword());
    }
}
