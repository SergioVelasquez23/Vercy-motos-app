package com.prog3.security.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ResponseService responseService;

    // Directorio donde se guardarán las imágenes
    private final Path imageLocation = Paths.get("src/main/resources/static/images/platos");

    public ImageController() {
        try {
            // Crear el directorio si no existe
            Files.createDirectories(imageLocation);
            
            // Verificar permisos de lectura/escritura
            if (!Files.isReadable(imageLocation) || !Files.isWritable(imageLocation)) {
                throw new RuntimeException("El directorio de imágenes no tiene permisos suficientes");
            }
            
            System.out.println("✅ Directorio de imágenes configurado: " + imageLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de imágenes", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                return responseService.badRequest("El archivo está vacío");
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return responseService.badRequest("El archivo debe ser una imagen");
            }

            // Validar nombre de archivo
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return responseService.badRequest("Nombre de archivo inválido");
            }

            // Generar nombre único para el archivo
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Guardar el archivo
            Path targetLocation = imageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Generar URL de acceso
            String imageUrl = "/images/platos/" + uniqueFilename;

            return responseService.success(imageUrl, "Imagen subida exitosamente");
        } catch (IOException e) {
            return responseService.internalError("Error al subir la imagen: " + e.getMessage());
        }
    }

    @GetMapping("/platos/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = imageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determinar el tipo de contenido
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/platos/{filename:.+}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable String filename) {
        try {
            Path filePath = imageLocation.resolve(filename).normalize();

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return responseService.success(null, "Imagen eliminada exitosamente");
            } else {
                return responseService.notFound("Imagen no encontrada");
            }
        } catch (IOException e) {
            return responseService.internalError("Error al eliminar la imagen: " + e.getMessage());
        }
    }

    // Nuevo endpoint para subir imágenes en base64
    @PostMapping("/upload-base64")
    public ResponseEntity<ApiResponse<String>> uploadImageBase64(@RequestBody Map<String, String> payload) {
        try {
            String base64 = payload.get("imageBase64");
            String fileName = payload.get("fileName");
            if (base64 == null || fileName == null) {
                return responseService.badRequest("Faltan datos");
            }
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
            String fileExtension = fileName.substring(fileName.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = imageLocation.resolve(uniqueFilename);
            Files.write(targetLocation, imageBytes);
            String imageUrl = "/images/platos/" + uniqueFilename;
            return responseService.success(imageUrl, "Imagen subida exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al subir la imagen: " + e.getMessage());
        }
    }
    
}
