package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    public long id;
    public int turn;

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

    public Map<String, Object> makeSalvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", this.getTurn());
        dto.put("player", makePlayer2DTO());
        return dto;
    }
    public Map<String, Object> makePlayer2DTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", getGamePlayer().getPlayer().getId());
        dto.put("locations", getSalvoLocations());
        return dto;
    }


}
