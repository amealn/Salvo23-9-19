package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date finishDate;
    private double score;

    //Constructor
    public Score() {}
    public Score(Game game, Player player, double score, Date finishDate) {
        this.finishDate = finishDate;
        this.score = score;
        this.player = player;
        this.game = game;
    }
    //Getters y setters
    public long getId() {return id;}
    public void setId(long id) {
        this.id = id;
    }

    public Date getFinishDate() {return finishDate;}
    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public double getScore() {return score;}
    public void setScore(double score) {
        this.score = score;
    }

    //Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    public Player player;
    public Player getPlayer(){
        return player;
    }
    public void setPlayer(Player player) {this.player = player;}

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    public Game game;
    public Game getGame(){
        return game;
    }
    public void setGame(Game game) {this.game = game;}

    //DTO para /games
    public Map<String, Object> makeScoreDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", this.player.getId());
        dto.put("score",this.score);
        dto.put("finishDate", this.finishDate);
        return dto;
    }
}
