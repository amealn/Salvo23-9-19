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
    private ScoreRepository scoreRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        if (!isGuest(authentication)) {
            dto.put("player", playerRepository.findByUserName(authentication.getName()).makePlayerDTO());
        } else {
            dto.put("player", "Guest");
        }
        dto.put("games", getAllGames());
        return dto;
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
                    List<Long> playersId = currentGame.getPlayers().stream().map(Player::getId).collect(Collectors.toList());
                    Long AuthId = (creatorPlayer == null ? 0 : creatorPlayer.getId());
                    if (playersId.get(0) == AuthId) {
                        return new ResponseEntity<>("Player joining is the same as the creator of the game", HttpStatus.FORBIDDEN);
                    } else {
                        GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newDate, creatorPlayer, currentGame));
                        long gamePlayerID = newGamePlayer.getId();
                        return new ResponseEntity<>(makeMap("gpid", gamePlayerID), HttpStatus.CREATED);
                    }
                }
            }
        }
    }


    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(@RequestParam("email") String email,
                                               @RequestParam("password") String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Enter all data", HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            return new ResponseEntity<>("Name already used", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>("Player created", HttpStatus.CREATED);
    }

    @RequestMapping(value = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Object> addShips(@PathVariable Long gamePlayerId,
                                           @RequestBody List<Ship> ships,
                                           Authentication authentication) {
        Player loggedPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer gpActual = gamePlayerRepository.findById(gamePlayerId).get();
        if (loggedPlayer == null) {
            return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
        } else if (gpActual == null) {
            return new ResponseEntity<>("Gameplayer doesn't exist", HttpStatus.UNAUTHORIZED);
        } else if (authentication.getName() != gpActual.getPlayer().getUserName()) {
            return new ResponseEntity<>("Gameplayer is not current player", HttpStatus.UNAUTHORIZED);
        } else if (gpActual.getShips().size() >= 5) {
            return new ResponseEntity<>("Your ships are already placed", HttpStatus.FORBIDDEN);
        } else {
            for (Ship ship : ships) {
                Ship sh = new Ship();
                sh.setType(ship.getType());
                sh.setShipLocations(ship.getShipLocations());
                gpActual.addShip(sh);
                shipRepository.save(sh);
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<String> addSalvoes(@PathVariable Long gamePlayerId,
                                             Authentication authentication,
                                             @RequestBody Salvo salvo) {
        GamePlayer gpActual = gamePlayerRepository.findById(gamePlayerId).get();
        if (isGuest(authentication)) {
            return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
        } else if (gpActual == null) {
            return new ResponseEntity<>("Gameplayer doesn't exist", HttpStatus.UNAUTHORIZED);
        } else if (authentication.getName() != gpActual.getPlayer().getUserName()) {
            return new ResponseEntity<>("Gameplayer is not current player", HttpStatus.UNAUTHORIZED);
        } else {
            Set<Salvo> salvoes = gpActual.getSalvoes();
            if (salvo.getSalvoLocations().size() > 5) {
                return new ResponseEntity<>("Your salvoes are already placed", HttpStatus.FORBIDDEN);
            } else {
                Long turnRepeat = salvoes.stream().filter(salvo1 -> salvo1.getTurn() == salvo.getTurn()).collect(Collectors.counting());
                if (turnRepeat > 0) {
                    return new ResponseEntity<>("Can only fire salvoes once per turn", HttpStatus.FORBIDDEN);
                } else {
                    gpActual.addSalvo(salvo);
                    salvoRepository.save(salvo);
                    return new ResponseEntity<>(HttpStatus.CREATED);
                }
            }
        }
    }

    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(player -> player.makePlayerLeaderboardDTO())
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    public List<Map<String, Object>> getAllGames() {
        return gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList());
    }

    //Game_view te odio
    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> findGamePlayer(@PathVariable Long gamePlayerId,
                                                              Authentication authentication) {
        GamePlayer gpSelf = gamePlayerRepository.findById(gamePlayerId).get();
        GamePlayer gpOpponent = gamePlayerRepository.findById(gamePlayerId)
                .get()
                .getGame()
                .getGamePlayers()
                .stream()
                .filter(gp -> gp.getId() != gpSelf.getId())
                .findAny().orElse(null);

        Map<String, Object> dto;
        Map<String, Object> hits = new LinkedHashMap<String, Object>();

        if (authentication.getName() == gpSelf.getPlayer().getUserName()) {
            dto = makeGameViewDTO();
            dto.put("hits", hits);
            hits.put("self", getAllSelves());
            hits.put("opponent", getAllOpponents());
            return ResponseEntity.ok(dto);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    //DTO para Game_view/n
    public Map<String, Object> makeGameViewDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", getId());
        dto.put("created", this.game.getCreationDate());
        //dto.put("gameState", );
        dto.put("gamePlayers", getGame().getAllGamePlayers());
        dto.put("ships", getAllShips());
        dto.put("salvoes", getGame().getAllSalvoes());
        return dto;
    }

    //Dto para game_view/n
    public Map<String, Object> makeShipDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getShipLocations());
        return dto;
    }

    //DTO para Game_view/n
    public Map<String, Object> makeSalvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    //Los hits que el gpActual hace al oponente
    public Map<String, Object> makeSelfDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", );
        dto.put("hitLocations", getAllHitLocations());
        dto.put("damages", );
        dto.put("missed", );
        return dto;
    }

    //Los hits que el oponente le hace al gpActual
    public Map<String, Object> makeOpponentDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", );
        dto.put("hitLocations", getAllHitLocations());
        dto.put("damages", );
        dto.put("missed", );
        return dto;
    }
    //Lista para game_view/n

    public List<String> getAllHitLocations() {
        List<String> salvoList = getSalvoes()
                .stream()
                .flatMap(salvo -> salvo.getSalvoLocations().stream())
                .collect(Collectors.toList());
        List<String> shipList = getShips()
                .stream()
                .flatMap(ship -> ship.getShipLocations().stream())
                .collect(Collectors.toList());
        shipList.retainAll(salvoList);
        return shipList;
    }

    //Lista para game_view/n

    public List<Map<String, Object>> getAllSelves() {
        return game.gamePlayers
                .stream()
                .map(gamePlayer -> gamePlayer.makeSelfDTO())
                .collect(Collectors.toList());
    }
    //Lista para game_view/n

    public List<Map<String, Object>> getAllOpponents() {
        return game.gamePlayers
                .stream()
                .map(gamePlayer -> gamePlayer.makeOpponentDTO())
                .collect(Collectors.toList());
    }


    //Lista para game_view/n
    public List<Map<String, Object>> getAllShips() {
        return shipRepository.findAll()
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(Collectors.toList());
    }

    //List para Game_view/n
    public List<Object> getAllSalvoes() {
        return gamePlayerRepository.findAll()
                .stream()
                .flatMap(gamePlayer -> gamePlayer.getSalvoes().stream())
                .map(salvo -> salvo.makeSalvoDTO())
                .collect(Collectors.toList());
    }


}





