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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@CrossOrigin
@RestController
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

    // ================================
    // 📤 ENDPOINTS API (Para subir y gestionar imágenes)
    // ================================
    
    @PostMapping("/api/images/upload")
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

    // ================================
    // 🖼️ ENDPOINTS PARA SERVIR IMÁGENES (Acceso directo desde frontend)
    // ================================
    
    /**
     * Endpoint principal para servir imágenes desde /images/platos/{filename}
     * Este es el endpoint que usa el frontend para cargar las imágenes
     */
    @GetMapping("/images/platos/{filename:.+}")
    public ResponseEntity<Resource> getImageDirect(@PathVariable String filename) {
        return serveImage(filename);
    }
    
    /**
     * Endpoint API alternativo para servir imágenes desde /api/images/platos/{filename}
     */
    @GetMapping("/api/images/platos/{filename:.+}")
    public ResponseEntity<Resource> getImageAPI(@PathVariable String filename) {
        return serveImage(filename);
    }
    
    /**
     * Método helper para servir imágenes (reutilizado por ambos endpoints)
     * Busca en uploads/platos primero, luego en el directorio por defecto
     */
    private ResponseEntity<Resource> serveImage(String filename) {
        try {
            // ✅ CORREGIDO: Buscar primero en uploads/platos (directorio de producción)
            Path uploadsPath = Paths.get("uploads/platos").resolve(filename).normalize();
            Path defaultPath = imageLocation.resolve(filename).normalize();
            
            Path filePath = null;
            
            // Priorizar uploads/platos si existe
            if (Files.exists(uploadsPath)) {
                filePath = uploadsPath;
                System.out.println("🖼️ Sirviendo imagen desde uploads: " + uploadsPath.toAbsolutePath());
            } else if (Files.exists(defaultPath)) {
                filePath = defaultPath;
                System.out.println("🖼️ Sirviendo imagen desde directorio por defecto: " + defaultPath.toAbsolutePath());
            } else {
                System.out.println("❌ Imagen no encontrada: " + filename);
                System.out.println("   Buscado en: " + uploadsPath.toAbsolutePath());
                System.out.println("   Buscado en: " + defaultPath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
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
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache por 1 hora
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.err.println("❌ URL malformada: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            System.err.println("❌ Error de IO: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/api/images/platos/{filename:.+}")
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
    @PostMapping("/api/images/upload-base64")
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
    
    // ================================
    // 🔍 ENDPOINTS DE DEBUGGING PARA VERIFICAR ARCHIVOS
    // ================================
    
    /**
     * Endpoint para listar todas las imágenes disponibles (solo para debugging)
     */
    @GetMapping("/api/images/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listAvailableImages() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Verificar directorio uploads/platos
            Path uploadsDir = Paths.get("uploads/platos");
            List<String> uploadsFiles = new ArrayList<>();
            if (Files.exists(uploadsDir)) {
                try {
                    Files.list(uploadsDir)
                        .filter(Files::isRegularFile)
                        .forEach(file -> uploadsFiles.add(file.getFileName().toString()));
                } catch (IOException e) {
                    System.err.println("Error listando uploads: " + e.getMessage());
                }
            }
            
            // Verificar directorio por defecto
            List<String> defaultFiles = new ArrayList<>();
            if (Files.exists(imageLocation)) {
                try {
                    Files.list(imageLocation)
                        .filter(Files::isRegularFile)
                        .forEach(file -> defaultFiles.add(file.getFileName().toString()));
                } catch (IOException e) {
                    System.err.println("Error listando directorio por defecto: " + e.getMessage());
                }
            }
            
            result.put("uploadsDirectory", uploadsDir.toAbsolutePath().toString());
            result.put("uploadsExists", Files.exists(uploadsDir));
            result.put("uploadsFiles", uploadsFiles);
            result.put("defaultDirectory", imageLocation.toAbsolutePath().toString());
            result.put("defaultExists", Files.exists(imageLocation));
            result.put("defaultFiles", defaultFiles);
            result.put("totalFiles", uploadsFiles.size() + defaultFiles.size());
            
            return responseService.success(result, "Lista de archivos obtenida");
        } catch (Exception e) {
            return responseService.internalError("Error listando archivos: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint para verificar si una imagen específica existe
     */
    @GetMapping("/api/images/check/{filename:.+}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkImage(@PathVariable String filename) {
        try {
            Path uploadsPath = Paths.get("uploads/platos").resolve(filename).normalize();
            Path defaultPath = imageLocation.resolve(filename).normalize();
            
            Map<String, Object> result = new HashMap<>();
            result.put("filename", filename);
            result.put("uploadsPath", uploadsPath.toAbsolutePath().toString());
            result.put("uploadsExists", Files.exists(uploadsPath));
            result.put("defaultPath", defaultPath.toAbsolutePath().toString());
            result.put("defaultExists", Files.exists(defaultPath));
            result.put("anyExists", Files.exists(uploadsPath) || Files.exists(defaultPath));
            
            if (Files.exists(uploadsPath)) {
                result.put("fileSize", Files.size(uploadsPath));
                result.put("lastModified", Files.getLastModifiedTime(uploadsPath).toString());
            } else if (Files.exists(defaultPath)) {
                result.put("fileSize", Files.size(defaultPath));
                result.put("lastModified", Files.getLastModifiedTime(defaultPath).toString());
            }
            
            return responseService.success(result, "Verificación de archivo completada");
        } catch (Exception e) {
            return responseService.internalError("Error verificando archivo: " + e.getMessage());
        }
    }
    
}
