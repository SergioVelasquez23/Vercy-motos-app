/**
 * Cliente WebSocket para recibir notificaciones en tiempo real
 *
 * Este script permite conectarse al servidor WebSocket y recibir notificaciones
 * sobre actualizaciones en mesas, pedidos e inventario.
 */

// Conexión al servidor WebSocket
let stompClient = null;
let connected = false;

// Función para conectar al servidor WebSocket
function connectWebSocket() {
  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);

  // Conectar al servidor
  stompClient.connect(
    {},
    function (frame) {
      connected = true;
      console.log("Conectado a WebSocket: " + frame);

      // Suscribirse a los diferentes canales
      subscribeToTables();
      subscribeToOrders();
      subscribeToInventory();
    },
    function (error) {
      connected = false;
      console.log("Error de conexión WebSocket: " + error);

      // Intentar reconectar después de 5 segundos
      setTimeout(connectWebSocket, 5000);
    }
  );
}

// Suscribirse a notificaciones de mesas
function subscribeToTables() {
  if (!connected || !stompClient) return;

  stompClient.subscribe("/topic/mesas", function (response) {
    const data = JSON.parse(response.body);
    console.log("Notificación de mesa recibida:", data);

    // Ejemplo: Actualizar la interfaz de usuario
    if (data.tipo === "MESA_ACTUALIZADA") {
      notifyTableUpdate(data);
    }
  });
}

// Suscribirse a notificaciones de pedidos
function subscribeToOrders() {
  if (!connected || !stompClient) return;

  stompClient.subscribe("/topic/pedidos", function (response) {
    const data = JSON.parse(response.body);
    console.log("Notificación de pedido recibida:", data);

    // Ejemplo: Actualizar la interfaz de usuario
    if (data.tipo === "PEDIDO_ACTUALIZADO") {
      notifyOrderUpdate(data);
    }
  });
}

// Suscribirse a notificaciones de inventario
function subscribeToInventory() {
  if (!connected || !stompClient) return;

  stompClient.subscribe("/topic/inventario", function (response) {
    const data = JSON.parse(response.body);
    console.log("Notificación de inventario recibida:", data);

    // Ejemplo: Actualizar la interfaz de usuario
    if (data.tipo === "INVENTARIO_ACTUALIZADO") {
      notifyInventoryUpdate(data);
    }
  });
}

// Funciones para actualizar la interfaz de usuario

function notifyTableUpdate(data) {
  // Actualizar la tabla de mesas en la interfaz
  const tableElement = document.querySelector(`#mesa-${data.mesaId}`);

  if (tableElement) {
    // Actualizar el estado de la mesa en la interfaz
    if (data.ocupada) {
      tableElement.classList.add("ocupada");
      tableElement.classList.remove("libre");
    } else {
      tableElement.classList.add("libre");
      tableElement.classList.remove("ocupada");
    }

    // Mostrar una notificación en la interfaz
    showNotification(`Mesa ${data.nombreMesa} actualizada`);
  } else {
    // Si la mesa no existe en el DOM, recargar la lista completa
    reloadTableList();
  }
}

function notifyOrderUpdate(data) {
  // Actualizar la lista de pedidos en la interfaz
  const orderElement = document.querySelector(`#pedido-${data.pedidoId}`);

  if (orderElement) {
    // Actualizar el estado del pedido
    const statusElement = orderElement.querySelector(".estado-pedido");
    if (statusElement) {
      statusElement.textContent = data.estado;
      statusElement.className = `estado-pedido estado-${data.estado.toLowerCase()}`;
    }

    // Mostrar una notificación en la interfaz
    showNotification(`Pedido actualizado: ${data.estado}`);
  } else {
    // Si el pedido no existe en el DOM, recargar la lista completa
    reloadOrderList();
  }
}

function notifyInventoryUpdate(data) {
  // Actualizar los elementos de inventario en la interfaz
  const inventoryElement = document.querySelector(
    `#inventario-${data.productoId}`
  );

  if (inventoryElement) {
    // Actualizar el stock del producto
    const stockElement = inventoryElement.querySelector(".stock");
    if (stockElement && data.stockNuevo !== undefined) {
      stockElement.textContent = data.stockNuevo;

      // Destacar el cambio con una animación
      stockElement.classList.add("stock-updated");
      setTimeout(() => stockElement.classList.remove("stock-updated"), 2000);
    }

    // Mostrar una notificación en la interfaz
    showNotification(
      `Inventario actualizado: ${data.nombreProducto || "Producto"}`
    );
  } else {
    // Si el producto no existe en el DOM, recargar la lista completa
    reloadInventoryList();
  }
}

// Funciones auxiliares para la interfaz

function showNotification(message) {
  // Implementación de notificaciones en la interfaz
  const notificationContainer = document.getElementById("notifications");

  if (notificationContainer) {
    const notification = document.createElement("div");
    notification.className = "notification";
    notification.textContent = message;

    notificationContainer.appendChild(notification);

    // Eliminar la notificación después de 5 segundos
    setTimeout(() => {
      notification.classList.add("fadeout");
      setTimeout(() => notification.remove(), 500);
    }, 5000);
  }
}

function reloadTableList() {
  // Recargar la lista de mesas desde el servidor
  fetch("/api/mesas")
    .then((response) => response.json())
    .then((data) => {
      if (data.status === "success" && data.data) {
        updateTableUI(data.data);
      }
    })
    .catch((error) => console.error("Error al recargar mesas:", error));
}

function reloadOrderList() {
  // Recargar la lista de pedidos desde el servidor
  fetch("/api/pedidos")
    .then((response) => response.json())
    .then((data) => {
      if (data.status === "success" && data.data) {
        updateOrderUI(data.data);
      }
    })
    .catch((error) => console.error("Error al recargar pedidos:", error));
}

function reloadInventoryList() {
  // Recargar la lista de inventario desde el servidor
  fetch("/api/inventario")
    .then((response) => response.json())
    .then((data) => {
      if (data.status === "success" && data.data) {
        updateInventoryUI(data.data);
      }
    })
    .catch((error) => console.error("Error al recargar inventario:", error));
}

// Función para inicializar el cliente WebSocket cuando se carga la página
document.addEventListener("DOMContentLoaded", function () {
  // Iniciar la conexión WebSocket
  connectWebSocket();

  // Inicializar la interfaz
  console.log("Cliente WebSocket inicializado");
});
