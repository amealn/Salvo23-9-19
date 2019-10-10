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
    private int turn;

    //Constructor
    public Salvo() {
    }
    public Salvo(int turn, GamePlayer gamePlayer, Set<String> salvoLocations) {
        this.turn = turn;
        this.gamePlayer = gamePlayer;
        this.salvoLocations = salvoLocations;
    }
    //Getters y setters
    public long getId() {return id;}
    public void setId(long id) {
        this.id = id;
    }

    public int getTurn() {return turn;}
    public void setTurn(int turn) {
        this.turn = turn;
    }

    //Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    public GamePlayer gamePlayer;
    public GamePlayer getGamePlayer(){
        return gamePlayer;
    }
    public void setGamePlayer(GamePlayer gamePlayer) {this.gamePlayer = gamePlayer;}

    @ElementCollection
    @Column(name="salvoLocations")
    private Set<String> salvoLocations = new LinkedHashSet<>();
    public Set<String> getSalvoLocations() {
        return salvoLocations;
    }
    public void setSalvoLocations(Set<String> salvoLocations) {this.salvoLocations = salvoLocations;}

}
