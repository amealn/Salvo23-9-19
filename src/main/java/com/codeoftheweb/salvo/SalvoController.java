package com.codeoftheweb.salvo;

import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

        @Autowired
        private GameRepository gameRepository;

        @RequestMapping("/games")
        public List<Long> getAllGames() {
            return gameRepository.findAll()
                                 .stream()
                                 .map(game -> game.getId())
                                 .collect(Collectors.toList());
        }
        public Map<String, Object> makeGameDTO(Game game) {
                Map<String, Object> dto = new LinkedHashMap<String, Object>();
                dto.put("Id", getAllGames());
                dto.put("Creation Name", game.getCreationDate());
                return dto;
        }

}
