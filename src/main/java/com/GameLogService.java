package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameLogService {

    @Autowired
    GameLogRepository gameLogRepository;

    @Autowired
    MyUserRepository userRepository;

    public void reset() {

        gameLogRepository.deleteAll();

    }

    public void add(String log) {

        GameLog gameLog = new GameLog();
        gameLog.setCreatedBy(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
        gameLog.setLog(log);
        gameLogRepository.save(gameLog);

    }


    public List<String> findAllLogOrderByGameLogIdDesc() {
        return gameLogRepository.findAllByOrderByGameLogIdDesc().stream().map(x -> x.getLog()).collect(Collectors.toList());
    }

    public String findLastLog() {
        return findAllLogOrderByGameLogIdDesc().get(0);
    }

}
