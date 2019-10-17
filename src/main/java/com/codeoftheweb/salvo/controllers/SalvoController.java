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

    private List<Map<String, Object>> getAllGames() {
        return gameRepository.findAll()
                .stream()
                .map(Game::makeGameDTO)
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
            Game newGame = gameRepository.save(new Game(newDate));
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
            Game currentGame = gameRepository.findById(gameId).orElse(null);
            if (currentGame == null) {
                return new ResponseEntity<>("No such game", HttpStatus.FORBIDDEN);
            } else {
                if (currentGame.getGamePlayers().stream().filter(player -> player.getId() > 0).count() > 1) {
                    return new ResponseEntity<>("Game is full", HttpStatus.FORBIDDEN);
                } else {
                    List<Long> playersId = currentGame.getPlayers().stream().map(Player::getId).collect(Collectors.toList());
                    Long AuthId = (creatorPlayer == null ? 0 : creatorPlayer.getId());
                    if (playersId.get(0).equals(AuthId)) {
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
    public ResponseEntity<Object> createPlayer(@RequestParam("email") String email,
                                               @RequestParam("password") String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Enter all data"), HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Name already used"), HttpStatus.FORBIDDEN);
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
        GamePlayer gpActual = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (loggedPlayer == null) {
            return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
        } else if (gpActual == null) {
            return new ResponseEntity<>("Gameplayer doesn't exist", HttpStatus.UNAUTHORIZED);
        } else if (gpActual.getPlayer().getId() != loggedPlayer.getId()) {
            return new ResponseEntity<>("Incorrect game", HttpStatus.UNAUTHORIZED);
        } else {
            if (gpActual.getShips().isEmpty()) {
                ships.forEach(ship -> ship.setGamePlayer(gpActual));
                shipRepository.saveAll(ships);
                return new ResponseEntity<>(makeMap("OK", "Ships placed"), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(makeMap("error", "Player already has ships"), HttpStatus.FORBIDDEN);
            }
        }
    }

    //Create salvoes
    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> addSalvoes(@PathVariable Long gamePlayerId,
                                             Authentication authentication,
                                             @RequestBody Salvo salvo) {
        Player loggedPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer gpActual = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (loggedPlayer == null) {
            return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
        }
        if (gpActual == null) {
            return new ResponseEntity<>("Gameplayer doesn't exist", HttpStatus.UNAUTHORIZED);
        }
        if (gpActual.getPlayer().getId() != loggedPlayer.getId()) {
            return new ResponseEntity<>("Wrong gameplayer", HttpStatus.UNAUTHORIZED);
        }
        Set<Salvo> salvoes = gpActual.getSalvoes();
        for (Salvo salvo1 : salvoes) {
            if (salvo.getTurn() == salvo1.getTurn()) {
                return new ResponseEntity<>("Your salvoes are already placed", HttpStatus.FORBIDDEN);
            }
        }
        salvo.setGamePlayer(gpActual);
        salvoRepository.save(new Salvo(salvoes.size() + 1, gpActual, salvo.getSalvoLocations()));
        return new ResponseEntity<>(makeMap("OK", "Salvoes created"), HttpStatus.CREATED);
    }


    //Leaderboard
    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> getLeaderboard() {
        return playerRepository.findAll()
                .stream()
                .map(Player::makePlayerLeaderboardDTO)
                .collect(Collectors.toList());
    }

    //Game_view te odio, morite
    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long gamePlayerId,
                                                           Authentication authentication) {
        GamePlayer gpActual = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        Player loggedPlayer = playerRepository.findByUserName(authentication.getName());

        Map<String, Object> dto;
        if (gpActual == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else if (loggedPlayer == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            dto = makeGameViewDTO(gpActual);
            return ResponseEntity.ok(dto);
        }
    }

    //DTO para Game_view
    private Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        Map<String, Object> hits = new LinkedHashMap<String, Object>();

        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getCreationDate());
        dto.put("gameState", getGameState(gamePlayer));
        dto.put("gamePlayers", gamePlayer.getGame().getAllGamePlayers());
        dto.put("ships", gamePlayer.getShips().stream().map(this::makeShipDTO).collect(Collectors.toList()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes().stream().map(this::makeSalvoDTO)).collect(Collectors.toList()));
        dto.put("hits", hits);
        if (getOpponent(gamePlayer) != null) {
            hits.put("self", getAllHits(getOpponent(gamePlayer)));
            hits.put("opponent", getAllHits(gamePlayer));
        } else {
            hits.put("self", new ArrayList<>());
            hits.put("opponent", new ArrayList<>());
        }
        return dto;
    }

    //Dto para Game_view -->ships
    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    //DTO para Game_view --> salvoes
    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    //Lista y DTO para Game_view --> turn-hitLocations-damages-missed
    private List<Map<String, Object>> getAllHits(GamePlayer gamePlayer) {
        List<Map<String, Object>> dto = new ArrayList<>();

        //Acumuladores para el TOTAL de hits a un barco particular
        int carrierHitsTotal = 0;
        int battleshipHitsTotal = 0;
        int submarineHitsTotal = 0;
        int destroyerHitsTotal = 0;
        int patrolboatHitsTotal = 0;

        List<Salvo> salvoes = orderSalvoes(gamePlayer.getSalvoes());

        //Loop de SALVOES del PLAYER y los SHIPS del OPPONENT
        for (Salvo salvo : salvoes) {

            List<String> hitLocations = new ArrayList<>();

            //Acumuladores que indican los salvo hits que se hacen a cada barco EN CADA TURNO
            int carrierHitsInTurn = 0;
            int battleshipHitsInTurn = 0;
            int submarineHitsInTurn = 0;
            int destroyerHitsInTurn = 0;
            int patrolboatHitsInTurn = 0;

            for (Ship ship : getOpponent(gamePlayer).getShips()) {
                List<String> hits = new ArrayList<>(salvo.getSalvoLocations());
                hits.retainAll(ship.getLocations());
                int shots = hits.size();
                if (shots != 0) {
                    hitLocations.addAll(hits);
                    switch (ship.getType()) {
                        case "carrier":
                            carrierHitsInTurn += shots;
                            carrierHitsTotal += shots;
                            break;
                        case "battleship":
                            battleshipHitsInTurn += shots;
                            battleshipHitsTotal += shots;
                            break;
                        case "submarine":
                            submarineHitsInTurn += shots;
                            submarineHitsTotal += shots;
                            break;
                        case "destroyer":
                            destroyerHitsInTurn += shots;
                            destroyerHitsTotal += shots;
                            break;
                        case "patrolboat":
                            patrolboatHitsInTurn += shots;
                            patrolboatHitsTotal += shots;
                            break;
                    }
                }
            }

            Map<String, Object> hitsDTO = new LinkedHashMap<>();
            Map<String, Object> damagesDTO = new LinkedHashMap<>();

            //DTO para HITS ---> SELF y OPPONENT
            hitsDTO.put("turn", salvo.getTurn());
            hitsDTO.put("hitLocations", hitLocations);
            hitsDTO.put("damages", damagesDTO);
            hitsDTO.put("missed", salvo.getSalvoLocations().size() - hitLocations.size());

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

    //GAMESTATE
    private String getGameState(GamePlayer gpActual) {
        if (gpActual.getShips().isEmpty()) {
            return "PLACESHIPS";
        }
        if (getOpponent(gpActual) == null) {
            return "WAITINGFOROPP";
        }
        if (getOpponent(gpActual).getShips().size() == 0) {
            return "WAIT";
        }
        if ((gpActual.getSalvoes().size() <= getOpponent(gpActual).getSalvoes().size()) &&
                (getSunkShips(gpActual) < 17 && getSunkShips(getOpponent(gpActual)) < 17)) {
            return "PLAY";
        }
        if (gpActual.getSalvoes().size() > getOpponent(gpActual).getSalvoes().size()) {
            return "WAIT";
        }

        Date date = new Date();

        if (getSunkShips(gpActual) < 17 && getSunkShips(getOpponent(gpActual)) == 17) {
            Score newScore = new Score(gpActual.getGame(), gpActual.getPlayer(), 1, date);
            if (!existsScore(newScore, gpActual.getGame())) {
                scoreRepository.save(newScore);
            }
            return "WON";
        } else if (getSunkShips(gpActual) == 17 && getSunkShips(getOpponent(gpActual)) < 17) {
            Score newScore = new Score(gpActual.getGame(), gpActual.getPlayer(), 0, date);
            if (!existsScore(newScore, gpActual.getGame())) {
                scoreRepository.save(newScore);
            }
            return "LOST";
        } else if (getSunkShips(gpActual) == 17 && getSunkShips(getOpponent(gpActual)) == 17) {
            Score newScore = new Score(gpActual.getGame(), gpActual.getPlayer(), 0.5, date);
            if (!existsScore(newScore, gpActual.getGame())) {
                scoreRepository.save(newScore);
            }
            return "TIE";
        } else
            return "WAIT";
    }

    //METODOS PARA MODULARIZAR

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
    private GamePlayer getOpponent(GamePlayer gamePlayer) {
        return gamePlayer.getGame().getGamePlayers()
                .stream()
                .filter(gamePlayerInStream -> gamePlayerInStream.getId() != gamePlayer.getId())
                .findAny().orElse(null);
    }

    //Metodo para indicar numero de ships HUNDIDOS para verificar GAME OVER
    private int getSunkShips(GamePlayer gp) {
        GamePlayer opponent = getOpponent(gp);
        List<String> ships = new ArrayList<>();
        List<String> salvoes = new ArrayList<>();
        for (Ship ship : gp.getShips()) {
            ships.addAll(ship.getLocations());
        }
        for (Salvo salvo : opponent.getSalvoes()) {
            salvoes.addAll(salvo.getSalvoLocations());
        }
        ships.retainAll(salvoes);
        return ships.size();
    }

    //Metodo para ordenar salvoes
    private List<Salvo> orderSalvoes(Set<Salvo> salvoes) {
        return salvoes.stream().sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList());
    }

    //Metodo para verificar si existen scores de un player en un game
    private Boolean existsScore(Score score, Game game) {

        Set<Score> scores = game.getScores();
        for (Score score1 : scores) {
            if (score.getPlayer().getUserName().equals(score1.getPlayer().getUserName()))
                return true;
        }
        return false;
    }

}
