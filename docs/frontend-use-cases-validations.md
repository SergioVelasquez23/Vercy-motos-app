# 🔧 Casos de Uso y Validaciones - Sistema de Caja

Esta guía complementa la integración frontend con ejemplos específicos de casos de uso y validaciones críticas.

## 🎯 **CASOS DE USO ESPECÍFICOS**

### **1. 💳 PAGO MIXTO - Ejemplo Completo**

**Escenario**: Pedido de $100.000 pagado con $60.000 efectivo + $40.000 transferencia

```typescript
// Estado inicial del pedido
const pedido = {
  _id: "pedido123",
  total: 100000,
  totalPagado: 0,
  estado: "activo",
  pagosParciales: [],
};

// Paso 1: Agregar primer pago (efectivo)
async function pagarParte1() {
  const response = await fetch("/api/pedidos/pedido123/pagos-parciales", {
    method: "POST",
    body: JSON.stringify({
      monto: 60000,
      formaPago: "efectivo",
      procesadoPor: "Cajero 1",
    }),
  });

  // Estado después del primer pago
  // totalPagado: 60000
  // saldoPendiente: 40000
  // pagosParciales: [{ monto: 60000, formaPago: "efectivo" }]
}

// Paso 2: Agregar segundo pago (transferencia)
async function pagarParte2() {
  const response = await fetch("/api/pedidos/pedido123/pagos-parciales", {
    method: "POST",
    body: JSON.stringify({
      monto: 40000,
      formaPago: "transferencia",
      procesadoPor: "Cajero 1",
    }),
  });

  // Estado final
  // totalPagado: 100000
  // saldoPendiente: 0
  // estado: "pagado" (automáticamente)
  // pagosParciales: [
  //   { monto: 60000, formaPago: "efectivo" },
  //   { monto: 40000, formaPago: "transferencia" }
  // ]
}

// Resultado en caja:
// ventasEfectivo += 60000
// ventasTransferencias += 40000
// efectivoEsperado += 60000
```

### **2. 💸 GASTO DESDE CAJA vs NO DESDE CAJA**

#### **Caso A: Gasto que sale de caja (pagadoDesdeCaja = true)**

```typescript
// Ejemplo: Pago de servicios públicos con efectivo de caja
const gastoDesdeCAJA = {
  concepto: "Pago de luz",
  monto: 50000,
  cuadreCajaId: "cuadre123",
  pagadoDesdeCaja: true, // 🔥 CRÍTICO
  formaPago: "efectivo",
};

// Efecto en caja:
// efectivoEsperado -= 50000 ✅
// totalGastos += 50000 ✅
// fondoInicial NO cambia ✅
```

#### **Caso B: Gasto que NO sale de caja (pagadoDesdeCaja = false)**

```typescript
// Ejemplo: Factura de servicio que se pagará después
const gastoNODesdeCAJA = {
  concepto: "Factura de internet (por pagar)",
  monto: 80000,
  cuadreCajaId: "cuadre123",
  pagadoDesdeCaja: false, // 🔥 NO afecta efectivo
  formaPago: null, // No aplica
};

// Efecto en caja:
// efectivoEsperado NO cambia ✅
// totalGastos += 80000 ✅ (para reportes)
// fondoInicial NO cambia ✅
```

### **3. 🧾 FACTURA COMPRA DESDE CAJA**

```typescript
// Ejemplo: Compra de ingredientes pagada con efectivo de caja
const facturaCompra = {
  numero: "FC-001",
  proveedor: "Distribuidora ABC",
  total: 200000,
  tipoFactura: "compra",
  pagadoDesdeCaja: true, // 🔥 CRÍTICO
  medioPago: "Efectivo",
  itemsIngredientes: [
    {
      ingredienteId: "pollo123",
      nombreIngrediente: "Pollo",
      cantidad: 25,
      unidadMedida: "kg",
      precioUnitario: 8000,
      subtotal: 200000,
    },
  ],
};

// Efectos automáticos:
// 1. Stock: pollo.cantidad += 25 ✅
// 2. Caja: efectivoEsperado -= 200000 ✅
// 3. Inventario: crear movimiento "ENTRADA" ✅
// 4. Cierre: totalFacturasCompras += 200000 ✅
```

### **4. 🗑️ ELIMINACIÓN CON REVERSIÓN**

#### **Eliminar Pedido Pagado con Pagos Mixtos**

```typescript
// Pedido existente con pagos mixtos
const pedidoExistente = {
  _id: "pedido456",
  total: 150000,
  totalPagado: 150000,
  estado: "pagado",
  pagosParciales: [
    { monto: 100000, formaPago: "efectivo" },
    { monto: 50000, formaPago: "transferencia" },
  ],
};

// Al eliminar:
DELETE / api / pedidos / pedido456;

// Efectos automáticos:
// ventasEfectivo -= 100000 ✅
// ventasTransferencias -= 50000 ✅
// efectivoEsperado -= 100000 ✅
// totalVentas -= 150000 ✅
```

#### **Eliminar Gasto que Salió de Caja**

```typescript
// Gasto existente que salió de caja
const gastoExistente = {
  _id: "gasto789",
  monto: 75000,
  pagadoDesdeCaja: true,
  formaPago: "efectivo",
  cuadreCajaId: "cuadre123",
};

// Al eliminar:
DELETE / api / gastos / gasto789;

// Efectos automáticos:
// fondoInicial += 75000 ✅ (devuelve dinero)
// totalGastos -= 75000 ✅
// efectivoEsperado += 75000 ✅ (por el fondo aumentado)
```

---

## ⚠️ **VALIDACIONES CRÍTICAS FRONTEND**

### **1. Validación de Efectivo Disponible**

```typescript
interface ValidacionEfectivo {
  efectivoDisponible: number;
  montoRequerido: number;
  tipo: "gasto" | "factura";
}

async function validarEfectivoAntes(
  validacion: ValidacionEfectivo
): Promise<boolean> {
  // Obtener estado actual de caja
  const detalles = await fetch("/api/cuadre-caja/detalles-ventas").then((r) =>
    r.json()
  );

  const efectivoReal = detalles.totalEfectivoEnCaja;

  if (efectivoReal < validacion.montoRequerido) {
    // Mostrar error detallado
    const mensaje = `
      ❌ EFECTIVO INSUFICIENTE
      
      Disponible en caja: $${efectivoReal.toLocaleString()}
      Requerido para ${
        validacion.tipo
      }: $${validacion.montoRequerido.toLocaleString()}
      Faltante: $${(validacion.montoRequerido - efectivoReal).toLocaleString()}
      
      Opciones:
      • Cambiar forma de pago a Transferencia
      • Reducir el monto
      • Hacer más ventas en efectivo primero
    `;

    alert(mensaje);
    return false;
  }

  // Advertencia si queda poco efectivo
  const efectivoRestante = efectivoReal - validacion.montoRequerido;
  if (efectivoRestante < 100000) {
    // Menos de $100k restante
    const confirmar = confirm(`
      ⚠️ ADVERTENCIA: Efectivo bajo después de esta operación
      
      Quedará en caja: $${efectivoRestante.toLocaleString()}
      
      ¿Continuar?
    `);

    return confirmar;
  }

  return true;
}

// Uso en formularios
async function crearGastoValidado(gasto: FormularioGasto) {
  if (gasto.pagadoDesdeCaja && gasto.formaPago === "efectivo") {
    const valido = await validarEfectivoAntes({
      efectivoDisponible: estadoCaja.efectivoDisponible,
      montoRequerido: gasto.monto,
      tipo: "gasto",
    });

    if (!valido) return; // Cancelar operación
  }

  // Proceder con creación
  await crearGasto(gasto);
}
```

### **2. Validación de Pagos Mixtos**

```typescript
interface ValidadorPagoMixto {
  pedidoId: string;
  totalPedido: number;
  pagosParciales: PagoParcial[];
}

class ValidadorPagos {
  static validarNuevoPago(
    monto: number,
    formaPago: string,
    saldoPendiente: number
  ): string[] {
    const errores: string[] = [];

    // Validar monto
    if (monto <= 0) {
      errores.push("El monto debe ser mayor que cero");
    }

    if (monto > saldoPendiente) {
      errores.push(
        `El monto ($${monto.toLocaleString()}) no puede ser mayor que el saldo pendiente ($${saldoPendiente.toLocaleString()})`
      );
    }

    // Validar forma de pago
    const formasValidas = ["efectivo", "transferencia", "tarjeta"];
    if (!formasValidas.includes(formaPago)) {
      errores.push("Forma de pago inválida");
    }

    return errores;
  }

  static validarPagoCompleto(validador: ValidadorPagoMixto): boolean {
    const totalPagado = validador.pagosParciales.reduce(
      (sum, pago) => sum + pago.monto,
      0
    );
    const diferencia = Math.abs(totalPagado - validador.totalPedido);

    // Tolerancia de $1 para problemas de redondeo
    if (diferencia > 1) {
      alert(`
        ❌ PAGO INCOMPLETO
        
        Total pedido: $${validador.totalPedido.toLocaleString()}
        Total pagado: $${totalPagado.toLocaleString()}
        ${
          totalPagado > validador.totalPedido ? "Exceso" : "Faltante"
        }: $${diferencia.toLocaleString()}
      `);
      return false;
    }

    return true;
  }

  static validarEliminacionPago(
    pago: PagoParcial,
    pagosParciales: PagoParcial[]
  ): boolean {
    // No permitir eliminar si es el único pago y el pedido está marcado como pagado
    if (pagosParciales.length === 1) {
      const confirmar = confirm(`
        ⚠️ Al eliminar este pago, el pedido volvería a estado ACTIVO
        
        ¿Está seguro de que desea continuar?
      `);

      return confirmar;
    }

    return true;
  }
}
```

### **3. Validación de Estados de Cuadre**

```typescript
interface EstadoCuadre {
  cuadreActivo: CuadreCaja | null;
  operacion: "crear_pedido" | "crear_gasto" | "crear_factura";
}

function validarCuadreActivo(estado: EstadoCuadre): boolean {
  if (!estado.cuadreActivo) {
    alert(`
      ❌ NO HAY CAJA ACTIVA
      
      Para ${estado.operacion.replace("_", " ")} debe haber una caja abierta.
      
      Por favor:
      1. Abra una nueva caja
      2. O contacte al supervisor
    `);
    return false;
  }

  if (estado.cuadreActivo.cerrada) {
    alert(`
      ❌ CAJA CERRADA
      
      La caja "${estado.cuadreActivo.nombre}" está cerrada.
      
      No se pueden realizar operaciones en cajas cerradas.
    `);
    return false;
  }

  return true;
}
```

---

## 🎨 **COMPONENTES UI RECOMENDADOS**

### **1. Componente Selector de Pago**

```html
<!-- PagoSelector.vue -->
<template>
  <div class="pago-selector">
    <div class="toggle-pago">
      <button
        :class="['btn-toggle', { active: !esPagoMixto }]"
        @click="esPagoMixto = false"
      >
        Pago Simple
      </button>
      <button
        :class="['btn-toggle', { active: esPagoMixto }]"
        @click="esPagoMixto = true"
      >
        Pago Mixto
      </button>
    </div>

    <!-- Pago Simple -->
    <div v-if="!esPagoMixto" class="pago-simple">
      <select v-model="pagoSimple.formaPago">
        <option value="efectivo">Efectivo</option>
        <option value="transferencia">Transferencia</option>
        <option value="tarjeta">Tarjeta</option>
      </select>
      <button @click="pagarCompleto()">
        Pagar ${{ totalPedido.toLocaleString() }}
      </button>
    </div>

    <!-- Pago Mixto -->
    <PagoMixto
      v-else
      :pedido-id="pedidoId"
      :total="totalPedido"
      @completado="$emit('pagado')"
    />
  </div>
</template>
```

### **2. Componente Indicador de Efectivo**

```html
<!-- IndicadorEfectivo.vue -->
<template>
  <div class="indicador-efectivo" :class="clasesEfectivo">
    <div class="icono">💰</div>
    <div class="info">
      <div class="monto">${{ efectivoDisponible.toLocaleString() }}</div>
      <div class="etiqueta">Efectivo Disponible</div>
    </div>
    <div class="estado" :class="estadoColor">{{ estadoTexto }}</div>
  </div>
</template>

<script>
  computed: {
    estadoColor() {
      if (this.efectivoDisponible > 500000) return 'abundante';
      if (this.efectivoDisponible > 200000) return 'normal';
      if (this.efectivoDisponible > 50000) return 'bajo';
      return 'critico';
    },

    estadoTexto() {
      switch(this.estadoColor) {
        case 'abundante': return 'Excelente';
        case 'normal': return 'Normal';
        case 'bajo': return 'Bajo';
        case 'critico': return 'Crítico';
      }
    }
  }
</script>
```

### **3. Componente Confirmación de Eliminación**

```html
<!-- ConfirmacionEliminacion.vue -->
<template>
  <div class="modal-confirmacion" v-if="mostrar">
    <div class="modal-content">
      <h3>⚠️ Confirmar Eliminación</h3>

      <div class="info-item">
        <strong>{{ tipoItem }}:</strong> {{ item.concepto || item.numero ||
        item.mesa }}
        <br />
        <strong>Monto:</strong> ${{ item.total || item.monto || item.totalPagado
        }}
      </div>

      <div class="impacto-caja" v-if="impactoCaja">
        <h4>📊 Impacto en Caja:</h4>
        <ul>
          <li v-for="efecto in efectosEliminacion" :key="efecto.tipo">
            {{ efecto.descripcion }}
          </li>
        </ul>
      </div>

      <div class="acciones">
        <button class="btn-cancelar" @click="cancelar()">❌ Cancelar</button>
        <button class="btn-confirmar" @click="confirmar()">✅ Eliminar</button>
      </div>
    </div>
  </div>
</template>

<script>
  computed: {
    efectosEliminacion() {
      const efectos = [];

      if (this.tipoItem === 'pedido' && this.item.pagosParciales) {
        this.item.pagosParciales.forEach(pago => {
          efectos.push({
            tipo: 'reversion',
            descripcion: `Se devolverán $${pago.monto.toLocaleString()} de ${pago.formaPago} a la caja`
          });
        });
      }

      if (this.tipoItem === 'gasto' && this.item.pagadoDesdeCaja) {
        efectos.push({
          tipo: 'devolucion',
          descripcion: `Se devolverán $${this.item.monto.toLocaleString()} al fondo inicial de la caja`
        });
      }

      return efectos;
    }
  }
</script>
```

---

## 📱 **RESPONSIVE DESIGN CONSIDERACIONES**

### **Mobile First - Pagos Mixtos**

```css
/* Diseño para móviles */
.pago-mixto-mobile {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pago-parcial-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.nuevo-pago-mobile {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 15px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

@media (min-width: 768px) {
  /* Diseño para tablets/desktop */
  .pago-mixto-desktop {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;
  }
}
```

---

## 🚀 **OPTIMIZACIONES DE RENDIMIENTO**

### **1. Debounce para Validaciones**

```typescript
// Evitar validaciones excesivas mientras el usuario tipea
const validarMontoDebounced = debounce(async (monto: number) => {
  if (formulario.pagadoDesdeCaja && formulario.formaPago === "efectivo") {
    await validarEfectivoDisponible(monto);
  }
}, 500);
```

### **2. Cache de Datos de Caja**

```typescript
// Cache con TTL de 30 segundos
class CacheDetallesCaja {
  private cache: { data: any; timestamp: number } | null = null;
  private TTL = 30000; // 30 segundos

  async obtenerDetalles(): Promise<DetallesVentas> {
    const ahora = Date.now();

    if (this.cache && ahora - this.cache.timestamp < this.TTL) {
      return this.cache.data;
    }

    const datos = await fetch("/api/cuadre-caja/detalles-ventas").then((r) =>
      r.json()
    );
    this.cache = { data: datos, timestamp: ahora };

    return datos;
  }
}
```

### **3. WebSocket para Actualizaciones en Tiempo Real**

```typescript
// Opcional: para múltiples usuarios simultáneos
class WebSocketCaja {
  private ws: WebSocket;

  constructor() {
    this.ws = new WebSocket("ws://localhost:8080/caja-updates");

    this.ws.onmessage = (event) => {
      const update = JSON.parse(event.data);

      switch (update.tipo) {
        case "pago_realizado":
          this.actualizarDetallesCaja();
          break;
        case "gasto_creado":
          this.actualizarGastos();
          break;
        case "factura_creada":
          this.actualizarFacturas();
          break;
      }
    };
  }
}
```

---

## ✅ **TESTING RECOMENDADO**

### **1. Tests de Validación**

```typescript
describe("Validaciones de Pago", () => {
  test("debe rechazar pago mayor al saldo pendiente", () => {
    const resultado = ValidadorPagos.validarNuevoPago(60000, "efectivo", 50000);
    expect(resultado).toContain("no puede ser mayor que el saldo pendiente");
  });

  test("debe validar pago completo correctamente", () => {
    const validador = {
      pedidoId: "123",
      totalPedido: 100000,
      pagosParciales: [
        { monto: 60000, formaPago: "efectivo" },
        { monto: 40000, formaPago: "transferencia" },
      ],
    };

    expect(ValidadorPagos.validarPagoCompleto(validador)).toBe(true);
  });
});
```

### **2. Tests de Integración**

```typescript
describe("Flujo Completo de Pago Mixto", () => {
  test("debe procesar pago mixto correctamente", async () => {
    // Crear pedido
    const pedido = await crearPedido({ total: 100000 });

    // Pago parcial 1
    await agregarPagoParcial(pedido._id, 60000, "efectivo");

    // Pago parcial 2
    await agregarPagoParcial(pedido._id, 40000, "transferencia");

    // Verificar estado final
    const pedidoFinal = await obtenerPedido(pedido._id);
    expect(pedidoFinal.estado).toBe("pagado");
    expect(pedidoFinal.totalPagado).toBe(100000);
  });
});
```

Esta guía complementa perfectamente la documentación principal y proporciona todos los detalles específicos que el frontend necesita para implementar correctamente el sistema de caja con pagos mixtos, control de efectivo y eliminaciones inteligentes. 🎉
