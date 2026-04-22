package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.repository.TicketRepository;
import com.xebec.BusTracking.service.RouteService;
import com.xebec.BusTracking.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class TicketsController {

    private final TicketRepository ticketRepository;
    private final RouteService routeService;
    private final TicketService ticketService;

    @GetMapping("/tickets")
    public String showTicketsPage(Model model) {
        model.addAttribute("activePage", "tickets");
        model.addAttribute("tickets", ticketRepository.findAll());
        model.addAttribute("routes", routeService.getAllRoutes());
        return "admin/tickets";
    }

    @GetMapping("/tickets/export")
    public String exportTickets(RedirectAttributes flash) {
        flash.addFlashAttribute("errorMessage", "Ticket export is not yet implemented.");
        return "redirect:/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable("id") Long id, RedirectAttributes flash) {
        flash.addFlashAttribute("errorMessage", "Ticket detail page is not yet implemented.");
        return "redirect:/tickets";
    }

    @PostMapping("/tickets/cancel")
    public String cancelTicket(@RequestParam("id") Long id, RedirectAttributes flash) {
        try {
            ticketService.cancelTicket(id);
            flash.addFlashAttribute("successMessage", "Ticket cancelled successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/tickets";
    }

}
