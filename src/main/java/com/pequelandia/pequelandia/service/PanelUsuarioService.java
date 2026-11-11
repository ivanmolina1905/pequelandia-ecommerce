package com.pequelandia.pequelandia.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.dto.PanelUsuarioDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.repository.IPedidoRepository;

import lombok.RequiredArgsConstructor;

/** [Doc]
 * Reúne gasto, pedidos, cantidades y series mensuales del usuario para su panel.
 */
@Service
@RequiredArgsConstructor
public class PanelUsuarioService implements IPanelUsuarioService {

// [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
@Autowired
  private  IPedidoRepository pedidoRepo;

  /** [Doc]
   * Calcula gasto, unidades, cantidad y promedio de pedidos, obtiene el último pedido y completa los doce meses
   * del año con sus importes.
   * @param usuarioId identificador del usuario.
   * @return PanelUsuarioDTO con el resultado de la operación; véase el cuerpo para las ramas de error.
   */
  public PanelUsuarioDTO build(Long usuarioId) {

    // [Doc] Variable local totalGastado (Double): total gastado. Se inicializa con pedidoRepo.totalGastadoPorUsuario(usuarioId).
    Double totalGastado = pedidoRepo.totalGastadoPorUsuario(usuarioId);
    // [Doc] Variable local totalProductos (Long): total productos. Se inicializa con pedidoRepo.totalProductosComprados(usuarioId).
    Long totalProductos = pedidoRepo.totalProductosComprados(usuarioId);
    // [Doc] Variable local totalPedidos (long): total pedidos. Se inicializa con pedidoRepo.countByUsuario_Id(usuarioId).
    long totalPedidos = pedidoRepo.countByUsuario_Id(usuarioId);
    

    // [Doc] Variable local dto (PanelUsuarioDTO): objeto de transferencia con los datos de la operación. Se inicializa con new PanelUsuarioDTO().
    PanelUsuarioDTO dto = new PanelUsuarioDTO();
    // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setTotalGastado(totalGastado != null ? totalGastado : 0.0);
    dto.setTotalGastado(totalGastado != null ? totalGastado : 0.0);
    // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setTotalProductosComprados(totalProductos != null ? totalProductos : 0);
    dto.setTotalProductosComprados(totalProductos != null ? totalProductos : 0);
    // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setTotalPedidos(totalPedidos);
    dto.setTotalPedidos(totalPedidos);
    // [Doc] Variable local promedioCompra (double): promedio compra. Se inicializa con totalPedidos > 0 ? dto.getTotalGastado() / totalPedidos : 0.0.
    double promedioCompra = totalPedidos > 0
            ? dto.getTotalGastado() / totalPedidos
            : 0.0;

    // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setPromedioCompra(promedioCompra);
    dto.setPromedioCompra(promedioCompra);
    
    // Último pedido (sin paginar, buscamos 1)
    // [Doc] Variable local page (var; tipo inferido por Java): índice de página solicitado, comenzando en cero. Se inicializa con pedidoRepo.findByUsuarioId( usuarioId, org.springframework.data.domain.PageRequest.of(0, 1) ).
    var page = pedidoRepo.findByUsuarioId(
        usuarioId,
        org.springframework.data.domain.PageRequest.of(0, 1)
    );

    // [Doc] Evalúa !page.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (!page.isEmpty()) {
      // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con page.getContent().get(0).
      Pedido p = page.getContent().get(0);
      // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setUltimoPedidoId(p.getId_pedido());
      dto.setUltimoPedidoId(p.getId_pedido());
      // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setUltimoPedidoEstado(p.getEstado_pedido());
      dto.setUltimoPedidoEstado(p.getEstado_pedido());
      // [Doc] Actualiza una propiedad o configuración mediante la llamada: dto.setUltimoPedidoFecha(String.valueOf(p.getFecha_pedido()));
      dto.setUltimoPedidoFecha(String.valueOf(p.getFecha_pedido()));
    }

    // Serie mensual año actual
    // [Doc] Variable local year (int): year. Se inicializa con LocalDate.now().getYear().
    int year = LocalDate.now().getYear();
    // [Doc] Variable local serie (List<Object[]>): serie. Se inicializa con pedidoRepo.comprasPorMes(usuarioId, year).
    List<Object[]> serie = pedidoRepo.comprasPorMes(usuarioId, year);

    // Armamos 12 meses para que el gráfico no "salte"
    // [Doc] Variable local nombres (String[]): nombres. Se inicializa con {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"}.
    String[] nombres = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
    // [Doc] Variable local valores (double[]): valores. Se inicializa con new double[12].
    double[] valores = new double[12];
    // [Doc] Recorre los elementos o repite según Object[] row : serie. Cada vuelta ejecuta el cuerpo del bucle.
    for (Object[] row : serie) {
      // [Doc] Variable local mes (int): mes de la consulta. Se inicializa con ((Number) row[0]).intValue().
      int mes = ((Number) row[0]).intValue(); // 1..12
      // [Doc] Variable local total (double): importe acumulado. Se inicializa con ((Number) row[1]).doubleValue().
      double total = ((Number) row[1]).doubleValue();
      // [Doc] Ejecuta la llamada o modificación valores[mes-1] = total; Sus efectos dependen del método invocado y del objeto receptor.
      valores[mes-1] = total;
    }
    // [Doc] Recorre los elementos o repite según int i=0;i<12;i++. Cada vuelta ejecuta el cuerpo del bucle.
    for (int i=0;i<12;i++){
      // [Doc] Ejecuta la llamada o modificación dto.getMeses().add(nombres[i]); Sus efectos dependen del método invocado y del objeto receptor.
      dto.getMeses().add(nombres[i]);
      // [Doc] Ejecuta la llamada o modificación dto.getTotales().add(valores[i]); Sus efectos dependen del método invocado y del objeto receptor.
      dto.getTotales().add(valores[i]);
    }

    // [Doc] Termina el método o la función y devuelve dto.
    return dto;
  }
}
