package com.prog3.security.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Permission;
import com.prog3.security.Models.Role;
import com.prog3.security.Models.RolePermission;
import com.prog3.security.Repositories.PermissionRepository;
import com.prog3.security.Repositories.RolePermissionRepository;
import com.prog3.security.Repositories.RoleRepository;

@CrossOrigin
@RestController
@RequestMapping("api/role-permission")
public class RolePermissionController {

    @Autowired
    private RolePermissionRepository theRolePermissionRepository;
    @Autowired
    private PermissionRepository thePermissionRepository;
    @Autowired
    private RoleRepository theRoleRepository;

    @GetMapping("")
    public List<RolePermission> findAll() {
        return this.theRolePermissionRepository.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("role/{roleId}/permission/{permissionId}")
    public RolePermission create(@PathVariable String roleId,
            @PathVariable String permissionId) {
        Role theRole = this.theRoleRepository.findById(roleId)
                .orElse(null);
        Permission thePermission = this.thePermissionRepository.findById((permissionId))
                .orElse(null);
        if (theRole != null && thePermission != null) {
            RolePermission newRolePermission = new RolePermission();
            newRolePermission.setRole(theRole);
            newRolePermission.setPermission(thePermission);
            return this.theRolePermissionRepository.save(newRolePermission);
        } else {
            return null;
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        RolePermission theRolePermission = this.theRolePermissionRepository
                .findById(id)
                .orElse(null);
        if (theRolePermission != null) {
            this.theRolePermissionRepository.delete(theRolePermission);
        }
    }

    @GetMapping("role/{roleId}")
    public List<RolePermission> findPermissionsByRole(@PathVariable String roleId) {
        return this.theRolePermissionRepository.getPermissionsByRole(roleId);
    }

    @GetMapping("most-used/{roleId}")
    public String getMostUsedPermissionByRole(@PathVariable String roleId) {
        // Obtener los RolePermission asociados al rol específico
        List<RolePermission> rolePermissions = this.theRolePermissionRepository.getPermissionsByRole(roleId);
        Map<String, Integer> permissionCount = new HashMap<>();

        // Contar las apariciones de cada permiso
        for (RolePermission rolePermission : rolePermissions) {
            if (rolePermission.getPermission() != null && rolePermission.getPermission().getUrl() != null) {
                String permissionUrl = rolePermission.getPermission().getUrl();
                permissionCount.put(permissionUrl, permissionCount.getOrDefault(permissionUrl, 0) + 1);
            }
        }

        // Encontrar el permiso más utilizado
        Optional<Map.Entry<String, Integer>> mostUsedPermission = permissionCount.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        // Retornar la URL del permiso más utilizado
        return mostUsedPermission.map(Map.Entry::getKey).orElse("No permissions found for this role");
    }

}
