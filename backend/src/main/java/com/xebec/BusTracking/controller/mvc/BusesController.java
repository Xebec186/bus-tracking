package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.dto.BusDto;
import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.repository.UserRepository;
import com.xebec.BusTracking.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BusesController {

    private final BusService busService;
    private final UserRepository userRepository;
    private final BusRepository busRepository;

    @GetMapping("/buses")
    public String showBusPage(Model model) {
        model.addAttribute("activePage", "buses");
        model.addAttribute("buses", busRepository.findAll());
        model.addAttribute("drivers", userRepository.findByRole(UserRole.DRIVER));
        model.addAttribute("busForm", new BusDto());
        return "admin/buses";
    }

    @PostMapping("/buses/add")
    public String addBus(@Valid @ModelAttribute("busForm") BusDto busForm,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "buses");
            model.addAttribute("buses", busRepository.findAll());
            model.addAttribute("drivers", userRepository.findByRole(UserRole.DRIVER));
            return "admin/buses";
        }
        try {
            busService.addBus(busForm);
            flash.addFlashAttribute("successMessage", "Bus created successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buses";
    }

    @PostMapping("/buses/edit")
    public String editBus(@RequestParam("id") Long id,
                          @Valid @ModelAttribute BusDto busForm,
                          BindingResult result,
                          RedirectAttributes flash,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "buses");
            model.addAttribute("buses", busRepository.findAll());
            model.addAttribute("drivers", userRepository.findByRole(UserRole.DRIVER));
            model.addAttribute("busForm", new BusDto());
            return "admin/buses";
        }
        try {
            busService.updateBus(id, busForm);
            flash.addFlashAttribute("successMessage", "Bus updated successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buses";
    }

    @PostMapping("/buses/delete")
    public String deleteBus(@RequestParam("id") Long id, RedirectAttributes flash) {
        try {
            busService.deleteBus(id);
            flash.addFlashAttribute("successMessage", "Bus deleted successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buses";
    }
}
