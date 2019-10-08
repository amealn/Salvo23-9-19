package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping("/games")
    public Map<String, Object> getAllGames2(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        if (!isGuest(authentication)) {
            dto.put("player", playerRepository.findByUserName(authentication.getName()).makePlayerDTO());
        } else {
            dto.put("player", "Guest");
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

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> createGame(Authentication authentication) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            Player creatorPlayer = playerRepository.findByUserName(authentication.getName());
            Date newDate = new Date();
            Game newGame = gameRepository.save(new Game());
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newDate, creatorPlayer, newGame));
            long gamePlayerID = newGamePlayer.getId();
            return new ResponseEntity<>(makeMap("gpid", gamePlayerID), HttpStatus.CREATED);
        }
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Object> joinGame(@PathVariable Long gameId, Authentication authentication) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            Player creatorPlayer = playerRepository.findByUserName(authentication.getName());
            Date newDate = new Date();
            Game currentGame = gameRepository.findById(gameId).get();
            if (currentGame == null) {
                return new ResponseEntity<>("No such game", HttpStatus.FORBIDDEN);
            } else {
                if (currentGame.getGamePlayers().stream().filter(player -> player.getId() > 0).count() > 1) {
                    return new ResponseEntity<>("Game is full", HttpStatus.FORBIDDEN);
                } else {
                    GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newDate, creatorPlayer, currentGame));
                    long gamePlayerID = newGamePlayer.getId();
                    return new ResponseEntity<>(makeMap("gpid", gamePlayerID), HttpStatus.CREATED);
                }
            }
        }
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> findGamePlayer(@PathVariable Long gamePlayerId,
                                                              Authentication authentication) {
        GamePlayer gp = gamePlayerRepository.findById(gamePlayerId).get();
        if (authentication.getName() == gp.getPlayer().getUserName()) {
            return ResponseEntity.ok(gp.makeGame2DTO());
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(player -> player.makePlayerLeaderboardDTO())
                .collect(Collectors.toList());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(@RequestParam("email") String email,
                                               @RequestParam("password") String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("No email given", HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            return new ResponseEntity<>("Name already used", HttpStatus.CONFLICT);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>("Name added", HttpStatus.CREATED);
    }

    @RequestMapping(value = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Object> addShips(@PathVariable Long gamePlayerId,
                                           @RequestBody Set<Ship> ships,
                                           Authentication authentication) {
        Player loggedPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer gpActual = gamePlayerRepository.findById(gamePlayerId).get();
        if (loggedPlayer == null) {
            return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
        } else if (gpActual == null) {
            return new ResponseEntity<>("Game doesn't exist", HttpStatus.UNAUTHORIZED);
        } /*else if (wrongGamePlayer(gpActual, loggedPlayer)) {
            return new ResponseEntity<>("Wrong game", HttpStatus.UNAUTHORIZED);
        }*/ else if (gpActual.getShips().size() >= 5) {
            return new ResponseEntity<>("Your ships are already placed", HttpStatus.FORBIDDEN);
        } else {
            for (Ship item : ships) {
                ships.forEach(ship -> ship.setGamePlayer(gpActual));
                shipRepository.saveAll(ships);
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<String> addSalvoes(@PathVariable Long gamePlayerId,
                                             Authentication authentication,
                                             @RequestBody Salvo salvo) {
        GamePlayer gp1 = gamePlayerRepository.findById(gamePlayerId).get();
        if (isGuest(authentication)) {
            return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
        } else if (gp1 == null) {
            return new ResponseEntity<>("Game doesn't exist", HttpStatus.UNAUTHORIZED);
        } else if (!hasTurnedSalvo(salvo, gp1.getSalvoes())) {
            gp1.addSalvo(salvo);
            salvo.setGamePlayer(gp1);
            salvoRepository.save(salvo);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else gp1.getSalvoes().isEmpty();
        return new ResponseEntity<>("Your salvoes are already placed", HttpStatus.FORBIDDEN);
    }

    /*private boolean hasTurnedSalvo(Salvo newSalvo, Set<Salvo> salvosGameplayer) {
        boolean hasSalvoes = false;
        for (Salvo salvo : salvosGameplayer) {
            if (salvo.getTurn().equals) {
                return;
            }
        }
    }

    private boolean wrongGamePlayer(GamePlayer gamePlayer, Player player) {
        return gamePlayerRepository.findById(g).get();
    }*/
}





