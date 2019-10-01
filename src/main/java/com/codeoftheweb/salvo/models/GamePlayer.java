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
    public Date joinDate;

    public void setId(long id) {
        this.id = id;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }

    public GamePlayer(Date joinDate){
        this.joinDate = joinDate;
    }

    public Date getJoinDate(){
    return joinDate;
}

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    public Player player;

    public Player getPlayer(){
        return player;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    public Game game;


    public Game getGame(){
        return game;
    }

    public GamePlayer() { }

    public GamePlayer(Date joinDate,Player player, Game game) {
        this.joinDate=joinDate;
        this.player = player;
        this.game = game;
    }



    public long getId() {
        return id;
    }

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    Set<Ship> ships;

    public Set<Ship> getShips() {
        return ships;
    }

    //DTO para /games
    public Map<String, Object> makeGamePlayersDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("player", getPlayer().makePlayerDTO());
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

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    Set<Salvo> salvoes;

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }


}
