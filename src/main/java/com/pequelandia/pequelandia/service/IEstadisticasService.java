package com.pequelandia.pequelandia.service;

import java.time.LocalDate;
import java.util.List;

import com.pequelandia.pequelandia.dto.EstadisticasAdminDTO;
import com.pequelandia.pequelandia.dto.PuntoSerieDTO;

/** [Doc]
 * Define el contrato de operaciones de estadisticas que implementa el servicio correspondiente.
 */
public interface IEstadisticasService {

    /** [Doc]
     * Devuelve dashboard resumen, según el cuerpo o la implementación del contrato.
     * @return EstadisticasAdminDTO con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    EstadisticasAdminDTO getDashboardResumen();

    // Series por día (entre fechas)
    /** [Doc]
     * Devuelve ventas por dia, según el cuerpo o la implementación del contrato.
     * @param desde inicio del intervalo consultado.
     * @param hasta fin del intervalo consultado.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<PuntoSerieDTO> getVentasPorDia(LocalDate desde, LocalDate hasta);

    // Serie por mes (un año)
    /** [Doc]
     * Devuelve ventas por mes, según el cuerpo o la implementación del contrato.
     * @param anio año de la consulta.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<PuntoSerieDTO> getVentasPorMes(int anio);

    // Serie anual (varios años)
    /** [Doc]
     * Devuelve ventas por anio, según el cuerpo o la implementación del contrato.
     * @param anioInicio anio inicio.
     * @param anioFin anio fin.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<PuntoSerieDTO> getVentasPorAnio(int anioInicio, int anioFin);
}
