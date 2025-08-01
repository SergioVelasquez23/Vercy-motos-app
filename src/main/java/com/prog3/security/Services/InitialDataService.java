package com.prog3.security.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.prog3.security.Models.Role;
import com.prog3.security.Repositories.RoleRepository;

@Component
public class InitialDataService implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default roles if they don't exist
        createRoleIfNotExists("SUPERADMIN", "Acceso completo a todas las funcionalidades del sistema");
        createRoleIfNotExists("ADMIN", "Acceso a administración y configuración del sistema");
        createRoleIfNotExists("MESERO", "Acceso limitado para crear pedidos y consultar información básica");
    }

    private void createRoleIfNotExists(String name, String description) {
        Role existingRole = roleRepository.findByName(name);
        if (existingRole == null) {
            Role newRole = new Role(name, description);
            roleRepository.save(newRole);
            System.out.println("Rol creado: " + name);
        }
    }
}
