package com.codeoftheweb.salvo.controllers;


import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;


    @RequestMapping("/games")
    public List<Map<String, Object>> getAllGames() {
        return gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList());
    }

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> findGamePlayer(@PathVariable Long gamePlayerId) {
        return gamePlayerRepository.findById(gamePlayerId)
                .get()
                .makeGame2DTO();
    }

    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> getAllPlayers(){
        return playerRepository.findAll()
                .stream()
                .map(player -> player.makePlayerLeaderboardDTO())
                .collect(Collectors.toList());
    }

}