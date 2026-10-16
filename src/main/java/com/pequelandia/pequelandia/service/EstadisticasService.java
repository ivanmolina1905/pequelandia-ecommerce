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

@Service
@Transactional
@RequiredArgsConstructor
public class EstadisticasService implements IEstadisticasService {

	@Autowired
    private  IPedidoRepository pedidoRepo;
	@Autowired
    private  IUsuarioRepository usuarioRepo;
	@Autowired
    private  IProductoRepository productoRepo;

	@Override
    public EstadisticasAdminDTO getDashboardResumen() {

        EstadisticasAdminDTO dto = new EstadisticasAdminDTO();

        // --- Totales principales ---
        dto.setTotalUsuarios(usuarioRepo.count());
        dto.setTotalProductos(productoRepo.count());
        dto.setTotalPedidos(pedidoRepo.contarPedidos());

        // --- Ventas ---
        Double vHoy = pedidoRepo.ventasDelDia(LocalDate.now());
        Double vMes = pedidoRepo.ventasDelMes(LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        
        // Año completo
        Double vAnio = pedidoRepo.ventasPorAnio(
                LocalDate.now().getYear(),
                LocalDate.now().getYear()
        ).stream()
         .map(arr -> ((Number) arr[1]).doubleValue())
         .findFirst()
         .orElse(0.0);

        dto.setVentasHoy(vHoy != null ? vHoy : 0);
        dto.setVentasMes(vMes != null ? vMes : 0);
        dto.setVentasAnio(vAnio != null ? vAnio : 0);

        // --- Top productos ---
        List<Object[]> top = pedidoRepo.productosMasVendidos();
        List<ProductoMasVendidoDTO> topList = top.stream()
                .map(arr -> new ProductoMasVendidoDTO(
                        ((Number) arr[0]).longValue(),
                        (String) arr[1],
                        ((Number) arr[2]).longValue()
                ))
                .collect(Collectors.toList());
        dto.setTopProductos(topList);

        // --- Stock bajo ---
        dto.setProductosStockBajo(productoRepo.obtenerStockBajo(3));

        // --- Usuarios nuevos del mes ---
        dto.setUsuariosNuevosMes(
            usuarioRepo.usuariosRegistradosPorMes(LocalDate.now().getYear()).stream()
                .filter(arr -> ((Number) arr[0]).intValue() == LocalDate.now().getMonthValue())
                .map(arr -> ((Number) arr[1]).longValue())
                .findFirst()
                .orElse(0L)
        );

        // --- Pedidos completados (entregados) ---
        long completados = pedidoRepo.countByEstadoPedido("ENTREGADO");
        dto.setPedidosCompletados(completados);
        return dto;
    }

    // =======================================================
    // 2) SERIE - VENTAS POR DÍA
    // =======================================================
    @Override
    public List<PuntoSerieDTO> getVentasPorDia(LocalDate desde, LocalDate hasta) {
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
    @Override
    public List<PuntoSerieDTO> getVentasPorMes(int anio) {
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
    @Override
    public List<PuntoSerieDTO> getVentasPorAnio(int inicio, int fin) {
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
    private String nombreMes(int mes) {
        return switch (mes) {
            case 1 -> "ENERO";
            case 2 -> "FEBRERO";
            case 3 -> "MARZO";
            case 4 -> "ABRIL";
            case 5 -> "MAYO";
            case 6 -> "JUNIO";
            case 7 -> "JULIO";
            case 8 -> "AGOSTO";
            case 9 -> "SEPTIEMBRE";
            case 10 -> "OCTUBRE";
            case 11 -> "NOVIEMBRE";
            case 12 -> "DICIEMBRE";
            default -> "DESCONOCIDO";
        };
    }

}
