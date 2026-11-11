package com.pequelandia.pequelandia.controller;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.repository.IPedidoRepository;
import com.pequelandia.pequelandia.service.INotificacionService;
import com.pequelandia.pequelandia.service.IPedidoService;

/** [Doc]
 * Presenta pedidos para administración y delega los cambios de estado.
 */
@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

  // [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
  @Autowired private IPedidoRepository pedidoRepo;
  // [Doc] Atributo pedidoService (IPedidoService): colaborador que concentra operaciones de pedido.
  @Autowired private IPedidoService pedidoService;

  
  
  /** [Doc]
   * Consulta pedidos con los filtros administrativos recibidos y prepara su listado en el panel.
   * @param estado estado usado por la operación.
   * @param desde inicio del intervalo consultado.
   * @param hasta fin del intervalo consultado.
   * @param usuario cuenta de usuario utilizada por la operación.
   * @param page índice de página solicitado, comenzando en cero.
   * @param size size.
   * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
   */
  @GetMapping("/lista")
  @ResponseBody
  public Map<String, Object> lista(
          @RequestParam(required = false) String estado,

          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate desde,

          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate hasta,

          @RequestParam(required = false) String usuario,

          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size
  ) {

      if (desde != null && hasta != null && desde.isAfter(hasta)) {
          throw new IllegalArgumentException("La fecha Desde debe ser anterior o igual a Hasta.");
      }

      // Evitar valores inválidos desde la URL
      // [Doc] Variable local paginaNormalizada (int): pagina normalizada. Se inicializa con Math.max(page, 0).
      int paginaNormalizada = Math.max(page, 0);
      // [Doc] Variable local tamanioNormalizado (int): tamanio normalizado. Se inicializa con Math.max(1, Math.min(size, 100)).
      int tamanioNormalizado = Math.max(1, Math.min(size, 100));

      // [Doc] Variable local estadoNormalizado (String): estado normalizado. Se inicializa con estado == null ? "" : estado.trim().
      String estadoNormalizado =
              estado == null ? "" : estado.trim();

      // [Doc] Variable local usuarioNormalizado (String): usuario normalizado. Se inicializa con usuario == null ? "" : usuario.trim().
      String usuarioNormalizado =
              usuario == null ? "" : usuario.trim();

      // [Doc] Variable local pageable (PageRequest): parámetros de paginación y orden. Se inicializa con PageRequest.of( paginaNormalizada, tamanioNormalizado ).
      PageRequest pageable = PageRequest.of(
              paginaNormalizada,
              tamanioNormalizado
      );

      // [Doc] Variable local pedidosPage (Page<Pedido>): pedidos page. Se inicializa con pedidoRepo.filtrarPedidosAdmin( estadoNormalizado, desde, hasta, usuarioNormalizado, pageable ).
      Page<Pedido> pedidosPage = pedidoRepo.filtrarPedidosAdmin(
              estadoNormalizado,
              desde,
              hasta,
              usuarioNormalizado,
              pageable
      );

      // [Doc] Variable local content (List<Map<String, Object>>): content. Se inicializa con pedidosPage.getContent() .stream() .map(p -> Map.<String, Object>of( "id", p.getId_pedido(), "fecha", Strin....
      List<Map<String, Object>> content =
              pedidosPage.getContent()
                      .stream()
                      .map(p -> Map.<String, Object>of(
                              "id", p.getId_pedido(),

                              "fecha",
                              String.valueOf(p.getFecha_pedido()),

                              "usuario",
                              p.getUsuario() != null
                                      ? p.getUsuario().getNombre_usuario()
                                      : "—",

                              "estado",
                              p.getEstado_pedido() != null
                                      ? p.getEstado_pedido()
                                      : "PENDIENTE",

                              "total",
                              p.getCarrito() != null
                                      && p.getCarrito().getTotal() != null
                                      ? p.getCarrito().getTotal()
                                      : BigDecimal.ZERO
                      ))
                      .toList();

      // [Doc] Variable local hoy (LocalDate): hoy. Se inicializa con LocalDate.now().
      LocalDate hoy = LocalDate.now();

      // [Doc] Variable local resumen (Map<String, Object>): resumen. Se inicializa con Map.of( "totalPedidos", pedidoRepo.count(), "pedidosHoy", pedidoRepo.contarPedidosDelDia(hoy), "pendientes"....
      Map<String, Object> resumen = Map.of(
              "totalPedidos",
              pedidoRepo.count(),

              "pedidosHoy",
              pedidoRepo.contarPedidosDelDia(hoy),

              "pendientes",
              pedidoRepo.contarPorEstadoSinDistinguirMayusculas("PENDIENTE"),

              "cancelados",
              pedidoRepo.contarPorEstadoSinDistinguirMayusculas("CANCELADO")
      );

      // [Doc] Termina el método o la función y devuelve Map.of( "content", content, "page", pedidosPage.getNumber(), "size", pedidosPage.getSize(), "totalPages", pedidosPage.getTotalPages(), "totalElements", pedid....
      return Map.of(
              "content", content,
              "page", pedidosPage.getNumber(),
              "size", pedidosPage.getSize(),
              "totalPages", pedidosPage.getTotalPages(),
              "totalElements", pedidosPage.getTotalElements(),
              "resumen", resumen
      );
  }
  
  
  /** [Doc]
   * Cambia estado en el ámbito de AdminPedidoController.
   * @param id identificador del registro.
   * @param estado estado usado por la operación.
   * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
   */
  @PostMapping("/{id}/estado")
  @ResponseBody
  public Map<String,Object> cambiarEstado(@PathVariable Long id, @RequestParam String estado){
      // [Doc] Ejecuta la llamada o modificación pedidoService.cambiarEstadoConReposicion(id, estado); Sus efectos dependen del método invocado y del objeto receptor.
      pedidoService.cambiarEstadoConReposicion(id, estado);
      // [Doc] Termina el método o la función y devuelve Map.of("ok", true).
      return Map.of("ok", true);
  }
}
