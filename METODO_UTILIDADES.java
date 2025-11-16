// Método utilitario para recalcular totales de forma consistente
// Se puede agregar a PedidosController para evitar duplicación de código

/**
 * 💰 Recalcula el total de un pedido basándose en sus items actuales
 * 
 * @param pedido El pedido a recalcular
 */
private void recalcularTotalPedido(Pedido pedido) {
    double nuevoTotal = 0;
    if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
        nuevoTotal = pedido.getItems().stream().mapToDouble(ItemPedido::getSubtotal).sum();
    }
    pedido.setTotal(nuevoTotal);

    System.out.println("💰 Total recalculado para pedido " + pedido.get_id() + ": $" + nuevoTotal);
}

// Uso en métodos existentes:
// recalcularTotalPedido(actualPedido);
// thePedidoRepository.save(actualPedido);
