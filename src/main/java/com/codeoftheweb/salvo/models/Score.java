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
    public long id;
    public Date finishDate;
    public double score;

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

    public Map<String, Object> makeScoreDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", getPlayer().getId());
        dto.put("scores", getScore());
        return dto;
    }







}
