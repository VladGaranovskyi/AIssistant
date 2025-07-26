package com.aissistant.demo.services.Auth;

import com.aissistant.demo.models.ERole;
import com.aissistant.demo.models.Role;
import com.aissistant.demo.models.User;
import com.aissistant.demo.payload.request.AddPersonalDataRequest;
import com.aissistant.demo.payload.request.LoginRequest;
import com.aissistant.demo.payload.request.SignupRequest;
import com.aissistant.demo.payload.response.ErrorsResponse;
import com.aissistant.demo.payload.response.MessageResponse;
import com.aissistant.demo.payload.response.UserInfoResponse;
import com.aissistant.demo.repositories.RoleRepository;
import com.aissistant.demo.repositories.UserRepository;
import com.aissistant.demo.security.jwt.JwtUtils;
import com.aissistant.demo.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthValidator authValidator;

    public ResponseEntity<?> registerUser(SignupRequest signupRequest){

        ResponseEntity validated = authValidator.validateRegisterRequest(signupRequest);

        if (!validated.getStatusCode().is2xxSuccessful()){
            return validated; // Return errors if validation fails
        }

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        //Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        user.setRoles(roles);
        userRepository.save(user);

        // return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        return this.authenticateUser(new LoginRequest(signupRequest.getEmail(), signupRequest.getPassword()));
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(
                        jwtCookie.toString()
                ));
    }
    public ResponseEntity<?> logoutUser(HttpServletRequest servletRequest){
        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new MessageResponse(
                        "logged out"
                ));
    }

    public Optional<User> getUserDataFromCookies(HttpServletRequest servletRequest){
        String cookie = jwtUtils.getJwtFromCookies(servletRequest);
        if(cookie == null){
            return null;
        }
        String email = jwtUtils.getEmailFromJwtToken(cookie);

        return userRepository.findByEmail(email);
    }

    public boolean isAdmin(HttpServletRequest servletRequest){
        Optional<User> userOptional = getUserDataFromCookies(servletRequest);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            for( Role r : user.getRoles()){
                if(r.getName() == roleRepository.findByName(ERole.ROLE_ADMIN).get().getName()){
                    return true;
                }
            }
        }
        return false;
    }
}
