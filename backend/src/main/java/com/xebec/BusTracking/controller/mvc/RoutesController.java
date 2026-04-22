package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.dto.RouteDto;
import com.xebec.BusTracking.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RoutesController {

    private final RouteService routeService;

    @GetMapping("/routes")
    public String showRoutePage(Model model) {
        model.addAttribute("activePage", "routes");
        model.addAttribute("routes", routeService.getAllRoutes());
        model.addAttribute("routeForm", new RouteDto());
        return "admin/routes";
    }

    @PostMapping("/routes/add")
    public String addRoute(@Valid @ModelAttribute("routeForm") RouteDto routeForm,
                           BindingResult result,
                           Model model,
                           RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "routes");
            model.addAttribute("routes", routeService.getAllRoutes());
            return "admin/routes";
        }
        try {
            routeService.addRoute(routeForm);
            flash.addFlashAttribute("successMessage", "Route created successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/routes";
    }

    @PostMapping("/routes/edit")
    public String editRoute(@RequestParam("id") Long id,
                            @Valid @ModelAttribute RouteDto routeForm,
                            BindingResult result,
                            Model model,
                            RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "routes");
            model.addAttribute("routes", routeService.getAllRoutes());
            model.addAttribute("routeForm", new RouteDto());
            return "admin/routes";
        }
        try {
            routeService.updateRoute(id, routeForm);
            flash.addFlashAttribute("successMessage", "Route updated successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/routes";
    }

    @PostMapping("/routes/delete")
    public String deleteRoute(@RequestParam("id") Long id, RedirectAttributes flash) {
        try {
            routeService.deleteRoute(id);
            flash.addFlashAttribute("successMessage", "Route deleted successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/routes";
    }

    @GetMapping("/routes/{id}")
    public String viewRouteStops(@PathVariable("id") Long id, RedirectAttributes flash) {
        flash.addFlashAttribute("errorMessage", "Route stop detail page is not yet implemented.");
        return "redirect:/routes";
    }
}
