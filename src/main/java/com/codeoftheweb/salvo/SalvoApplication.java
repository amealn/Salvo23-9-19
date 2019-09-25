package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);}

		@Bean
		public CommandLineRunner initData(PlayerRepository playerRepository,
										  GameRepository gameRepository,
										  GamePlayerRepository gamePlayerRepository,
										  ShipRepository shipRepository){
			return(args) ->{

				Player p1 = new Player("j.bauer@ctu.gov");
				Player p2 = new Player("c.obrian@ctu.gov");
				Player p3 = new Player("kim_bauer@gmail.com");
				Player p4 = new Player("t.almeida@ctu.gov");
				Player p5 = new Player("d.palmer@whitehouse.gov");
				playerRepository.saveAll(Arrays.asList(p1,p2,p3,p4,p5));

                Date date = new Date();

                Game g1 = new Game(date);
                Game g2 = new Game(Date.from(date.toInstant().plusSeconds(3600)));
                Game g3 = new Game(Date.from(date.toInstant().plusSeconds(7200)));
				Game g4 = new Game(Date.from(date.toInstant().plusSeconds(10800)));
				Game g5 = new Game(Date.from(date.toInstant().plusSeconds(14400)));
				Game g6 = new Game(Date.from(date.toInstant().plusSeconds(18000)));
				Game g7 = new Game(Date.from(date.toInstant().plusSeconds(21600)));
				Game g8 = new Game(Date.from(date.toInstant().plusSeconds(25200)));
				gameRepository.saveAll(Arrays.asList(g1, g2, g3,g4,g5,g6,g7,g8));

                GamePlayer gp1= new GamePlayer(date,p1,g1);
				GamePlayer gp2= new GamePlayer(date,p2,g1);
				GamePlayer gp3= new GamePlayer(date,p1,g2);
				GamePlayer gp4= new GamePlayer(date,p2,g2);
				GamePlayer gp5= new GamePlayer(date,p2,g3);
				GamePlayer gp6= new GamePlayer(date,p4,g3);
				GamePlayer gp7= new GamePlayer(date,p2,g4);
				GamePlayer gp8= new GamePlayer(date,p1,g4);
				GamePlayer gp9= new GamePlayer(date,p4,g5);
				GamePlayer gp10= new GamePlayer(date,p1,g5);
				GamePlayer gp11= new GamePlayer(date,p3,g6);
				GamePlayer gp12= new GamePlayer(date,p4,g7);
				GamePlayer gp13= new GamePlayer(date,p3,g8);
				GamePlayer gp14= new GamePlayer(date,p4,g8);
				gamePlayerRepository.saveAll(Arrays.asList(gp1, gp2, gp3,gp4,gp5,gp6,gp7,gp8,gp9,gp10,gp11,gp12,gp13,gp14));

				Ship s1=new Ship(gp1,"Destroyer", Arrays.asList("H2", "H3", "H4"));
				Ship s2=new Ship(gp1,"Submarine", Arrays.asList("E1", "F1", "G1"));
				Ship s3=new Ship(gp1,"Patrol Boat", Arrays.asList("B4", "B5"));
				Ship s4=new Ship(gp2,"Destroyer", Arrays.asList("B5", "C5", "D5"));
				Ship s5=new Ship(gp2,"Patrol Boat", Arrays.asList("F1", "F2"));
				shipRepository.saveAll(Arrays.asList(s1,s2,s3,s4,s5));

			};


		}

    }






