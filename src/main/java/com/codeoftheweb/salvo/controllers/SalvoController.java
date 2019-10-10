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

    //Create games
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

    //Join games
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

    //Create players
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

    //Create ships
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
            return new ResponseEntity<>(makeMap("OK", "Ships created"), HttpStatus.CREATED);
        }
    }

    //Create salvoes
    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> addSalvoes(@PathVariable Long gamePlayerId,
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
                    return new ResponseEntity<>(makeMap("OK", "Salvoes created"), HttpStatus.CREATED);
                }
            }
        }
    }

    //Leaderboard
    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> getLeaderboard() {
        return playerRepository.findAll()
                .stream()
                .map(player -> player.makePlayerLeaderboardDTO())
                .collect(Collectors.toList());
    }

    //Game_view te odio, morite
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
        dto.put("gameState", "PLACESHIPS");
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
        dto.put("hits", hits);
        if (getOpponent(gamePlayer) == null || getOpponent(gamePlayer).getShips().isEmpty() || getOpponent(gamePlayer).getSalvoes().isEmpty()){
            hits.put("self", new ArrayList<>());
            hits.put("opponent", new ArrayList<>());   }
        else{
        hits.put("self", getAllHits(gamePlayer));
        hits.put("opponent", getAllHits(getOpponent(gamePlayer)));
    }
        return dto;
}

    //Dto para Game_view -->ships
    public Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getShipLocations());
        return dto;
    }

    //DTO para Game_view --> salvoes
    public Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    //Lista y DTO para Game_view --> turn-hitLocations-damages-missed
    public Set<Map> getAllHits(GamePlayer gamePlayer) {
        Set<Map> dto = new HashSet<>();

        //Acumuladores para el total de hits a un barco particular
        Integer carrierHitsTotal = 0;
        Integer battleshipHitsTotal = 0;
        Integer submarineHitsTotal = 0;
        Integer destroyerHitsTotal = 0;
        Integer patrolboatHitsTotal = 0;

        //Listas que indican donde estan cada tipo de barco para player y oponente
        Set<String> carrierLocations = new HashSet<>();
        Set<String> battleshipLocations = new HashSet<>();
        Set<String> submarineLocations = new HashSet<>();
        Set<String> destroyerLocations = new HashSet<>();
        Set<String> patrolboatLocations = new HashSet<>();

        //Loop de los SHIPS del PLAYER junto a sus locations y tipo de ship
        for (Ship ship : gamePlayer.getShips()) {
            switch (ship.getType()) {
                case "Carrier":
                    carrierLocations = ship.getShipLocations();
                    break;
                case "Battleship":
                    battleshipLocations = ship.getShipLocations();
                    break;
                case "Submarine":
                    submarineLocations = ship.getShipLocations();
                    break;
                case "Destroyer":
                    destroyerLocations = ship.getShipLocations();
                    break;
                case "Patrol Boat":
                    patrolboatLocations = ship.getShipLocations();
                    break;
            }
        }

        //Loop de los SALVOES del OPPONENT
        for (Salvo salvo : getOpponent(gamePlayer).getSalvoes()) {

            //Acumuladores que indican los salvo hits que se hacen a cada barco en cada turno
            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;

            //Contador que agarra el tamanio total de SalvoLocations y le va restando
            Integer missedSalvoes = salvo.getSalvoLocations().size();

            Set<String> salvoLocationsSet = new HashSet<>(salvo.getSalvoLocations());

            //Lista de hitLocations
            Set<String> hitLocation = new HashSet<>();

            //Loop para relacionar la lista de ships del PLAYER con los salvos del OPPONENT
            for (String salvoShot : salvoLocationsSet) {
                if (carrierLocations.contains(salvoShot)) {
                    carrierHitsInTurn++;
                    carrierHitsTotal++;
                    hitLocation.add(salvoShot);
                    missedSalvoes--;
                }
                if (battleshipLocations.contains(salvoShot)) {
                    battleshipHitsInTurn++;
                    battleshipHitsTotal++;
                    hitLocation.add(salvoShot);
                    missedSalvoes--;
                }
                if (submarineLocations.contains(salvoShot)) {
                    submarineHitsInTurn++;
                    submarineHitsTotal++;
                    hitLocation.add(salvoShot);
                    missedSalvoes--;
                }
                if (destroyerLocations.contains(salvoShot)) {
                    destroyerHitsInTurn++;
                    destroyerHitsTotal++;
                    hitLocation.add(salvoShot);
                    missedSalvoes--;
                }
                if (patrolboatLocations.contains(salvoShot)) {
                    patrolboatHitsInTurn++;
                    patrolboatHitsTotal++;
                    hitLocation.add(salvoShot);
                    missedSalvoes--;
                }
            }
            Map<String, Object> hitsDTO = new LinkedHashMap<>();
            Map<String, Object> damagesDTO = new LinkedHashMap<>();

            //DTO para self y opponent
            hitsDTO.put("turn", salvo.getTurn());
            hitsDTO.put("hitLocations", hitLocation);
            hitsDTO.put("damages", damagesDTO);
            hitsDTO.put("missed", missedSalvoes);

            //DTO para DAMAGES
            //Hits a los barcos que se van haciendo en diferentes turnos
            damagesDTO.put("carrierHits", carrierHitsInTurn);
            damagesDTO.put("battleshipHits", battleshipHitsInTurn);
            damagesDTO.put("submarineHits", submarineHitsInTurn);
            damagesDTO.put("destroyerHits", destroyerHitsInTurn);
            damagesDTO.put("patrolboatHits", patrolboatHitsInTurn);

            //Total de Hits a los barcos
            damagesDTO.put("carrier", carrierHitsTotal);
            damagesDTO.put("battleship", battleshipHitsTotal);
            damagesDTO.put("submarine", submarineHitsTotal);
            damagesDTO.put("destroyer", destroyerHitsTotal);
            damagesDTO.put("patrolboat", patrolboatHitsTotal);

            //Agrega el DTO para self y opponent al DTO retornado, que a su vez se hace una Lista para gameViewDTO
            dto.add(hitsDTO);
        }
        return dto;
    }

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

    //Metodo para indicar que alguien no esta loggeado y es un GUEST
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    //Metodo para indicar el oponente del PLAYER en un game en particular
    public GamePlayer getOpponent(GamePlayer gpSelf) {
        return gpSelf.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gpSelf.getId()).findAny().orElse(null);
    }

}
