package com.codeoftheweb.salvo.models;

import com.codeoftheweb.salvo.repositories.GameRepository;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String userName;
    private String password;

    //Constructor
    public Player() {
    }

    public Player(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    //Getters y setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //Relationships
    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayers.add(gamePlayer);
    }

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<Score> scores;

    public Set<Score> getScores() {
        return scores;
    }
    public void addScore(Score score) {scores.add(score);}
    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    //DTO para /games
    public Map<String, Object> makePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        return dto;
    }

    //DTO para /leaderBoard
    public Map<String, Object> makePlayerLeaderboardDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        Map<String, Object> score = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        dto.put("score", score);
        score.put("total", this.getTotalScore());
        score.put("won", this.getWinScore());
        score.put("lost", this.getLostScore());
        score.put("tied", this.getTiedScore());
        return dto;
    }

    public Double getTotalScore() {
        return this.getWinScore() * 1.0D + this.getTiedScore() * 0.5D;
    }

    public long getWinScore() {
        return this.getScores().stream().filter(score -> score.getScore() == 1.0D).count();
    }

    public long getLostScore() {
        return this.getScores().stream().filter(score -> score.getScore() == 0.0D).count();
    }

    public long getTiedScore() {
        return this.getScores().stream().filter(score -> score.getScore() == 0.5D).count();
    }


    /*public List<Map<String, Object>> getSelf(){
        return gamePlayers.stream().filter(gpself -> gpself.(authentication.getName()))*/
}



