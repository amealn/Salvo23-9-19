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
    public String type;

    public void setId(long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setShipLocations(Set<String> shipLocations) {
        this.shipLocations = shipLocations;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    public GamePlayer gamePlayer;

    public GamePlayer getGamePlayer(){
        return gamePlayer;
    }

    @ElementCollection
    @Column(name="shipLocations")
    private Set<String> shipLocations = new LinkedHashSet<>();

    public Set<String> getShipLocations() {
        return shipLocations;
    }

    public long getId() {
        return id;
    }

    public Ship() {
    }

    public Ship(GamePlayer gamePlayer, String type, Set<String> shipLocation) {
        this.gamePlayer = gamePlayer;
        this.type=type;
        this.shipLocations = shipLocation;
    }

    public Ship(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    //Dto para game_view/n
    public Map<String, Object> makeShipDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", this.getType());
        dto.put("shipLocations", this.getShipLocations());
        return dto;
    }





}