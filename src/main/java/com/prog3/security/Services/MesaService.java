package com.prog3.security.Services;

import com.prog3.security.Models.Mesa;
import com.prog3.security.Repositories.MesaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MesaService {
    @Autowired
    private MesaRepository mesaRepository;
    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Limpia el estado de la mesa si no tiene pedidos activos
     */
    public void limpiarMesaSiNoTienePedidos(String nombreMesa) {
        Mesa mesa = mesaRepository.findByNombre(nombreMesa);
        if (mesa != null) {
            // Buscar pedidos activos (no cancelados, no pagados)
            List<com.prog3.security.Models.Pedido> pedidos = pedidoRepository.findByMesa(nombreMesa);
            boolean tienePedidosActivos = pedidos.stream().anyMatch(p -> !"pagado".equals(p.getEstado()) && !"cancelado".equals(p.getEstado()));
            if (!tienePedidosActivos) {
                mesa.setOcupada(false);
                mesa.setTotal(0.0);
                mesaRepository.save(mesa);
            }
        }
    }
}
