package com.vpm.Accounts.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vpm.Accounts.Service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.vpm.Accounts.Entity.Users;


@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")
public class UsersController {

    @Autowired
    private UserService service;

    @PostMapping
    public Users saveUser(@RequestBody Users user) {
        return service.saveUsers(user);
    }
    
    
}
