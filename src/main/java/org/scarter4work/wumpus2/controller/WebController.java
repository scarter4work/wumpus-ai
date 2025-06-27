package org.scarter4work.wumpus2.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for web pages.
 */
@Slf4j
@Controller
public class WebController {

    /**
     * Redirect the root URL to the game.
     * 
     * @return Redirect to index.html
     */
    @GetMapping("/")
    public String index() {
        log.info("Redirecting root URL to index.html");
        return "redirect:/index.html";
    }
}
