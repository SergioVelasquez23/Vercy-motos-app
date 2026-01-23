package com.prog3.security.Services;

import com.prog3.security.Models.Categoria;
import com.prog3.security.Repositories.CategoriaRepository;
import com.prog3.security.Exception.ResourceNotFoundException;
import com.prog3.security.Exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Obtener todas las categorías activas con caché
     */
    @Cacheable(value = "categorias", key = "'activas'")
    public List<Categoria> obtenerCategoriasActivas() {
        System.out.println("🔄 Cargando categorías activas desde BD");
        return categoriaRepository.findByActivoTrueOrderByOrdenAsc();
    }

    /**
     * Obtener todas las categorías (incluyendo inactivas)
     */
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    /**
     * Obtener una categoría por ID
     */
    public Categoria obtenerPorId(String id) {
        return categoriaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    /**
     * Crear una nueva categoría
     */
    @CacheEvict(value = "categorias", allEntries = true)
    public Categoria crear(Categoria categoria) {
        // Validar que el nombre no exista
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new BusinessException(
                    "Ya existe una categoría con el nombre: " + categoria.getNombre());
        }

        // Validaciones básicas
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre de la categoría es obligatorio");
        }

        return categoriaRepository.save(categoria);
    }

    /**
     * Actualizar una categoría
     */
    @CacheEvict(value = "categorias", allEntries = true)
    public Categoria actualizar(String id, Categoria categoriaActualizada) {
        Categoria categoria = obtenerPorId(id);

        // Verificar que el nombre no esté en uso por otra categoría
        List<Categoria> categoriasConMismoNombre = categoriaRepository
                .findByNombreIgnoreCaseAndIdNot(categoriaActualizada.getNombre(), id);

        if (!categoriasConMismoNombre.isEmpty()) {
            throw new BusinessException(
                    "Ya existe otra categoría con el nombre: " + categoriaActualizada.getNombre());
        }

        // Actualizar campos
        categoria.setNombre(categoriaActualizada.getNombre());
        categoria.setDescripcion(categoriaActualizada.getDescripcion());
        categoria.setIcono(categoriaActualizada.getIcono());
        categoria.setColor(categoriaActualizada.getColor());
        categoria.setImagenUrl(categoriaActualizada.getImagenUrl());
        categoria.setOrden(categoriaActualizada.getOrden());
        categoria.setActivo(categoriaActualizada.isActivo());

        return categoriaRepository.save(categoria);
    }

    /**
     * Eliminar una categoría
     */
    @CacheEvict(value = "categorias", allEntries = true)
    public void eliminar(String id) {
        Categoria categoria = obtenerPorId(id);
        categoriaRepository.delete(categoria);
    }

    /**
     * Desactivar una categoría (soft delete)
     */
    @CacheEvict(value = "categorias", allEntries = true)
    public Categoria desactivar(String id) {
        Categoria categoria = obtenerPorId(id);
        categoria.setActivo(false);
        return categoriaRepository.save(categoria);
    }

    /**
     * Activar una categoría
     */
    @CacheEvict(value = "categorias", allEntries = true)
    public Categoria activar(String id) {
        Categoria categoria = obtenerPorId(id);
        categoria.setActivo(true);
        return categoriaRepository.save(categoria);
    }

    /**
     * Buscar categorías por nombre
     */
    public List<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombreContaining(nombre);
    }
}
