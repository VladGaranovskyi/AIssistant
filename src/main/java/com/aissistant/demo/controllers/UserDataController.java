package com.aissistant.demo.controllers;

import com.aissistant.demo.models.User;
import com.aissistant.demo.payload.request.AddPersonalDataRequest;
import com.aissistant.demo.services.Auth.AuthService;
import com.aissistant.demo.services.UserDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/userdata")
public class UserDataController {

    @Autowired
    private UserDataService userDataService;
    @Autowired
    private AuthService authService;

    @PostMapping("/add_personal_data")
    public ResponseEntity<?> addPersonalData(@Valid @RequestBody AddPersonalDataRequest addPersonalDataRequest, HttpServletRequest request){
        return userDataService.addPersonalData(addPersonalDataRequest, request);
    }
    @GetMapping("/my_user_data")
    public ResponseEntity<Optional<User>> getUser(HttpServletRequest request){
        return new ResponseEntity<Optional<User>>(authService.getUserDataFromCookies(request), HttpStatus.OK);
    }

}
