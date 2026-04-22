package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Route;
import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.ScheduleDay;
import com.xebec.BusTracking.model.ScheduleStatus;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.ScheduleDayRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.service.RouteService;
import com.xebec.BusTracking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SchedulesController {

    private final ScheduleRepository scheduleRepository;
    private final BusRepository busRepository;
    private final RouteService routeService;
    private final RouteRepository routeRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleService scheduleService;

    @GetMapping("/schedules")
    public String showSchedulesPage(Model model) {
        model.addAttribute("activePage", "schedules");
        model.addAttribute("schedules", scheduleRepository.findAll());
        model.addAttribute("buses", busRepository.findAll());
        model.addAttribute("routes", routeService.getAllRoutes());
        model.addAttribute("scheduleForm", new Object());
        return "admin/schedules";
    }

    @PostMapping("/schedules/add")
    public String addSchedule(@RequestParam("busId") Long busId,
                              @RequestParam("routeId") Long routeId,
                              @RequestParam("effectiveDate") java.time.LocalDate effectiveDate,
                              @RequestParam(value = "expiryDate", required = false) java.time.LocalDate expiryDate,
                              @RequestParam("status") String status,
                              @RequestParam Map<String, String> params,
                              RedirectAttributes flash) {
        try {
            Schedule schedule = new Schedule();
            schedule.setBus(busRepository.findById(busId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bus not found")));
            schedule.setRoute(routeServiceEntity(routeId));
            schedule.setEffectiveDate(effectiveDate);
            schedule.setExpiryDate(expiryDate);
            schedule.setStatus(ScheduleStatus.valueOf(status.toUpperCase(Locale.ROOT)));
            Schedule saved = scheduleRepository.save(schedule);

            List<ScheduleDay> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                String day = params.get("scheduleDays[" + i + "].day");
                String enabled = params.get("scheduleDays[" + i + "].enabled");
                String departure = params.get("scheduleDays[" + i + "].departureTime");
                String arrival = params.get("scheduleDays[" + i + "].arrivalTime");
                if (enabled == null || day == null || departure == null || departure.isBlank() || arrival == null || arrival.isBlank()) {
                    continue;
                }

                ScheduleDay scheduleDay = new ScheduleDay();
                scheduleDay.setSchedule(saved);
                scheduleDay.setDay(DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT)));
                scheduleDay.setDepartureTime(LocalTime.parse(departure));
                scheduleDay.setArrivalTime(LocalTime.parse(arrival));
                days.add(scheduleDay);
            }
            scheduleDayRepository.saveAll(days);

            flash.addFlashAttribute("successMessage", "Schedule created successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/schedules";
    }

    @GetMapping("/schedules/edit/{id}")
    public String editSchedulePage(@PathVariable("id") Long id, RedirectAttributes flash) {
        flash.addFlashAttribute("errorMessage", "Inline schedule edit form is not yet implemented.");
        return "redirect:/schedules";
    }

    @PostMapping("/schedules/delete")
    public String deleteSchedule(@RequestParam("id") Long id, RedirectAttributes flash) {
        try {
            scheduleService.deleteSchedule(id);
            flash.addFlashAttribute("successMessage", "Schedule deleted successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/schedules";
    }

    private Route routeServiceEntity(Long routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
    }

}
