package com.aissistant.demo.controllers;

import com.aissistant.demo.models.User;
import com.aissistant.demo.payload.request.AddPersonalDataRequest;
import com.aissistant.demo.payload.request.LoginRequest;
import com.aissistant.demo.payload.request.SignupRequest;
import com.aissistant.demo.payload.response.MessageResponse;
import com.aissistant.demo.services.Auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        return authService.registerUser(signupRequest);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request){
        return authService.logoutUser(request);
    }


    @GetMapping("/is_admin")
    public ResponseEntity<?> isAdmin(HttpServletRequest request){
        boolean admin = authService.isAdmin(request);
        MessageResponse msg = new MessageResponse(admin ? "true" : "false");
        return ResponseEntity.ok(msg);
    }
}
