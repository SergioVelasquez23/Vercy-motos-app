package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.PedidoInterno;
import com.prog3.security.Repositories.PedidoInternoRepository;

@CrossOrigin
@RestController
@RequestMapping("api/pedidos-internos")
public class PedidosInternosController {

    @Autowired
    PedidoInternoRepository thePedidoInternoRepository;

    @GetMapping("")
    public List<PedidoInterno> find() {
        return this.thePedidoInternoRepository.findAll();
    }

    @GetMapping("/{id}")
    public PedidoInterno findById(@PathVariable String id) {
        return this.thePedidoInternoRepository.findById(id).orElse(null);
    }

    @GetMapping("/producto/{productoId}")
    public List<PedidoInterno> findByProducto(@PathVariable String productoId) {
        return this.thePedidoInternoRepository.findByProductoId(productoId);
    }

    @GetMapping("/guardado-por/{guardadoPor}")
    public List<PedidoInterno> findByGuardadoPor(@PathVariable String guardadoPor) {
        return this.thePedidoInternoRepository.findByGuardadoPor(guardadoPor);
    }

    @GetMapping("/pedido-por/{pedidoPor}")
    public List<PedidoInterno> findByPedidoPor(@PathVariable String pedidoPor) {
        return this.thePedidoInternoRepository.findByPedidoPor(pedidoPor);
    }

    @GetMapping("/estado/{estado}")
    public List<PedidoInterno> findByEstado(@PathVariable String estado) {
        return this.thePedidoInternoRepository.findByEstado(estado);
    }

    @GetMapping("/fechas")
    public List<PedidoInterno> findByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return this.thePedidoInternoRepository.findByFechaPedidoBetween(fechaInicio, fechaFin);
    }

    @GetMapping("/hoy")
    public List<PedidoInterno> findHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return this.thePedidoInternoRepository.findByFechaPedidoGreaterThanEqual(inicioHoy);
    }

    @GetMapping("/pendientes")
    public List<PedidoInterno> findPendientesHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return this.thePedidoInternoRepository.findByEstadoAndFechaPedidoGreaterThanEqual("pendiente", inicioHoy);
    }

    @PostMapping
    public PedidoInterno create(@RequestBody PedidoInterno newPedidoInterno) {
        // Asegurar que la fecha esté establecida
        if (newPedidoInterno.getFechaPedido() == null) {
            newPedidoInterno.setFechaPedido(LocalDateTime.now());
        }

        // Asegurar que el estado esté establecido
        if (newPedidoInterno.getEstado() == null || newPedidoInterno.getEstado().isEmpty()) {
            newPedidoInterno.setEstado("pendiente");
        }

        return this.thePedidoInternoRepository.save(newPedidoInterno);
    }

    @PutMapping("/{id}")
    public PedidoInterno update(@PathVariable String id, @RequestBody PedidoInterno newPedidoInterno) {
        PedidoInterno actualPedidoInterno = this.thePedidoInternoRepository.findById(id).orElse(null);
        if (actualPedidoInterno != null) {
            actualPedidoInterno.setProductoId(newPedidoInterno.getProductoId());
            actualPedidoInterno.setCantidad(newPedidoInterno.getCantidad());
            actualPedidoInterno.setNota(newPedidoInterno.getNota());
            actualPedidoInterno.setGuardadoPor(newPedidoInterno.getGuardadoPor());
            actualPedidoInterno.setPedidoPor(newPedidoInterno.getPedidoPor());
            actualPedidoInterno.setFechaCortesia(newPedidoInterno.getFechaCortesia());
            actualPedidoInterno.setEstado(newPedidoInterno.getEstado());

            this.thePedidoInternoRepository.save(actualPedidoInterno);
            return actualPedidoInterno;
        } else {
            return null;
        }
    }

    @PutMapping("/{id}/estado/{estado}")
    public PedidoInterno cambiarEstado(@PathVariable String id, @PathVariable String estado) {
        PedidoInterno pedidoInterno = this.thePedidoInternoRepository.findById(id).orElse(null);
        if (pedidoInterno != null) {
            pedidoInterno.setEstado(estado);
            this.thePedidoInternoRepository.save(pedidoInterno);
            return pedidoInterno;
        }
        return null;
    }

    @PutMapping("/{id}/completar")
    public PedidoInterno completar(@PathVariable String id) {
        PedidoInterno pedidoInterno = this.thePedidoInternoRepository.findById(id).orElse(null);
        if (pedidoInterno != null) {
            pedidoInterno.setEstado("completado");
            this.thePedidoInternoRepository.save(pedidoInterno);
            return pedidoInterno;
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        this.thePedidoInternoRepository.findById(id).ifPresent(thePedidoInterno
                -> this.thePedidoInternoRepository.delete(thePedidoInterno));
    }
}
