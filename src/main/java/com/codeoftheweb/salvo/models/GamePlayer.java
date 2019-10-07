package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

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

    /*public GamePlayer(Date joinDate){
        this.joinDate = joinDate;
    }*/
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

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    Set<Salvo> salvoes;

    public Set<Salvo> getSalvoes() {return salvoes;}
    public void setSalvoes(Set<Salvo> salvoes) {this.salvoes = salvoes;}

    //DTO para /games
    public Map<String, Object> makeGamePlayersDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gpid", this.getId());
        dto.put("id", getPlayer().getId());
        dto.put("name", getPlayer().getUserName());
        return dto;
    }
    //DTO para Game_view/n
    public Map<String, Object> makeGame2DTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.game.getId());
        dto.put("creationDate", this.game.getCreationDate());
        dto.put("gamePlayers", this.game.getAllGamePlayers(this.game.getGamePlayers()));
        dto.put("ships", getAllShips());
        dto.put("salvoes", getGame().getAllSalvoes());
        return dto;
    }
    //Lista para game_view/n
    public List<Map<String, Object>> getAllShips() {
        return ships
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(Collectors.toList());
    }



}

