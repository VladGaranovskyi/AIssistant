package com.aissistant.demo.services.Auth;

import com.aissistant.demo.payload.request.SignupRequest;
import com.aissistant.demo.payload.response.ErrorsResponse;
import com.aissistant.demo.repositories.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Data
public class AuthValidator {

    @Autowired
    UserRepository userRepository;

    /**
     * Validates the registration request.
     *
     * @param signupRequest The signup request containing user details.
     * @return ResponseEntity with errors if validation fails, otherwise returns false.
     */
    public ResponseEntity<?> validateRegisterRequest(SignupRequest signupRequest){
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

        return ResponseEntity.ok().body(false);
    }
}
