package com.aissistant.demo.services;

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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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

    public ResponseEntity<?> registerUser(SignupRequest signupRequest){
        Set<String> errors = new HashSet<>();

        if(userRepository.existsByUsername(signupRequest.getUsername())){
            errors.add("Username is already taken");
        }
        if(userRepository.existsByEmail(signupRequest.getEmail())){
            errors.add("Email is already taken");
        }
        if(signupRequest.getUsername().length() > 20){
            errors.add("Username must be less than 20 characters");
        }
        if(signupRequest.getEmail().length() > 100){
            errors.add("Email must be less than 100 characters");
        }
        // Check if email exists by trying to send verification code
        if(signupRequest.getPassword().length() > 64 || signupRequest.getPassword().length() < 8){
            errors.add("Password must be from 8 to 64 characters long");
        }

        if(errors.toArray().length > 0){
            return ResponseEntity.badRequest()
                    .body(new ErrorsResponse(errors));
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

    public ResponseEntity<?> addPersonalData(AddPersonalDataRequest request, HttpServletRequest servletRequest){
        // Check data for errors
        Set<String> errors = new HashSet<>();

        if(request.getFirstName().length() > 30){
            errors.add("Your first name should be less than 30 characters");
        }
        if(request.getLastName().length() > 30){
            errors.add("Your last name should be less than 30 characters");
        }

        // Get cookie
        String cookie = jwtUtils.getJwtFromCookies(servletRequest);
        if(cookie == null){
            errors.add("You are not logged in");
        }
        String email = jwtUtils.getEmailFromJwtToken(cookie);

        if(errors.toArray().length > 0){
            return ResponseEntity.badRequest()
                    .body(new ErrorsResponse(errors));
        }


        // Check user Data
        Optional<User> user = userRepository.findByEmail(email);
        if(isPersonalDataEqual(request, user)){
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("You didn't change the data yet tried to edit it"));
        }
        // Update user Data
//        mongoTemplate.update(User.class)
//                .matching(Criteria.where("email").is(email))
//                .apply(new Update().set("firstName", request.getFirstName()).set("lastName", request.getLastName())
//                        .set("age", request.getAge()).set("avatarId", request.getAvatarId()))
//                .first();
        user.get().setFirstName(request.getFirstName());
        user.get().setLastName(request.getLastName());
        user.get().setAge(request.getAge());
        user.get().setAvatarId(request.getAvatarId());

        userRepository.save(user.get());

        return ResponseEntity.ok(new MessageResponse("added personal information"));
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

    private boolean isPersonalDataEqual(AddPersonalDataRequest request, Optional<User> user){
        return user.get().getFirstName() != null &&
                user.get().getLastName() != null &&
                user.get().getAge() != 0 &&
                user.get().getAvatarId() != 0 &&
                user.get().getFirstName().equals(request.getFirstName()) &&
                user.get().getLastName().equals(request.getLastName()) &&
                user.get().getAge() == request.getAge() &&
                user.get().getAvatarId() == request.getAvatarId();
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
