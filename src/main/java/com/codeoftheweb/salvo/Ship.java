package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    public GamePlayer getGamePlayer(){
        return gamePlayer;
    }

    @OneToMany(mappedBy="ship", fetch=FetchType.EAGER)
    Set<ShipLocations> shipLocations;

    public Set<ShipLocations> getShipLocations() {
        return shipLocations;
    }

    public long getId() {
        return id;
    }
}
