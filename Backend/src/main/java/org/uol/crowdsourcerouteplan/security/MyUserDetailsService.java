package org.uol.crowdsourcerouteplan.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.uol.crowdsourcerouteplan.dto.UserPrincipal;
import org.uol.crowdsourcerouteplan.model.User;
import org.uol.crowdsourcerouteplan.repository.UserRepo;


import java.util.Optional;

@Service
public class MyUserDetailsService {
    @Autowired
    private UserRepo userRepo;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUsername(username);
        if (user == null) {
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user not found");
        }

        return new UserPrincipal(user);
    }
}
