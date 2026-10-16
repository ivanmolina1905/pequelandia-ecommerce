package com.pequelandia.pequelandia.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.dto.PanelUsuarioDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.repository.IPedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PanelUsuarioService implements IPanelUsuarioService {

@Autowired
  private  IPedidoRepository pedidoRepo;

  public PanelUsuarioDTO build(Long usuarioId) {

    Double totalGastado = pedidoRepo.totalGastadoPorUsuario(usuarioId);
    Long totalProductos = pedidoRepo.totalProductosComprados(usuarioId);
    long totalPedidos = pedidoRepo.countByUsuario_Id(usuarioId);
    

    PanelUsuarioDTO dto = new PanelUsuarioDTO();
    dto.setTotalGastado(totalGastado != null ? totalGastado : 0.0);
    dto.setTotalProductosComprados(totalProductos != null ? totalProductos : 0);
    dto.setTotalPedidos(totalPedidos);
    double promedioCompra = totalPedidos > 0
            ? dto.getTotalGastado() / totalPedidos
            : 0.0;

    dto.setPromedioCompra(promedioCompra);
    
    // Último pedido (sin paginar, buscamos 1)
    var page = pedidoRepo.findByUsuarioId(
        usuarioId,
        org.springframework.data.domain.PageRequest.of(0, 1)
    );

    if (!page.isEmpty()) {
      Pedido p = page.getContent().get(0);
      dto.setUltimoPedidoId(p.getId_pedido());
      dto.setUltimoPedidoEstado(p.getEstado_pedido());
      dto.setUltimoPedidoFecha(String.valueOf(p.getFecha_pedido()));
    }

    // Serie mensual año actual
    int year = LocalDate.now().getYear();
    List<Object[]> serie = pedidoRepo.comprasPorMes(usuarioId, year);

    // Armamos 12 meses para que el gráfico no "salte"
    String[] nombres = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
    double[] valores = new double[12];
    for (Object[] row : serie) {
      int mes = ((Number) row[0]).intValue(); // 1..12
      double total = ((Number) row[1]).doubleValue();
      valores[mes-1] = total;
    }
    for (int i=0;i<12;i++){
      dto.getMeses().add(nombres[i]);
      dto.getTotales().add(valores[i]);
    }

    return dto;
  }
}
