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

import com.prog3.security.Models.Pedido;
import com.prog3.security.Repositories.PedidoRepository;

@CrossOrigin
@RestController
@RequestMapping("api/pedidos")
public class PedidosController {

    @Autowired
    PedidoRepository thePedidoRepository;

    @GetMapping("")
    public List<Pedido> find() {
        return this.thePedidoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Pedido findById(@PathVariable String id) {
        return this.thePedidoRepository.findById(id).orElse(null);
    }

    @GetMapping("/tipo/{tipo}")
    public List<Pedido> findByTipo(@PathVariable String tipo) {
        return this.thePedidoRepository.findByTipo(tipo);
    }

    @GetMapping("/mesa/{mesa}")
    public List<Pedido> findByMesa(@PathVariable String mesa) {
        return this.thePedidoRepository.findByMesa(mesa);
    }

    @GetMapping("/cliente/{cliente}")
    public List<Pedido> findByCliente(@PathVariable String cliente) {
        return this.thePedidoRepository.findByCliente(cliente);
    }

    @GetMapping("/mesero/{mesero}")
    public List<Pedido> findByMesero(@PathVariable String mesero) {
        return this.thePedidoRepository.findByMesero(mesero);
    }

    @GetMapping("/estado/{estado}")
    public List<Pedido> findByEstado(@PathVariable String estado) {
        return this.thePedidoRepository.findByEstado(estado);
    }

    @GetMapping("/fechas")
    public List<Pedido> findByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return this.thePedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @GetMapping("/hoy")
    public List<Pedido> findHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return this.thePedidoRepository.findByFechaGreaterThanEqual(inicioHoy);
    }

    @GetMapping("/plataforma/{plataforma}")
    public List<Pedido> findByPlataforma(@PathVariable String plataforma) {
        return this.thePedidoRepository.findByPlataforma(plataforma);
    }

    @PostMapping
    public Pedido create(@RequestBody Pedido newPedido) {
        // Asegurar que la fecha esté establecida
        if (newPedido.getFecha() == null) {
            newPedido.setFecha(LocalDateTime.now());
        }

        // Asegurar que el estado esté establecido
        if (newPedido.getEstado() == null || newPedido.getEstado().isEmpty()) {
            newPedido.setEstado("pendiente");
        }

        return this.thePedidoRepository.save(newPedido);
    }

    @PutMapping("/{id}")
    public Pedido update(@PathVariable String id, @RequestBody Pedido newPedido) {
        Pedido actualPedido = this.thePedidoRepository.findById(id).orElse(null);
        if (actualPedido != null) {
            actualPedido.setTipo(newPedido.getTipo());
            actualPedido.setMesa(newPedido.getMesa());
            actualPedido.setCliente(newPedido.getCliente());
            actualPedido.setMesero(newPedido.getMesero());
            actualPedido.setItems(newPedido.getItems());
            actualPedido.setNotas(newPedido.getNotas());
            actualPedido.setPlataforma(newPedido.getPlataforma());
            actualPedido.setPedidoPor(newPedido.getPedidoPor());
            actualPedido.setGuardadoPor(newPedido.getGuardadoPor());
            actualPedido.setFechaCortesia(newPedido.getFechaCortesia());
            actualPedido.setEstado(newPedido.getEstado());

            this.thePedidoRepository.save(actualPedido);
            return actualPedido;
        } else {
            return null;
        }
    }

    @PutMapping("/{id}/estado/{estado}")
    public Pedido cambiarEstado(@PathVariable String id, @PathVariable String estado) {
        Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
        if (pedido != null) {
            pedido.setEstado(estado);
            this.thePedidoRepository.save(pedido);
            return pedido;
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        this.thePedidoRepository.findById(id).ifPresent(thePedido
                -> this.thePedidoRepository.delete(thePedido));
    }
}
