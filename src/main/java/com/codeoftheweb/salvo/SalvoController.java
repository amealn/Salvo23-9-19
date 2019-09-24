package com.codeoftheweb.salvo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                                 .map(game -> makeGameDTO(game))
                                 .collect(Collectors.toList());
        }
        private Map<String, Object> makeGameDTO(Game game) {
                Map<String, Object> dto = new LinkedHashMap<String, Object>();
                dto.put("id", game.getId());
                dto.put("created", game.getCreationDate());
                dto.put("game players", makeGamePlayerDTO(game.getGamePlayers()));
                return dto;
        }
        private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
                Map<String, Object> dto = new LinkedHashMap<String, Object>();
                dto.put("id", gamePlayer.getId());
                dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
                return dto;
        }
        private Map<String, Object> makePlayerDTO(Player player) {
                Map<String, Object> dto = new LinkedHashMap<String, Object>();
                dto.put("id", player.getId());
                dto.put("email", player.getUserName());
                return dto;
        }

}
