package com.vpm.Accounts.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.Users;
import com.vpm.Accounts.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public Users saveUsers(Users user){

        return repo.save(user);
    }
    
}
