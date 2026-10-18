package com.cesy.chessapp.controller;

import com.cesy.chessapp.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String showGame(Model model) {
        model.addAttribute("state", gameService.getGameState());
        return "game";
    }

    @PostMapping("/api/match/move")
    public String handleMove(@RequestParam int row, @RequestParam int col, Model model) {
        gameService.processInteraction(row, col);
        model.addAttribute("state", gameService.getGameState());
        // Retorna el bloque que actualiza tablero, paneles y errores vía HTMX
        return "components/fragments :: game-updates";
    }
    @PostMapping("/api/match/reset")
    public String resetGame(Model model) {
        gameService.resetGame();
        model.addAttribute("state", gameService.getGameState());
        return "components/fragments :: game-updates";
    }
}