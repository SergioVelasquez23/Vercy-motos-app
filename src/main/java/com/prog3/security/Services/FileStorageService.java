package com.prog3.security.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "src/main/resources/static/images/negocio/";

    public FileStorageService() {
        // Crear directorio si no existe
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Guarda un archivo en el servidor
     *
     * @param file El archivo a guardar
     * @return La URL relativa del archivo guardado
     * @throws IOException Si ocurre un error al guardar el archivo
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Normalizar nombre de archivo
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generar nombre único para evitar colisiones
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Guardar archivo
        Path targetLocation = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), targetLocation);

        // Retornar URL relativa
        return "/images/negocio/" + fileName;
    }

    /**
     * Elimina un archivo del servidor
     *
     * @param fileUrl La URL relativa del archivo
     * @return true si se eliminó correctamente, false si no
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        // Extraer nombre del archivo de la URL
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir + fileName);

        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}
