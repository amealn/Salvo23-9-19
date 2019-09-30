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
    public Date creationDate;


    public Game() {}

    public Game(Date creationDate){
            this.creationDate = creationDate;
        }

    public long getId() {
        return id;
    }

    public Date getCreationDate(){return creationDate;}

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {

        gamePlayers.add(gamePlayer);
    }
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<Score> scores;

    public Set<Score> getScores() {
        return scores;
    }

    public Map<String, Object> makeGameDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("created", this.getCreationDate());
        dto.put("gamePlayers", getAllGamePlayers(getGamePlayers()));
        dto.put("score", getAllScores());
        return dto;
    }

    public List<Map<String, Object>> getAllGamePlayers(Set<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                .map(gamePlayer -> gamePlayer.makeGamePlayersDTO())
                .collect(Collectors.toList());
    }

    public List<Object> getAllSalvoes(){
        return this.getGamePlayers()
                .stream()
                .flatMap(gamePlayer -> gamePlayer.getSalvoes().stream())
                .map(salvo -> salvo.makeSalvoDTO())
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllScores() {
        if(!scores.isEmpty()){
        return this.scores
                .stream()
                .map(score -> score.makeScoreDTO())
                .collect(Collectors.toList());}
        else {return null;}
    }


}
