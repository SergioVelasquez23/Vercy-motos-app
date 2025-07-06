package com.prog3.security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.User;
import com.prog3.security.Repositories.UserRepository;
import com.prog3.security.Services.EncryptionService;

@CrossOrigin
@RestController
@RequestMapping("api/users")
public class UsersControllers {

    @Autowired
    UserRepository theUserRepository;
    @Autowired
    EncryptionService theEncryptionService;

    @GetMapping("")
    public List<User> find() {
        return this.theUserRepository.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable String id) {
        return this.theUserRepository.findById(id).orElse(null);
    }

    @PostMapping
    public User create(@RequestBody User newUser) {
        newUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));
        return this.theUserRepository.save(newUser);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable String id, @RequestBody User newUser) {
        User actualUser = this.theUserRepository.findById(id).orElse(null);
        if (actualUser != null) {
            actualUser.setName(newUser.getName());
            actualUser.setEmail(newUser.getEmail());
            actualUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));
            this.theUserRepository.save(actualUser);
            return actualUser;
        } else {
            return null;
        }
    }

    @DeleteMapping({"/{id}"})
    public void delete(@PathVariable String id) {
        this.theUserRepository.findById(id).ifPresent(theUser -> this.theUserRepository.delete(theUser));
    }

}
