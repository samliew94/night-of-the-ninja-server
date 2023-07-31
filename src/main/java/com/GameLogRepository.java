package com;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameLogRepository extends JpaRepository<GameLog, Integer> {

    List<GameLog> findAllByOrderByGameLogIdDesc();

}
