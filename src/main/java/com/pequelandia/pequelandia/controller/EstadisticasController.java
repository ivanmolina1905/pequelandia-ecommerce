package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.dto.EstadisticasAdminDTO;
import com.pequelandia.pequelandia.dto.PuntoSerieDTO;
import com.pequelandia.pequelandia.service.IEstadisticasService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** [Doc]
 * Publica el resumen administrativo y las series de ventas por día, mes y año.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/estadisticas")
public class EstadisticasController {
	
    // [Doc] Atributo estadisticasService (IEstadisticasService): colaborador que concentra operaciones de estadisticas.
	@Autowired
    private  IEstadisticasService estadisticasService;

    // ================================
    // 1) VISTA PRINCIPAL – DASHBOARD
    // ================================
    /** [Doc]
     * Carga el resumen de indicadores, activa la sección estadísticas y renderiza el panel administrativo.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping
    public String dashboard(Model model) {
        // [Doc] Variable local dto (EstadisticasAdminDTO): objeto de transferencia con los datos de la operación. Se inicializa con estadisticasService.getDashboardResumen().
        EstadisticasAdminDTO dto = estadisticasService.getDashboardResumen();

        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("estad", dto);
        model.addAttribute("estad", dto);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("seccion", "estadisticas");
        model.addAttribute("seccion", "estadisticas");
    
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "paneladministracion";
    }

    // ================================
    // 2) API JSON PARA GRÁFICOS
    // ================================
    /** [Doc]
     * Consulta la serie de ventas del intervalo de fechas para alimentar el gráfico diario.
     * @param desde inicio del intervalo consultado.
     * @param hasta fin del intervalo consultado.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/serie-dia")
    @ResponseBody
    public List<PuntoSerieDTO> serieDia(
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta) {

        // [Doc] Termina el método o la función y devuelve estadisticasService.getVentasPorDia(desde, hasta).
        return estadisticasService.getVentasPorDia(desde, hasta);
    }

    /** [Doc]
     * Consulta la serie de ventas agrupadas por mes para el año solicitado.
     * @param anio año de la consulta.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/serie-mes")
    @ResponseBody
    public List<PuntoSerieDTO> serieMes(@RequestParam int anio) {
        // [Doc] Termina el método o la función y devuelve estadisticasService.getVentasPorMes(anio).
        return estadisticasService.getVentasPorMes(anio);
    }

    /** [Doc]
     * Consulta la serie de ventas por año dentro del intervalo solicitado.
     * @param inicio límite inicial del intervalo.
     * @param fin límite final del intervalo.
     * @return List&lt;PuntoSerieDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/serie-anio")
    @ResponseBody
    public List<PuntoSerieDTO> serieAnio(
            @RequestParam int inicio,
            @RequestParam int fin) {

        // [Doc] Termina el método o la función y devuelve estadisticasService.getVentasPorAnio(inicio, fin).
        return estadisticasService.getVentasPorAnio(inicio, fin);
    }
}
