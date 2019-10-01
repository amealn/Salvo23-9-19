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
    public Date finishDate;
    public double score;

    public void setId(long id) {
        this.id = id;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public long getId() {
        return id;
    }
    public Date getFinishDate() {
        return finishDate;
    }
    public double getScore() {
        return score;
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

    public Score() {
    }
    public Score(Game game, Player player, double score, Date finishDate) {
        this.finishDate = finishDate;
        this.score = score;
        this.player = player;
        this.game = game;
    }

    //DTO para /games
    public Map<String, Object> makeScoreDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", getPlayer().getId());
        dto.put("score", getScore());
        dto.put("finishDate", getFinishDate());
        return dto;
    }







}
