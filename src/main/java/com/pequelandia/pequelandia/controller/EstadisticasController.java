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

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/estadisticas")
public class EstadisticasController {
	
	@Autowired
    private  IEstadisticasService estadisticasService;

    // ================================
    // 1) VISTA PRINCIPAL – DASHBOARD
    // ================================
    @GetMapping
    public String dashboard(Model model) {
        EstadisticasAdminDTO dto = estadisticasService.getDashboardResumen();

        model.addAttribute("estad", dto);
        model.addAttribute("seccion", "estadisticas");
    
        return "paneladministracion";
    }

    // ================================
    // 2) API JSON PARA GRÁFICOS
    // ================================
    @GetMapping("/serie-dia")
    @ResponseBody
    public List<PuntoSerieDTO> serieDia(
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta) {

        return estadisticasService.getVentasPorDia(desde, hasta);
    }

    @GetMapping("/serie-mes")
    @ResponseBody
    public List<PuntoSerieDTO> serieMes(@RequestParam int anio) {
        return estadisticasService.getVentasPorMes(anio);
    }

    @GetMapping("/serie-anio")
    @ResponseBody
    public List<PuntoSerieDTO> serieAnio(
            @RequestParam int inicio,
            @RequestParam int fin) {

        return estadisticasService.getVentasPorAnio(inicio, fin);
    }
}
