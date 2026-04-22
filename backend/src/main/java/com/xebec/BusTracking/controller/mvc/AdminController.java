package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.dto.DashboardDto;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.security.MyUserDetails;
import com.xebec.BusTracking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final RouteRepository routeRepository;
    private final DashboardService dashboardService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/login";
    }

    @ModelAttribute("adminName")
    public String adminName(@AuthenticationPrincipal UserDetails principal) {
        if(principal == null) return "";
        return ((MyUserDetails) principal).getUser().getFullName();
    }

    @ModelAttribute("adminEmail")
    public String adminEmail(@AuthenticationPrincipal UserDetails principal) {
        if(principal == null) return "";
        return principal.getUsername();
    }

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) {
        model.addAttribute("activePage", "dashboard");
        DashboardDto dashboard = dashboardService.getDashboard();

        // Templates reference these attributes directly (stats, recentBookings, etc.)
        model.addAttribute("stats", dashboard.getStats());
        model.addAttribute("weeklyTicketSales", dashboard.getWeeklyTicketSales());
        model.addAttribute("weeklyRevenue", dashboard.getWeeklyRevenue());
        model.addAttribute("recentBookings", dashboard.getRecentBookings());
        model.addAttribute("activityFeed", dashboard.getActivityFeed());

        return "admin/dashboard";
    }
}