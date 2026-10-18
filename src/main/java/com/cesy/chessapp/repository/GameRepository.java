package com.cesy.chessapp.repository;

import com.cesy.chessapp.model.GameState;
import org.springframework.stereotype.Repository;

@Repository
public class GameRepository {
    private GameState currentGameState = new GameState();

    public GameState getGameState() { return currentGameState; }
    public void saveGameState(GameState state) { this.currentGameState = state; }
}