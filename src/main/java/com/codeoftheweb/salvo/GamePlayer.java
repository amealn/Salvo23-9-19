package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date joinDate;

    public GamePlayer(Date joinDate){
        this.joinDate = joinDate;
    }

    public Date getJoinDate(){
    return joinDate;
}

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    public Player getPlayer(){
        return player;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;


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
    Set<Ship> ship;

    public Set<Ship> getShip() {
        return ship;
    }
}
