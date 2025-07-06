package com.prog3.security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Permission;
import com.prog3.security.Repositories.PermissionRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/permissions")
public class PermissionsController {

    @Autowired
    private PermissionRepository thePermissionRepository;

    @GetMapping("")
    public List<Permission> findAll() {
        return this.thePermissionRepository.findAll();
    }

    @GetMapping("{url}/{method}")
    public Permission findPermission(@PathVariable String url, @PathVariable String method) {
        System.out.println("Buscando permiso con URL: " + url + " y Método: " + method);
        return this.thePermissionRepository.getPermission(url, method);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Permission create(@RequestBody Permission theNewPermission) {
        return this.thePermissionRepository.save(theNewPermission);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/all")
    public List<Permission> createAll(@RequestBody List<Permission> theNewPermissions) {
        return this.thePermissionRepository.saveAll(theNewPermissions);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("{id}")
    public Permission update(@PathVariable String id, @RequestBody Permission thePermission) {
        Permission thePermissionToUpdate = this.thePermissionRepository
                .findById(id)
                .orElse(null);
        if (thePermissionToUpdate != null) {
            thePermissionToUpdate.setUrl(thePermission.getUrl());
            thePermissionToUpdate.setMethod(thePermission.getMethod());
            return this.thePermissionRepository.save(thePermissionToUpdate);
        } else {
            return null;
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        Permission thePermission = this.thePermissionRepository
                .findById(id)
                .orElse(null);
        if (thePermission != null) {
            this.thePermissionRepository.delete(thePermission);
        }
    }
}
