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
    private PasswordEncoder passwordEncoder;

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

    public List<Map<String, Object>> getAllGames() {
        return gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList());
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
    public List<Map<String, Object>> getLeaderboard() {
        return playerRepository.findAll()
                .stream()
                .map(player -> player.makePlayerLeaderboardDTO())
                .collect(Collectors.toList());
    }


    //Game_view te odio
    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long gamePlayerId,
                                                           Authentication authentication) {
        GamePlayer gpActual = gamePlayerRepository.findById(gamePlayerId).get();
        Long loggedId = (getCurrentPlayer(authentication) == null ? 0 : getCurrentPlayer(authentication).getId());

        Map<String, Object> dto;
        if (validateLoggedGamePlayer(loggedId, gpActual)) {
            dto = makeGameViewDTO(gpActual);
            return ResponseEntity.ok(dto);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    //DTO para Game_view
    public Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        Map<String, Object> hits = new LinkedHashMap<String, Object>();

        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getCreationDate());
        //dto.put("gameState", );
        dto.put("gamePlayers", gamePlayer.getGame().getAllGamePlayers());
        if (getOpponent(gamePlayer) == null || gamePlayer.getShips().isEmpty())
            dto.put("ships", new ArrayList<>());
        else
            dto.put("ships", gamePlayer.getShips().stream().map(ship -> makeShipDTO(ship)));
        if (getOpponent(gamePlayer) == null || gamePlayer.getSalvoes().isEmpty())
            dto.put("salvoes", new ArrayList<>());
        else
            dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                    .stream()
                    .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes().stream().map(salvo -> makeSalvoDTO(salvo))));
        if (getOpponent(gamePlayer) == null)
            dto.put("hits", new ArrayList<>());
        else {
            if (getOpponent(gamePlayer).getShips().isEmpty() || getOpponent(gamePlayer).getSalvoes().isEmpty())
                dto.put("hits", new ArrayList<>());
            else
                dto.put("hits", hits);
            hits.put("self", getAllHits(gamePlayer));
            hits.put("opponent", getAllHits(getOpponent(gamePlayer)));
        }
        return dto;
    }


    //Dto para Game_view
    public Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getShipLocations());
        return dto;
    }


    //DTO para Game_view
    public Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    //Lista y DTO para Game_view --> turn-hitLocations-damages-missed
    public List<Map> getAllHits(GamePlayer gamePlayer) {

        List<Map> dto = new ArrayList<>();
        Integer carrierHitsTotal = 0;
        Integer battleshipHitsTotal = 0;
        Integer submarineHitsTotal = 0;
        Integer destroyerHitsTotal = 0;
        Integer patrolboatHitsTotal = 0;
        List<String> carrierLocations = new ArrayList<>();
        List<String> battleshipLocations = new ArrayList<>();
        List<String> submarineLocations = new ArrayList<>();
        List<String> destroyerLocations = new ArrayList<>();
        List<String> patrolboatLocations = new ArrayList<>();

        for (Ship ship : gamePlayer.getShips()) {
            switch (ship.getType()) {
                case "carrier":
                    carrierLocations = (List<String>) ship.getShipLocations();
                    break;
                case "battleship":
                    battleshipLocations = (List<String>) ship.getShipLocations();
                    break;
                case "submarine":
                    submarineLocations = (List<String>) ship.getShipLocations();
                    break;
                case "destroyer":
                    destroyerLocations = (List<String>) ship.getShipLocations();
                    break;
                case "patrolboat":
                    patrolboatLocations = (List<String>) ship.getShipLocations();
                    break;
            }
        }

        for (Salvo salvo : getOpponent(gamePlayer).getSalvoes()) {

            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;
            Integer missedShots = salvo.getSalvoLocations().size();
            Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
            Map<String, Object> damagesPerTurn = new LinkedHashMap<>();
            List<String> salvoLocationsList = new ArrayList<>();
            List<String> hitCellsList = new ArrayList<>();
            salvoLocationsList.addAll(salvo.getSalvoLocations());

            for (String salvoShot : salvoLocationsList) {
                if (carrierLocations.contains(salvoShot)) {
                    carrierHitsInTurn++;
                    carrierHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (battleshipLocations.contains(salvoShot)) {
                    battleshipHitsInTurn++;
                    battleshipHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (submarineLocations.contains(salvoShot)) {
                    submarineHitsInTurn++;
                    submarineHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (destroyerLocations.contains(salvoShot)) {
                    destroyerHitsInTurn++;
                    destroyerHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (patrolboatLocations.contains(salvoShot)) {
                    patrolboatHitsInTurn++;
                    patrolboatHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
            }
            //DTO para self y opponent
            hitsMapPerTurn.put("turn", salvo.getTurn());
            hitsMapPerTurn.put("hitLocations", hitCellsList);
            hitsMapPerTurn.put("damages", damagesPerTurn);
            hitsMapPerTurn.put("missed", missedShots);

            //DTO para DAMAGES
            damagesPerTurn.put("carrierHits", carrierHitsInTurn);
            damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
            damagesPerTurn.put("submarineHits", submarineHitsInTurn);
            damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);
            damagesPerTurn.put("carrier", carrierHitsTotal);
            damagesPerTurn.put("battleship", battleshipHitsTotal);
            damagesPerTurn.put("submarine", submarineHitsTotal);
            damagesPerTurn.put("destroyer", destroyerHitsTotal);
            damagesPerTurn.put("patrolboat", patrolboatHitsTotal);

            //Agrega el DTO para self y opponent al DTO retornado, que a su vez se hace una Lista para gameViewDTO
            dto.add(hitsMapPerTurn);
        }
        return dto;
    }

    /*/DTO para Game_view
    public Map<String, Object> makeHitsDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        //dto.put("turn", salvo.getTurn());
        dto.put("hitLocations", getAllHitLocations());
        dto.put("damages", "");
        dto.put("missed", "");
        return dto;
    }


    //Lista para game_view/n
    public List<String> getAllHitLocations() {
        List<String> salvoList = salvoRepository.findAll()
                .stream()
                .flatMap(salvo -> salvo.getSalvoLocations().stream())
                .collect(Collectors.toList());
        List<String> shipList = shipRepository.findAll()
                .stream()
                .flatMap(ship -> ship.getShipLocations().stream())
                .collect(Collectors.toList());
        shipList.retainAll(salvoList);
        return shipList;
    }*/


    //METODOS PARA MODULARIZAR
    //Metodo para obtener usuario logeado
    private Player getCurrentPlayer(Authentication authentication) {
        List<Player> listCurrentPlayer = new ArrayList<>();
        if (authentication != null) {
            listCurrentPlayer = playerRepository.findAll()
                    .stream()
                    .filter(player -> player.getUserName().equals(authentication.getName()))
                    .collect(Collectors.toList());
        }
        if (listCurrentPlayer.isEmpty()) {
            return null;
        } else {
            return listCurrentPlayer.get(0);
        }
    }

    //Validar que el usuario logeado sea el gameplayer del juego
    private Boolean validateLoggedGamePlayer(Long loggedId, GamePlayer gamePlayer) {
        return gamePlayer.getPlayer().getId() == loggedId;
    }

    //Metodo para mostrar mensaje junto a un HTTPStatus
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    public GamePlayer getOpponent(GamePlayer gpSelf) {
        return gpSelf.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gpSelf.getId()).findAny().orElse(null);
    }

    /*private Map<String, Object> emptyHits() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self", new ArrayList<>());
        dto.put("opponent", new ArrayList<>());
        return dto;
    }*/

}
