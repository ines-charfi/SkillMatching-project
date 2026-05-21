package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final SessionService sessionService;

    public AdminController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        if (!sessionService.isAuthenticated()) return "redirect:/login";
        if (!sessionService.isAdmin()) return "redirect:/login";
        return "dashboard-admin";
    }

    @GetMapping("/admin/candidats")
    public String adminCandidats() {
        if (!sessionService.isAdmin()) return "redirect:/login";
        return "dashboard-admin";
    }

    @GetMapping("/admin/entreprises")
    public String adminEntreprises() {
        if (!sessionService.isAdmin()) return "redirect:/login";
        return "dashboard-admin";
    }

    @GetMapping("/admin/statistiques")
    public String adminStatistiques() {
        if (!sessionService.isAdmin()) return "redirect:/login";
        return "dashboard-admin";
    }

    @GetMapping("/admin/verification")
    public String adminVerification() {
        if (!sessionService.isAdmin()) return "redirect:/login";
        return "dashboard-admin";
    }
}