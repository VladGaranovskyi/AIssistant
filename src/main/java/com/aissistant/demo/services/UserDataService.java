package com.aissistant.demo.services;

import com.aissistant.demo.models.User;
import com.aissistant.demo.payload.request.AddPersonalDataRequest;
import com.aissistant.demo.payload.response.ErrorsResponse;
import com.aissistant.demo.payload.response.MessageResponse;
import com.aissistant.demo.repositories.UserRepository;
import com.aissistant.demo.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class UserDataService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> addPersonalData(AddPersonalDataRequest request, HttpServletRequest servletRequest) {

        Set<String> errors = new HashSet<>();

        // Basic validation
        if (request.getName() == null || request.getName().length() > 30) {
            errors.add("Your name should be non-null and less than 30 characters");
        }

        if (request.getBio() == null || request.getBio().length() > 500) {
            errors.add("Your bio should be non-null and less than 500 characters");
        }

        if (request.getExpertiseTags() == null || request.getExpertiseTags().isEmpty()) {
            errors.add("Expertise tags cannot be empty");
        }

        // Get JWT from cookie
        String cookie = jwtUtils.getJwtFromCookies(servletRequest);
        if (cookie == null) {
            errors.add("You are not logged in");
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorsResponse(errors));
        }

        String email = jwtUtils.getEmailFromJwtToken(cookie);

        // Find user
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            errors.add("User not found");
            return ResponseEntity.badRequest().body(new ErrorsResponse(errors));
        }

        User user = optionalUser.get();

        // Check if nothing changed
        if (isUserDataEqual(request, user)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("You didn't change the data yet tried to edit it"));
        }

        // Update user fields directly
        user.setName(request.getName());
        user.setBio(request.getBio());

        // Replace expertiseTags map
        Map<String, Double> expertiseTags = request.getExpertiseTags();
        user.setExpertiseTags(expertiseTags);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Personal data updated successfully"));
    }

    private boolean isUserDataEqual(AddPersonalDataRequest request, User user) {

        boolean sameName = user.getName() != null && user.getName().equals(request.getName());
        boolean sameBio = user.getBio() != null && user.getBio().equals(request.getBio());
        boolean sameExpertise = user.getExpertiseTags() != null && user.getExpertiseTags().equals(request.getExpertiseTags());

        return sameName && sameBio && sameExpertise;
    }
}
