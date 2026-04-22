package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.dto.StopDto;
import com.xebec.BusTracking.repository.StopRepository;
import com.xebec.BusTracking.service.StopService;
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
public class StopsController {

    private final StopRepository stopRepository;
    private final StopService stopService;

    @GetMapping("/stops")
    public String showStopPage(Model model) {
        model.addAttribute("activePage", "stops");
        model.addAttribute("stops", stopRepository.findAll());
        model.addAttribute("stopForm", new StopDto());
        return "admin/stops";
    }

    @PostMapping("/stops/add")
    public String addStop(@Valid @ModelAttribute("stopForm") StopDto stopForm,
                          BindingResult result,
                          Model model,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "stops");
            model.addAttribute("stops", stopRepository.findAll());
            return "admin/stops";
        }
        try {
            stopService.addStop(stopForm);
            flash.addFlashAttribute("successMessage", "Stop created successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/stops";
    }

    @PostMapping("/stops/edit")
    public String editStop(@RequestParam("id") Long id,
                           @Valid @ModelAttribute StopDto stopForm,
                           BindingResult result,
                           Model model,
                           RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "stops");
            model.addAttribute("stops", stopRepository.findAll());
            model.addAttribute("stopForm", new StopDto());
            return "admin/stops";
        }
        try {
            stopService.updateStop(id, stopForm);
            flash.addFlashAttribute("successMessage", "Stop updated successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/stops";
    }

    @PostMapping("/stops/delete")
    public String deleteStop(@RequestParam("id") Long id, RedirectAttributes flash) {
        try {
            stopService.deleteStop(id);
            flash.addFlashAttribute("successMessage", "Stop deleted successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/stops";
    }
}
