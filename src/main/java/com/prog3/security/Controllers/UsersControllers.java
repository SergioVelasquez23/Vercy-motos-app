package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Role;
import com.prog3.security.Models.User;
import com.prog3.security.Models.UserRole;
import com.prog3.security.Repositories.RoleRepository;
import com.prog3.security.Repositories.UserRepository;
import com.prog3.security.Repositories.UserRoleRepository;
import com.prog3.security.Services.EncryptionService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/users")
public class UsersControllers {

    @Autowired
    UserRepository theUserRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    EncryptionService theEncryptionService;
    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<User>>> find() {
        try {
            List<User> users = this.theUserRepository.findAll();
            return responseService.success(users, "Usuarios obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener usuarios: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> findById(@PathVariable String id) {
        try {
            User user = this.theUserRepository.findById(id).orElse(null);
            if (user == null) {
                return responseService.notFound("Usuario no encontrado con ID: " + id);
            }
            return responseService.success(user, "Usuario encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar usuario: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> create(@RequestBody User newUser) {
        try {
            // Validar que no exista un usuario con el mismo email
            User existingUser = this.theUserRepository.getUserByEmail(newUser.getEmail());
            if (existingUser != null) {
                return responseService.conflict("Ya existe un usuario con el email: " + newUser.getEmail());
            }

            // Validaciones básicas
            if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
                return responseService.badRequest("El nombre es obligatorio");
            }

            if (newUser.getEmail() == null || newUser.getEmail().trim().isEmpty()) {
                return responseService.badRequest("El email es obligatorio");
            }

            if (newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
                return responseService.badRequest("La contraseña es obligatoria");
            }

            // Establecer campos de auditoría
            newUser.setFechaCreacion(LocalDateTime.now());
            newUser.setFechaActualizacion(LocalDateTime.now());
            Boolean activo = newUser.isActivo();
            if (activo == null) {
                newUser.setActivo(true); // Por defecto activo
            }

            newUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));
            User savedUser = this.theUserRepository.save(newUser);

            // Assign default MESERO role to new users
            Role meseroRole = this.roleRepository.findByName("MESERO");
            if (meseroRole != null) {
                UserRole newUserRole = new UserRole();
                newUserRole.setUser(savedUser);
                newUserRole.setRole(meseroRole);
                userRoleRepository.save(newUserRole);
            }

            return responseService.created(savedUser, "Usuario creado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable String id, @RequestBody User newUser) {
        try {
            User actualUser = this.theUserRepository.findById(id).orElse(null);
            if (actualUser == null) {
                return responseService.notFound("Usuario no encontrado con ID: " + id);
            }

            // Validar que el nuevo email no esté en uso por otro usuario
            User existingByEmail = this.theUserRepository.getUserByEmail(newUser.getEmail());
            if (existingByEmail != null && !existingByEmail.get_id().equals(id)) {
                return responseService.conflict("Ya existe otro usuario con el email: " + newUser.getEmail());
            }

            // Validaciones básicas
            if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
                return responseService.badRequest("El nombre es obligatorio");
            }

            if (newUser.getEmail() == null || newUser.getEmail().trim().isEmpty()) {
                return responseService.badRequest("El email es obligatorio");
            }

            // Actualizar campos
            actualUser.setName(newUser.getName());
            actualUser.setEmail(newUser.getEmail());
            
            // Solo actualizar la contraseña si se proporciona una nueva
            if (newUser.getPassword() != null && !newUser.getPassword().trim().isEmpty()) {
                actualUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));
            }
            
            // Actualizar campo activo si se proporciona
            Boolean activoNuevo = newUser.isActivo();
            if (activoNuevo != null) {
                actualUser.setActivo(activoNuevo);
            }
            
            // Actualizar fecha de modificación
            actualUser.setFechaActualizacion(LocalDateTime.now());
            
            User updatedUser = this.theUserRepository.save(actualUser);
            return responseService.success(updatedUser, "Usuario actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            User user = this.theUserRepository.findById(id).orElse(null);
            if (user == null) {
                return responseService.notFound("Usuario no encontrado con ID: " + id);
            }

            // Eliminar relaciones usuario-rol primero
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            userRoleRepository.deleteAll(userRoles);
            
            // Luego eliminar el usuario
            this.theUserRepository.delete(user);
            return responseService.success(null, "Usuario eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar usuario: " + e.getMessage());
        }
    }

}
