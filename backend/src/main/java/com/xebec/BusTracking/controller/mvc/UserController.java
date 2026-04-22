package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.dto.PagedResult;
import com.xebec.BusTracking.dto.user.*;
import com.xebec.BusTracking.exception.*;
import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.model.UserStatus;
import com.xebec.BusTracking.security.MyUserDetails;
import com.xebec.BusTracking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── LIST ─────────────────────────────────────────────────────────────
    @GetMapping
    public String list(UserFilterDto filter,
                       Model model,
                       @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("activePage", "users");

        PagedResult<UserListItemDto> paged = userService.findFiltered(filter);

        model.addAttribute("paged",      paged);
        model.addAttribute("users",      paged.getContent());
        model.addAttribute("filter",     filter);

        // Stats pills
        model.addAttribute("totalUsers",      userService.countTotal());
        model.addAttribute("activeUsers",     userService.countActive());
        model.addAttribute("totalAdmins",     userService.countByRole(UserRole.ADMIN));
        model.addAttribute("totalDrivers",    userService.countByRole(UserRole.DRIVER));
        model.addAttribute("totalPassengers", userService.countByRole(UserRole.PASSENGER));

        // Blank forms for modals
        model.addAttribute("createForm",  new UserCreateFormDto());
        model.addAttribute("roles",       UserRole.values());
        model.addAttribute("statuses",    UserStatus.values());
        model.addAttribute("currentUserId", resolveCurrentUserId(principal));

        return "admin/users";
    }

    // ── CREATE ───────────────────────────────────────────────────────────
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("createForm") UserCreateFormDto form,
                         BindingResult result,
                         Model model,
                         UserFilterDto filter,
                         RedirectAttributes flash,
                         @AuthenticationPrincipal UserDetails principal) {

        if (result.hasErrors()) {
            populateListModel(model, filter, principal);
            return "admin/users";   // redisplay with errors, modal re-opens via JS
        }

        try {
            userService.create(form);
            flash.addFlashAttribute("successMessage",
                    "User " + form.getFirstName() + " " + form.getLastName() + " created successfully.");
        } catch (PasswordsDoNotMatchException e) {
            result.rejectValue("confirmPassword", "mismatch", e.getMessage());
            populateListModel(model, filter, principal);
            return "admin/users";
        } catch (EmailAlreadyExistsException e) {
            result.rejectValue("email", "duplicate", e.getMessage());
            populateListModel(model, filter, principal);
            return "admin/users";
        }
        return "redirect:/users";
    }

    // ── GET EDIT FORM (AJAX / modal pre-fill) ────────────────────────────
    @GetMapping("/{id}/edit-form")
    @ResponseBody
    public UserEditFormDto getEditForm(@PathVariable Long id) {
        return userService.findEditFormById(id);
    }

    // ── UPDATE PROFILE ───────────────────────────────────────────────────
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("editForm") UserEditFormDto form,
                         BindingResult result,
                         Model model,
                         UserFilterDto filter,
                         RedirectAttributes flash,
                         @AuthenticationPrincipal UserDetails principal) {

        Long currentAdminId = resolveCurrentUserId(principal);

        if (result.hasErrors()) {
            populateListModel(model, filter, principal);
            return "admin/users";
        }
        try {
            userService.update(form, currentAdminId);
            flash.addFlashAttribute("successMessage", "User updated successfully.");
        } catch (PasswordsDoNotMatchException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        } catch (EmailAlreadyExistsException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ── CHANGE ROLE ──────────────────────────────────────────────────────
    @PostMapping("/change-role")
    public String changeRole(@Valid @ModelAttribute UserRoleChangeDto dto,
                             BindingResult result,
                             RedirectAttributes flash,
                             @AuthenticationPrincipal UserDetails principal) {

        if (result.hasErrors()) {
            flash.addFlashAttribute("errorMessage", "Invalid role change request.");
            return "redirect:/users";
        }
        try {
            userService.changeRole(dto, resolveCurrentUserId(principal));
            flash.addFlashAttribute("successMessage", "User role updated successfully.");
        } catch (SelfPrivilegeChangeException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        } catch (ResourceAccessException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ── CHANGE STATUS ────────────────────────────────────────────────────
    @PostMapping("/change-status")
    public String changeStatus(@Valid @ModelAttribute UserStatusChangeDto dto,
                               BindingResult result,
                               RedirectAttributes flash,
                               @AuthenticationPrincipal UserDetails principal) {

        if (result.hasErrors()) {
            flash.addFlashAttribute("errorMessage", "Invalid status change request.");
            return "redirect:/users";
        }
        try {
            userService.changeStatus(dto, resolveCurrentUserId(principal));
            flash.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (SelfPrivilegeChangeException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ── DELETE ───────────────────────────────────────────────────────────
    @PostMapping("/delete")
    public String delete(@RequestParam Long targetUserId,
                         RedirectAttributes flash,
                         @AuthenticationPrincipal UserDetails principal) {

        try {
            userService.delete(targetUserId, resolveCurrentUserId(principal));
            flash.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (SelfDeleteException | SelfPrivilegeChangeException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        } catch (ResourceNotFoundException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ── HELPERS ──────────────────────────────────────────────────────────

    /**
     * Resolves the DB primary key of the currently authenticated admin.
     */
    private Long resolveCurrentUserId(UserDetails principal) {
        return ((MyUserDetails) principal).getUser().getId();
    }

    private void populateListModel(Model model, UserFilterDto filter,
                                   UserDetails principal) {
        PagedResult<UserListItemDto> paged = userService.findFiltered(filter);
        model.addAttribute("paged",           paged);
        model.addAttribute("users",           paged.getContent());
        model.addAttribute("filter",          filter);
        model.addAttribute("totalUsers",      userService.countTotal());
        model.addAttribute("activeUsers",     userService.countActive());
        model.addAttribute("totalAdmins",     userService.countByRole(UserRole.ADMIN));
        model.addAttribute("totalDrivers",    userService.countByRole(UserRole.DRIVER));
        model.addAttribute("totalPassengers", userService.countByRole(UserRole.PASSENGER));
        model.addAttribute("roles",           UserRole.values());
        model.addAttribute("statuses",        UserStatus.values());
        model.addAttribute("currentUserId",   resolveCurrentUserId(principal));
    }
}
