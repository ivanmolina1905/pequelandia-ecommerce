package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.dto.EstadisticasAdminDTO;
import com.pequelandia.pequelandia.dto.ProductoMasVendidoDTO;
import com.pequelandia.pequelandia.dto.PuntoSerieDTO;
import com.pequelandia.pequelandia.repository.*;
import com.pequelandia.pequelandia.service.IEstadisticasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/** [Doc]
 * Construye indicadores administrativos y series de ventas a partir de consultas de repositorio.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EstadisticasService implements IEstadisticasService {

    // [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
	@Autowired
    private  IPedidoRepository pedidoRepo;
    // [Doc] Atributo usuarioRepo (IUsuarioRepository): acceso de persistencia a usuario.
	@Autowired
    private  IUsuarioRepository usuarioRepo;
    // [Doc] Atributo productoRepo (IProductoRepository): acceso de persistencia a producto.
	@Autowired
    private  IProductoRepository productoRepo;

    /** [Doc]
     * Devuelve dashboard resumen, según el cuerpo o la implementación del contrato.
     * @return EstadisticasAdminDTO con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
    public EstadisticasAdminDTO getDashboardResumen() {

        // [Doc] Variable local dto (EstadisticasAdminDTO): objeto de transferencia con los datos de la operación. Se inicializa con new EstadisticasAdminDTO().
        EstadisticasAdminDTO dto = new EstadisticasAdminDTO();

        // --- Totales principales ---
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setTotalUsuarios(usuarioRepo.count());
        dto.setTotalUsuarios(usuarioRepo.count());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setTotalProductos(productoRepo.count());
        dto.setTotalProductos(productoRepo.count());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setTotalPedidos(pedidoRepo.contarPedidos());
        dto.setTotalPedidos(pedidoRepo.contarPedidos());

        // --- Ventas ---
        // [Doc] Variable local vHoy (Double): v hoy. Se inicializa con pedidoRepo.ventasDelDia(LocalDate.now()).
        Double vHoy = pedidoRepo.ventasDelDia(LocalDate.now());
        // [Doc] Variable local vMes (Double): v mes. Se inicializa con pedidoRepo.ventasDelMes(LocalDate.now().getYear(), LocalDate.now().getMonthValue()).
        Double vMes = pedidoRepo.ventasDelMes(LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        
        // Año completo
        // [Doc] Variable local vAnio (Double): v anio. Se inicializa con pedidoRepo.ventasPorAnio( LocalDate.now().getYear(), LocalDate.now().getYear() ).stream() .map(arr -> ((Num....
        Double vAnio = pedidoRepo.ventasPorAnio(
                LocalDate.now().getYear(),
                LocalDate.now().getYear()
        ).stream()
         .map(arr -> ((Number) arr[1]).doubleValue())
         .findFirst()
         .orElse(0.0);

        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setVentasHoy(vHoy != null ? vHoy : 0);
        dto.setVentasHoy(vHoy != null ? vHoy : 0);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setVentasMes(vMes != null ? vMes : 0);
        dto.setVentasMes(vMes != null ? vMes : 0);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setVentasAnio(vAnio != null ? vAnio : 0);
        dto.setVentasAnio(vAnio != null ? vAnio : 0);

        // --- Top productos ---
        // [Doc] Variable local top (List<Object[]>): top. Se inicializa con pedidoRepo.productosMasVendidos().
        List<Object[]> top = pedidoRepo.productosMasVendidos();
        // [Doc] Variable local topList (List<ProductoMasVendidoDTO>): top list. Se inicializa con top.stream() .map(arr -> new ProductoMasVendidoDTO( ((Number) arr[0]).longValue(), (String) arr[1], ((Numbe....
        List<ProductoMasVendidoDTO> topList = top.stream()
                .map(arr -> new ProductoMasVendidoDTO(
                        ((Number) arr[0]).longValue(),
                        (String) arr[1],
                        ((Number) arr[2]).longValue()
                ))
                .collect(Collectors.toList());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setTopProductos(topList);
        dto.setTopProductos(topList);

        // --- Stock bajo ---
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setProductosStockBajo(productoRepo.obtenerStockBajo(3));
        dto.setProductosStockBajo(productoRepo.obtenerStockBajo(3));

        // --- Usuarios nuevos del mes ---
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setUsuariosNuevosMes( usuarioRepo.usuariosRegistradosPorMes(LocalDate.now().getYear()).stream() .filter(arr -> ((Number) arr[0]).intValue() == LocalDate.now().getM...
        dto.setUsuariosNuevosMes(
            usuarioRepo.usuariosRegistradosPorMes(LocalDate.now().getYear()).stream()
                .filter(arr -> ((Number) arr[0]).intValue() == LocalDate.now().getMonthValue())
                .map(arr -> ((Number) arr[1]).longValue())
                .findFirst()
                .orElse(0L)
        );

        // --- Pedidos completados (entregados) ---
        // [Doc] Variable local completados (long): completados. Se inicializa con pedidoRepo.countByEstadoPedido("ENTREGADO").
        long completados = pedidoRepo.countByEstadoPedido("ENTREGADO");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setPedidosCompletados(completados);
        dto.setPedidosCompletados(completados);
        // [Doc] Termina el método o la función y devuelve dto.
        return dto;
    }

    // =======================================================
    // 2) SERIE - VENTAS POR DÍA
    // =======================================================
    /** [Doc]
     * Devuelve ventas por dia, según el cuerpo o la implementación del contrato.
     * @param desde inicio del intervalo consultado.
     * @param hasta fin del intervalo consultado.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<PuntoSerieDTO> getVentasPorDia(LocalDate desde, LocalDate hasta) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasPorDia(desde, hasta) .stream() .map(arr -> new PuntoSerieDTO( arr[0].toString(), ((Number) arr[1]).doubleValue() )) .collect(Collectors.toLi....
        if (desde == null || hasta == null || desde.isAfter(hasta))
            throw new IllegalArgumentException("Rango de fechas inválido");
        return pedidoRepo.ventasPorDia(desde, hasta)
                .stream()
                .map(arr -> new PuntoSerieDTO(
                        arr[0].toString(),
                        ((Number) arr[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    // =======================================================
    // 3) SERIE - VENTAS POR MES (DE UN AÑO)
    // =======================================================
    /** [Doc]
     * Devuelve ventas por mes, según el cuerpo o la implementación del contrato.
     * @param anio año de la consulta.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<PuntoSerieDTO> getVentasPorMes(int anio) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasPorMes(anio) .stream() .map(arr -> new PuntoSerieDTO( nombreMes(((Number) arr[0]).intValue()), ((Number) arr[1]).doubleValue() )) .collect(C....
        return pedidoRepo.ventasPorMes(anio)
                .stream()
                .map(arr -> new PuntoSerieDTO(
                        nombreMes(((Number) arr[0]).intValue()),
                        ((Number) arr[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    // =======================================================
    // 4) SERIE - VENTAS POR AÑO (RANGO)
    // =======================================================
    /** [Doc]
     * Devuelve ventas por anio, según el cuerpo o la implementación del contrato.
     * @param inicio límite inicial del intervalo.
     * @param fin límite final del intervalo.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<PuntoSerieDTO> getVentasPorAnio(int inicio, int fin) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasPorAnio(inicio, fin) .stream() .map(arr -> new PuntoSerieDTO( String.valueOf(arr[0]), ((Number) arr[1]).doubleValue() )) .collect(Collectors....
        if (inicio > fin) throw new IllegalArgumentException("Rango de años inválido");
        return pedidoRepo.ventasPorAnio(inicio, fin)
                .stream()
                .map(arr -> new PuntoSerieDTO(
                        String.valueOf(arr[0]),
                        ((Number) arr[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    // =======================================================
    // Helper para nombres de meses
    // =======================================================
    /** [Doc]
     * Convierte el número de mes en la etiqueta que utiliza la serie del gráfico.
     * @param mes mes de la consulta.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private String nombreMes(int mes) {
        // [Doc] Termina el método o la función y devuelve switch (mes) { case 1 -> "ENERO"; case 2 -> "FEBRERO"; case 3 -> "MARZO"; case 4 -> "ABRIL"; case 5 -> "MAYO"; case 6 -> "JUNIO"; case 7 -> "JULIO"; case 8 -....
        return switch (mes) {
            // [Doc] Ejecuta la instrucción Java case: case 1 -> "ENERO";
            case 1 -> "ENERO";
            // [Doc] Ejecuta la instrucción Java case: case 2 -> "FEBRERO";
            case 2 -> "FEBRERO";
            // [Doc] Ejecuta la instrucción Java case: case 3 -> "MARZO";
            case 3 -> "MARZO";
            // [Doc] Ejecuta la instrucción Java case: case 4 -> "ABRIL";
            case 4 -> "ABRIL";
            // [Doc] Ejecuta la instrucción Java case: case 5 -> "MAYO";
            case 5 -> "MAYO";
            // [Doc] Ejecuta la instrucción Java case: case 6 -> "JUNIO";
            case 6 -> "JUNIO";
            // [Doc] Ejecuta la instrucción Java case: case 7 -> "JULIO";
            case 7 -> "JULIO";
            // [Doc] Ejecuta la instrucción Java case: case 8 -> "AGOSTO";
            case 8 -> "AGOSTO";
            // [Doc] Ejecuta la instrucción Java case: case 9 -> "SEPTIEMBRE";
            case 9 -> "SEPTIEMBRE";
            // [Doc] Ejecuta la instrucción Java case: case 10 -> "OCTUBRE";
            case 10 -> "OCTUBRE";
            // [Doc] Ejecuta la instrucción Java case: case 11 -> "NOVIEMBRE";
            case 11 -> "NOVIEMBRE";
            // [Doc] Ejecuta la instrucción Java case: case 12 -> "DICIEMBRE";
            case 12 -> "DICIEMBRE";
            // [Doc] Ejecuta la instrucción Java case: default -> "DESCONOCIDO";
            default -> "DESCONOCIDO";
        };
    }

}
