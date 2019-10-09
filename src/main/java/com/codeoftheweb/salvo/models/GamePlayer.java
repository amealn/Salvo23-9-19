package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.Authentication;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date joinDate;

    //Constructor
    public GamePlayer() { }

    public GamePlayer(Date joinDate,Player player, Game game) {
        this.joinDate=joinDate;
        this.player = player;
        this.game = game;
    }
    //Getters y setters
    public long getId() {return id;}
    public void setId(long id) {
        this.id = id;
    }

    public Date getJoinDate(){return joinDate;}
    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    //Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    public Player player;

    public Player getPlayer(){return player;}
    public void setPlayer(Player player) {this.player = player;}

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    public Game game;

    public Game getGame(){return game;}
    public void setGame(Game game) {this.game = game;}

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    Set<Ship> ships;

    public Set<Ship> getShips() {return ships;}
    public void setShips(Set<Ship> ships) {this.ships = ships;}
    public void addShip(Ship ship) {
        ships.add(ship);
    }


    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    Set<Salvo> salvoes;

    public Set<Salvo> getSalvoes() {return salvoes;}
    public void setSalvoes(Set<Salvo> salvoes) {this.salvoes = salvoes;}
    public void addSalvo(Salvo salvo) {
        salvoes.add(salvo);
    }

    //DTO para /games
    public Map<String, Object> makeGamePlayersDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("player", this.getPlayer().makePlayerDTO());
        dto.put("joinDate", this.getJoinDate());
        return dto;
    }
    //DTO para Game_view/n
    public Map<String, Object> makeGameViewDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.game.getId());
        dto.put("created", this.game.getCreationDate());
        //dto.put("gameState", );
        dto.put("gamePlayers", this.game.getAllGamePlayers());
        dto.put("ships", getAllShips());
        dto.put("salvoes", getGame().getAllSalvoes());
        dto.put("hits", this.makeHitsDTO());
        return dto;
    }
    //Lista para game_view/n
    public List<Map<String, Object>> getAllShips() {
        return ships
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(Collectors.toList());
    }
    public Map<String, Object> makeHitsDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("self", getAllSelf());
        dto.put("opponent", getAllOpponent());
        return dto;
    }
    public List<Map<String, Object>> getAllSelf() {
        return ships
                .stream()
                .map(ship -> ship.makeSelfDTO())
                .collect(Collectors.toList());
    }
    public List<Map<String, Object>> getAllOpponent() {
        return ships
                .stream()
                .map(ship -> ship.makeOpponentDTO())
                .collect(Collectors.toList());
    }
    public Map<String, Object> makeSelfDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", getSalvoes().getTurn());
        dto.put("hitLocations", getAllHitLocations());
        dto.put("damages", );
        dto.put("missed", );
        return dto;
    }
    public Map<String, Object> makeOpponentDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", getSalvoes().getTurn());
        dto.put("hitLocations", getAllHitLocations());
        dto.put("damages", );
        dto.put("missed", );
        return dto;
    }

    public Score getScore() {
        return player.getScore(game);
    }


}


