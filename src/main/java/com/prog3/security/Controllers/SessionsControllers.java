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

import com.prog3.security.Models.Session;
import com.prog3.security.Models.User;
import com.prog3.security.Repositories.SessionRepository;
import com.prog3.security.Repositories.UserRepository;

@CrossOrigin
@RestController
@RequestMapping("api/sessions")
public class SessionsControllers {

    @Autowired
    SessionRepository theSessionRepository;
    @Autowired
    UserRepository theUserRepository;

    @GetMapping("")
    public List<Session> find() {
        List<Session> sessions = this.theSessionRepository.findAll();
        System.out.println(sessions); // Verifica qué se está devolviendo
        return sessions;
    }

    @GetMapping("/{id}")
    public Session findById(@PathVariable String id) {
        return this.theSessionRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Session create(@RequestBody Session newSession) {
        return this.theSessionRepository.save(newSession);
    }

    @PostMapping("/create")
    public Session createSession(@RequestBody Session newSession) {
        return this.theSessionRepository.save(newSession);
    }

    @PutMapping("/{id}")
    public Session update(@PathVariable String id, @RequestBody Session newSession) {
        Session actualSession = this.theSessionRepository.findById(id).orElse(null);
        if (actualSession != null) {
            actualSession.setToken(newSession.getToken());
            actualSession.setUser(newSession.getUser());
            this.theSessionRepository.save(actualSession);
            return actualSession;
        } else {
            return null;
        }
    }

    @DeleteMapping({"/{id}"})
    public void delete(@PathVariable String id) {
        this.theSessionRepository.findById(id).ifPresent(theSession -> this.theSessionRepository.delete(theSession));
    }

    @PostMapping("/{sessionId}/user/{userId}")
    public String matchUser(@PathVariable String userId, @PathVariable String sessionId) {
        Session theSession = this.theSessionRepository.findById(sessionId).orElse(null);
        User theUser = this.theUserRepository.findById(userId).orElse(null);

        if (theSession != null && theUser != null) {
            theSession.setUser(theUser);
            this.theSessionRepository.save(theSession);
            return "User matched to session";
        } else {
            return "User or session not found";

        }
    }

    @GetMapping("/user/{userId}")
    public List<Session> getSessionByUser(@PathVariable String userId) {
        return this.theSessionRepository.getSessionByUser(userId);
    }

}
