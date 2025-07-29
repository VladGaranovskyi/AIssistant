package com.aissistant.demo.services;

import com.aissistant.demo.models.User;
import com.aissistant.demo.payload.request.AddPersonalDataRequest;
import com.aissistant.demo.payload.response.ErrorsResponse;
import com.aissistant.demo.payload.response.MessageResponse;
import com.aissistant.demo.repositories.UserRepository;
import com.aissistant.demo.security.jwt.JwtUtils;
import com.aissistant.demo.services.Auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserDataService {
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;


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

}
