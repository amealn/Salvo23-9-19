package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date creationDate;

    //Constructor
    public Game() {}

    public Game(Date creationDate){
        this.creationDate = creationDate;
    }
    //Getters y setters
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Date getCreationDate(){return creationDate;}
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    //Relationships
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        gamePlayers.add(gamePlayer);
    }
    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<Score> scores;

    public Set<Score> getScores() {
        return scores;
    }
    public void addScore(Score score) {

        scores.add(score);
    }
    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    //DTO para /games
    public Map<String, Object> makeGameDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("created", this.getCreationDate());
        dto.put("gamePlayers", getAllGamePlayers());
        dto.put("scores", getAllScores());
        return dto;
    }

    //List para /games
    public List<Map<String, Object>> getAllGamePlayers() {
        return gamePlayers
                .stream()
                .map(gamePlayer -> gamePlayer.makeGamePlayersDTO())
                .collect(Collectors.toList());
    }

    //List para /games
    public List<Map<String, Object>> getAllScores() {
        return this.scores
                .stream()
                .map(score -> score.makeScoreDTO())
                .collect(Collectors.toList());}

    public List<Player> getPlayers() {
        return gamePlayers.stream().map(player -> player.getPlayer()).collect(Collectors.toList());
    }

}