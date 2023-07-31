package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface SceneTileRepository extends JpaRepository<SceneTile, Integer>{
	
	List<SceneTile> findAllByOrderBySceneTileIdAsc();
	List<SceneTile> findAllByIsReplacedOrderBySceneTileIdAsc(boolean isReplaced);
	List<SceneTile> findAllBySceneOptionGreaterThan(int i);

	SceneTile findByScenePos(int randomedSceneId);
	SceneTile findFirstBySceneOptionOrderBySceneTileIdAsc(int i);
	
}