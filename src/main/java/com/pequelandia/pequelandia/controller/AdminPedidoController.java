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

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

  @Autowired private IPedidoRepository pedidoRepo;
  @Autowired private IPedidoService pedidoService;

  
  
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

      // Evitar valores inválidos desde la URL
      int paginaNormalizada = Math.max(page, 0);
      int tamanioNormalizado = Math.max(1, Math.min(size, 100));

      String estadoNormalizado =
              estado == null ? "" : estado.trim();

      String usuarioNormalizado =
              usuario == null ? "" : usuario.trim();

      PageRequest pageable = PageRequest.of(
              paginaNormalizada,
              tamanioNormalizado
      );

      Page<Pedido> pedidosPage = pedidoRepo.filtrarPedidosAdmin(
              estadoNormalizado,
              desde,
              hasta,
              usuarioNormalizado,
              pageable
      );

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

      LocalDate hoy = LocalDate.now();

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

      return Map.of(
              "content", content,
              "page", pedidosPage.getNumber(),
              "size", pedidosPage.getSize(),
              "totalPages", pedidosPage.getTotalPages(),
              "totalElements", pedidosPage.getTotalElements(),
              "resumen", resumen
      );
  }
  
  
  @PostMapping("/{id}/estado")
  @ResponseBody
  public Map<String,Object> cambiarEstado(@PathVariable Long id, @RequestParam String estado){
      pedidoService.cambiarEstadoConReposicion(id, estado);
      return Map.of("ok", true);
  }
}
