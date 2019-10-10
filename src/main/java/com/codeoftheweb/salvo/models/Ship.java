package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String type;

    //Constructor
    public Ship() {}
    public Ship(GamePlayer gamePlayer, String type, Set<String> shipLocations) {
        this.gamePlayer = gamePlayer;
        this.type=type;
        this.shipLocations = shipLocations;
    }

    //Getters y setters
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    //Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    public GamePlayer gamePlayer;
    public GamePlayer getGamePlayer(){return gamePlayer;}
    public void setGamePlayer(GamePlayer gamePlayer) {this.gamePlayer = gamePlayer;}

    @ElementCollection
    @Column(name="shipLocations")
    private Set<String> shipLocations = new LinkedHashSet<>();
    public Set<String> getShipLocations() {return shipLocations;}
    public void setShipLocations(Set<String> shipLocations) {this.shipLocations = shipLocations;}

}
