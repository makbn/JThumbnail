package io.github.makbn.jthumbnail.webservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Entry point for the admin UI: job list, retry failed, thumbnail preview.
 * Redirects to static {@code /admin/index.html}.
 */
@Controller
public class AdminController {

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/admin/index.html";
    }
}
