package com.pequelandia.pequelandia.service;

import java.time.LocalDate;
import java.util.List;

import com.pequelandia.pequelandia.dto.EstadisticasAdminDTO;
import com.pequelandia.pequelandia.dto.PuntoSerieDTO;

public interface IEstadisticasService {

    EstadisticasAdminDTO getDashboardResumen();

    // Series por día (entre fechas)
    List<PuntoSerieDTO> getVentasPorDia(LocalDate desde, LocalDate hasta);

    // Serie por mes (un año)
    List<PuntoSerieDTO> getVentasPorMes(int anio);

    // Serie anual (varios años)
    List<PuntoSerieDTO> getVentasPorAnio(int anioInicio, int anioFin);
}
