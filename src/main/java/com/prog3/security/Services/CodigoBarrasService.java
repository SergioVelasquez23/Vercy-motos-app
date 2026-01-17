package com.prog3.security.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.prog3.security.DTOs.EtiquetaCodigoBarrasDTO;
import com.prog3.security.DTOs.GenerarCodigoBarrasRequest;
import com.prog3.security.DTOs.ImprimirEtiquetasRequest;
import com.prog3.security.Exception.BusinessException;
import com.prog3.security.Exception.ResourceNotFoundException;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.Producto;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CodigoBarrasService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    /**
     * Genera un código de barras para un producto o ingrediente
     */
    public String generarCodigoBarras(GenerarCodigoBarrasRequest request) {
        String codigo;

        // Si tiene código personalizado, usarlo
        if (request.getCodigoPersonalizado() != null
                && !request.getCodigoPersonalizado().isEmpty()) {
            codigo = request.getCodigoPersonalizado();
        } else {
            // Usar el ID del item como código base
            codigo = request.getItemId();
        }

        // Validar y ajustar según el tipo de código
        codigo = ajustarCodigoSegunTipo(codigo, request.getTipoCodigo());

        // Guardar el código en el item correspondiente
        guardarCodigoEnItem(request.getItemId(), request.getTipoItem(), codigo);

        return codigo;
    }

    /**
     * Genera la imagen del código de barras
     */
    public byte[] generarImagenCodigoBarras(String codigo,
            GenerarCodigoBarrasRequest.TipoCodigoBarras tipo) throws WriterException, IOException {
        BitMatrix bitMatrix;
        int width = 300;
        int height = 150;

        switch (tipo) {
            case EAN13:
                if (codigo.length() != 13) {
                    throw new BusinessException("El código EAN-13 debe tener 13 dígitos");
                }
                EAN13Writer ean13Writer = new EAN13Writer();
                bitMatrix = ean13Writer.encode(codigo, BarcodeFormat.EAN_13, width, height);
                break;

            case EAN8:
                if (codigo.length() != 8) {
                    throw new BusinessException("El código EAN-8 debe tener 8 dígitos");
                }
                EAN8Writer ean8Writer = new EAN8Writer();
                bitMatrix = ean8Writer.encode(codigo, BarcodeFormat.EAN_8, width, height);
                break;

            case CODE128:
                Code128Writer code128Writer = new Code128Writer();
                bitMatrix = code128Writer.encode(codigo, BarcodeFormat.CODE_128, width, height);
                break;

            case QR:
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                bitMatrix = qrCodeWriter.encode(codigo, BarcodeFormat.QR_CODE, 250, 250);
                break;

            default:
                throw new BusinessException("Tipo de código no soportado: " + tipo);
        }

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Genera una etiqueta con código de barras para un item
     */
    public EtiquetaCodigoBarrasDTO generarEtiqueta(String itemId, String tipoItem,
            GenerarCodigoBarrasRequest.TipoCodigoBarras tipoCodigo)
            throws WriterException, IOException {
        EtiquetaCodigoBarrasDTO etiqueta = new EtiquetaCodigoBarrasDTO();
        etiqueta.setItemId(itemId);

        if ("producto".equalsIgnoreCase(tipoItem)) {
            Producto producto = productoRepository.findById(itemId).orElseThrow(
                    () -> new ResourceNotFoundException("Producto no encontrado: " + itemId));

            String codigo =
                    producto.getCodigoBarras() != null ? producto.getCodigoBarras() : itemId;
            etiqueta.setCodigo(codigo);
            etiqueta.setNombre(producto.getNombre());
            etiqueta.setPrecio(producto.getPrecio());
            etiqueta.setImagenCodigoBarras(generarImagenCodigoBarras(codigo, tipoCodigo));
            etiqueta.setFormatoImagen("PNG");

        } else if ("ingrediente".equalsIgnoreCase(tipoItem)) {
            Ingrediente ingrediente = ingredienteRepository.findById(itemId).orElseThrow(
                    () -> new ResourceNotFoundException("Ingrediente no encontrado: " + itemId));

            String codigo =
                    ingrediente.getCodigoBarras() != null ? ingrediente.getCodigoBarras() : itemId;
            etiqueta.setCodigo(codigo);
            etiqueta.setNombre(ingrediente.getNombre());
            etiqueta.setPrecio(ingrediente.getCosto());
            etiqueta.setImagenCodigoBarras(generarImagenCodigoBarras(codigo, tipoCodigo));
            etiqueta.setFormatoImagen("PNG");

        } else {
            throw new BusinessException("Tipo de item no válido: " + tipoItem);
        }

        return etiqueta;
    }

    /**
     * Genera múltiples etiquetas para impresión
     */
    public List<EtiquetaCodigoBarrasDTO> generarEtiquetasParaImprimir(
            ImprimirEtiquetasRequest request) throws WriterException, IOException {
        List<EtiquetaCodigoBarrasDTO> etiquetas = new ArrayList<>();

        for (ImprimirEtiquetasRequest.ItemEtiqueta item : request.getItems()) {
            for (int i = 0; i < item.getCantidad(); i++) {
                EtiquetaCodigoBarrasDTO etiqueta = generarEtiqueta(item.getItemId(),
                        item.getTipoItem(), GenerarCodigoBarrasRequest.TipoCodigoBarras.CODE128);
                etiquetas.add(etiqueta);
            }
        }

        return etiquetas;
    }

    /**
     * Busca un producto o ingrediente por código de barras
     */
    public Object buscarPorCodigoBarras(String codigo) {
        // Buscar en productos
        Optional<Producto> producto = productoRepository.findByCodigoBarras(codigo);
        if (producto.isPresent()) {
            return producto.get();
        }

        // Buscar en ingredientes
        Optional<Ingrediente> ingrediente = ingredienteRepository.findByCodigoBarras(codigo);
        if (ingrediente.isPresent()) {
            return ingrediente.get();
        }

        // Buscar por código interno
        producto = productoRepository.findByCodigoInterno(codigo);
        if (producto.isPresent()) {
            return producto.get();
        }

        ingrediente = ingredienteRepository.findByCodigoInterno(codigo);
        if (ingrediente.isPresent()) {
            return ingrediente.get();
        }

        throw new ResourceNotFoundException(
                "No se encontró ningún producto o ingrediente con el código: " + codigo);
    }

    /**
     * Ajusta el código según el tipo requerido
     */
    private String ajustarCodigoSegunTipo(String codigo,
            GenerarCodigoBarrasRequest.TipoCodigoBarras tipo) {
        // Extraer solo números del código
        String numeros = codigo.replaceAll("[^0-9]", "");

        switch (tipo) {
            case EAN13:
                // Ajustar a 12 dígitos y calcular dígito de control
                if (numeros.length() > 12) {
                    numeros = numeros.substring(0, 12);
                } else {
                    numeros = String.format("%012d",
                            Long.parseLong(numeros.isEmpty() ? "0" : numeros));
                }
                return numeros + calcularDigitoControlEAN13(numeros);

            case EAN8:
                // Ajustar a 7 dígitos y calcular dígito de control
                if (numeros.length() > 7) {
                    numeros = numeros.substring(0, 7);
                } else {
                    numeros = String.format("%07d",
                            Long.parseLong(numeros.isEmpty() ? "0" : numeros));
                }
                return numeros + calcularDigitoControlEAN8(numeros);

            case CODE128:
            case QR:
                // Estos tipos aceptan cualquier formato
                return codigo;

            default:
                return codigo;
        }
    }

    /**
     * Calcula el dígito de control para EAN-13
     */
    private int calcularDigitoControlEAN13(String codigo) {
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        int modulo = suma % 10;
        return (modulo == 0) ? 0 : 10 - modulo;
    }

    /**
     * Calcula el dígito de control para EAN-8
     */
    private int calcularDigitoControlEAN8(String codigo) {
        int suma = 0;
        for (int i = 0; i < 7; i++) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            suma += (i % 2 == 0) ? digito * 3 : digito;
        }
        int modulo = suma % 10;
        return (modulo == 0) ? 0 : 10 - modulo;
    }

    /**
     * Guarda el código de barras en el item correspondiente
     */
    private void guardarCodigoEnItem(String itemId, String tipoItem, String codigo) {
        if ("producto".equalsIgnoreCase(tipoItem)) {
            Producto producto = productoRepository.findById(itemId).orElseThrow(
                    () -> new ResourceNotFoundException("Producto no encontrado: " + itemId));
            producto.setCodigoBarras(codigo);
            productoRepository.save(producto);

        } else if ("ingrediente".equalsIgnoreCase(tipoItem)) {
            Ingrediente ingrediente = ingredienteRepository.findById(itemId).orElseThrow(
                    () -> new ResourceNotFoundException("Ingrediente no encontrado: " + itemId));
            ingrediente.setCodigoBarras(codigo);
            ingredienteRepository.save(ingrediente);

        } else {
            throw new BusinessException("Tipo de item no válido: " + tipoItem);
        }
    }
}
