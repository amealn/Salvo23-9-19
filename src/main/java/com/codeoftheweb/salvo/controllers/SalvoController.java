package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @RequestMapping("/games")
    public Map<String, Object> getAllGames2(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        if (!isGuest(authentication)) {
            dto.put("player", authentication.getName());
        } else{
            dto.put("player", "guest");
         }
        dto.put("games", getAllGames());
        return dto;
    }
    public List<Map<String, Object>> getAllGames() {
        return gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList());
    }
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    /*@RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> createUser(Authentication authentication) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            Player creatorPlayer = playerRepository.findByUserName(authentication.getName());
            Date newDate = new Date();
            Game newGame = gameRepository.save(new Game());
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newDate, newGame, creatorPlayer));
            long gamePlayerID = newGamePlayer.getId();
            return new ResponseEntity<>(makeMap("gpid", gamePlayerID), HttpStatus.CREATED);
        }
    }
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }*/

    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> findGamePlayer(@PathVariable Long gamePlayerId, Authentication authentication) {
        return gamePlayerRepository.findById(gamePlayerId.)
                .get()
                .makeGame2DTO();
    }
    @RequestMapping("/game_view/{gamePlayerId}")
  public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long gamePlayerId, Authentication authentication) {
    Map<String, Object> dto;
    Long idAut = ((authentication) == null ? 0 : (authentication).getName());

    GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId);

    if (validarViewGameplayer(idAut, gamePlayer)) {

      dto = GameDTO(gamePlayer.getGame());
      dto.put("ships", gamePlayer.getShips().stream().map(ship -> ShipDTO(ship)).collect(Collectors.toList()));
      dto.put("salvoes", SalvoDTO(gamePlayer));


      return new ResponseEntity<>(makeMapCUP("gameView", dto), HttpStatus.ACCEPTED);
    } else {
      return new ResponseEntity<>(makeMapCUP("error", "Game view no authorized"), HttpStatus.UNAUTHORIZED);
    }
  }

    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> getAllPlayers(){
        return playerRepository.findAll()
                .stream()
                .map(player -> player.makePlayerLeaderboardDTO())
                .collect(Collectors.toList());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(@RequestParam("email") String email,
                                               @RequestParam("password") String password) {
        if (email.isEmpty()) {
            return new ResponseEntity<>("No email given", HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            return new ResponseEntity<>("Name already used", HttpStatus.CONFLICT);
        }

        playerRepository.save(new Player());
        return new ResponseEntity<>("Name added", HttpStatus.CREATED);
    }

}


