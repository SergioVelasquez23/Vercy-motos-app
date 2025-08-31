package com.prog3.security.Controllers;

import com.prog3.security.DTOs.NegocioInfoRequest;
import com.prog3.security.Models.NegocioInfo;
import com.prog3.security.Repositories.NegocioInfoRepository;
import com.prog3.security.Services.FileStorageService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("api/negocio")
public class NegocioInfoController {

    @Autowired
    private NegocioInfoRepository negocioInfoRepository;

    @Autowired
    private ResponseService responseService;

    /**
     * Obtener la información del negocio
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<NegocioInfo>> getInfoNegocio() {
        try {
            // Normalmente solo habrá un documento de configuración del negocio
            List<NegocioInfo> negocioInfoList = negocioInfoRepository.findAll();

            if (negocioInfoList.isEmpty()) {
                return responseService.notFound("No se ha configurado la información del negocio");
            }

            // Retornar el primer documento encontrado
            return responseService.success(negocioInfoList.get(0), "Información del negocio obtenida exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener información del negocio: " + e.getMessage());
        }
    }

    /**
     * Crear o actualizar la información del negocio
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<NegocioInfo>> saveInfoNegocio(@RequestBody NegocioInfoRequest request) {
        try {
            // Verificar si existe información previa
            List<NegocioInfo> existingInfo = negocioInfoRepository.findAll();
            NegocioInfo negocioInfo;

            if (!existingInfo.isEmpty()) {
                // Actualizar el documento existente
                negocioInfo = existingInfo.get(0);
            } else {
                // Crear un nuevo documento
                negocioInfo = new NegocioInfo();
            }

            // Mapear los datos del request al modelo
            negocioInfo.setNombre(request.getNombre());
            negocioInfo.setNit(request.getNit());
            negocioInfo.setContacto(request.getContacto());
            negocioInfo.setEmail(request.getEmail());
            negocioInfo.setDireccion(request.getDireccion());
            negocioInfo.setPais(request.getPais());
            negocioInfo.setDepartamento(request.getDepartamento());
            negocioInfo.setCiudad(request.getCiudad());
            negocioInfo.setTelefono(request.getTelefono());
            negocioInfo.setPaginaWeb(request.getPaginaWeb());
            negocioInfo.setProductosConIngredientes(request.isProductosConIngredientes());
            negocioInfo.setUtilizoMesas(request.isUtilizoMesas());
            negocioInfo.setEnvioADomicilio(request.isEnvioADomicilio());
            negocioInfo.setCostosEnvio(request.getCostosEnvio());
            negocioInfo.setTipoDocumento(request.getTipoDocumento());
            negocioInfo.setPrefijo(request.getPrefijo());
            negocioInfo.setNumeroInicio(request.getNumeroInicio());
            negocioInfo.setPorcentajePropinaSugerida(request.getPorcentajePropinaSugerida());
            negocioInfo.setNombreDocumento(request.getNombreDocumento());
            negocioInfo.setNota1(request.getNota1());
            negocioInfo.setNota2(request.getNota2());
            negocioInfo.setLogoUrl(request.getLogoUrl());

            // Guardar en la base de datos
            NegocioInfo savedInfo = negocioInfoRepository.save(negocioInfo);

            if (!existingInfo.isEmpty()) {
                return responseService.success(savedInfo, "Información del negocio actualizada exitosamente");
            } else {
                return responseService.created(savedInfo, "Información del negocio creada exitosamente");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al guardar información del negocio: " + e.getMessage());
        }
    }

    /**
     * Actualizar la información del negocio
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NegocioInfo>> updateInfoNegocio(
            @PathVariable String id,
            @RequestBody NegocioInfoRequest request) {
        try {
            NegocioInfo negocioInfo = negocioInfoRepository.findById(id).orElse(null);

            if (negocioInfo == null) {
                return responseService.notFound("No se encontró información del negocio con el ID: " + id);
            }

            // Actualizar los campos
            negocioInfo.setNombre(request.getNombre());
            negocioInfo.setNit(request.getNit());
            negocioInfo.setContacto(request.getContacto());
            negocioInfo.setEmail(request.getEmail());
            negocioInfo.setDireccion(request.getDireccion());
            negocioInfo.setPais(request.getPais());
            negocioInfo.setDepartamento(request.getDepartamento());
            negocioInfo.setCiudad(request.getCiudad());
            negocioInfo.setTelefono(request.getTelefono());
            negocioInfo.setPaginaWeb(request.getPaginaWeb());
            negocioInfo.setProductosConIngredientes(request.isProductosConIngredientes());
            negocioInfo.setUtilizoMesas(request.isUtilizoMesas());
            negocioInfo.setEnvioADomicilio(request.isEnvioADomicilio());
            negocioInfo.setCostosEnvio(request.getCostosEnvio());
            negocioInfo.setTipoDocumento(request.getTipoDocumento());
            negocioInfo.setPrefijo(request.getPrefijo());
            negocioInfo.setNumeroInicio(request.getNumeroInicio());
            negocioInfo.setPorcentajePropinaSugerida(request.getPorcentajePropinaSugerida());
            negocioInfo.setNombreDocumento(request.getNombreDocumento());
            negocioInfo.setNota1(request.getNota1());
            negocioInfo.setNota2(request.getNota2());
            negocioInfo.setLogoUrl(request.getLogoUrl());

            // Guardar en la base de datos
            NegocioInfo updatedInfo = negocioInfoRepository.save(negocioInfo);
            return responseService.success(updatedInfo, "Información del negocio actualizada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar información del negocio: " + e.getMessage());
        }
    }

    /**
     * Eliminar la información del negocio (solo para administradores)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteInfoNegocio(@PathVariable String id) {
        try {
            NegocioInfo negocioInfo = negocioInfoRepository.findById(id).orElse(null);

            if (negocioInfo == null) {
                return responseService.notFound("No se encontró información del negocio con el ID: " + id);
            }

            negocioInfoRepository.delete(negocioInfo);
            return responseService.success(null, "Información del negocio eliminada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar información del negocio: " + e.getMessage());
        }
    }

    /**
     * Subir logo del negocio
     */
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/logo")
    public ResponseEntity<ApiResponse<NegocioInfo>> updateLogo(@RequestParam("file") MultipartFile file) {
        try {
            List<NegocioInfo> existingInfo = negocioInfoRepository.findAll();

            if (existingInfo.isEmpty()) {
                return responseService.notFound("No se ha configurado la información del negocio");
            }

            NegocioInfo negocioInfo = existingInfo.get(0);

            // Si ya hay un logo, eliminarlo primero
            if (negocioInfo.getLogoUrl() != null && !negocioInfo.getLogoUrl().isEmpty()) {
                fileStorageService.deleteFile(negocioInfo.getLogoUrl());
            }

            // Guardar el nuevo logo
            String logoUrl = fileStorageService.storeFile(file);
            negocioInfo.setLogoUrl(logoUrl);

            NegocioInfo updatedInfo = negocioInfoRepository.save(negocioInfo);
            return responseService.success(updatedInfo, "Logo del negocio actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar logo del negocio: " + e.getMessage());
        }
    }
}
