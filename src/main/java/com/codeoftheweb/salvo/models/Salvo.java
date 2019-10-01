package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    public int turn;

    public void setId(long id) {
        this.id = id;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setSalvoLocations(Set<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    public GamePlayer gamePlayer;

    public GamePlayer getGamePlayer(){
        return gamePlayer;
    }

    public long getId() {
        return id;
    }

    public int getTurn() {
        return turn;
    }

    @ElementCollection
    @Column(name="salvoLocations")
    private Set<String> salvoLocations = new LinkedHashSet<>();

    public Set<String> getSalvoLocations() {
        return salvoLocations;
    }

    public Salvo() {
    }

    public Salvo(int turn, GamePlayer gamePlayer, Set<String> salvoLocations) {
        this.turn = turn;
        this.gamePlayer = gamePlayer;
        this.salvoLocations = salvoLocations;
    }
    //DTO para Game_view/n
    public Map<String, Object> makeSalvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", this.getTurn());
        dto.put("player", this.getGamePlayer().getPlayer().getId());
        dto.put("salvoLocations", getSalvoLocations());
        return dto;
    }



}
