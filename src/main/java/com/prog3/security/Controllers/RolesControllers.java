package com.prog3.security.Controllers;

import com.prog3.security.Models.Role;
import com.prog3.security.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("api/roles")
public class RolesControllers {
    @Autowired
    private RoleRepository theRoleRepository;
    // Get and Post Mapping

    // GetMapping
    @GetMapping("")
    public List<Role> find(){
        return this.theRoleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Role findById(@PathVariable String id) {
        return this.theRoleRepository.findById(id).orElse(null);
    }

    // PostMapping
    @PostMapping // post modificar
    public Role create(@RequestBody Role newRole){
        return this.theRoleRepository.save(newRole);
    }

    // PutMapping
    @PutMapping("/{id}")
    public Role update(@PathVariable String id, @RequestBody Role newRole){
        Role actualRole = this.theRoleRepository.findById(id).orElse(null);
        if (actualRole != null){
            actualRole.setName(newRole.getName());
            actualRole.setDescription(newRole.getDescription());
            this.theRoleRepository.save(actualRole);
            return actualRole;
        }else{
            return null;
        }
    }

    // DeleteMapping
    @DeleteMapping({"/{id}"})
    public void delete(@PathVariable String id){
        this.theRoleRepository.findById(id).ifPresent(theRole -> this.theRoleRepository.delete(theRole));
    }
}
