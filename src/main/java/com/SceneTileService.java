package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
class SceneTileService {

	@Autowired
	SceneTileRepository sceneTileRepository;

	@Autowired
	TimerService timerService;

	@Autowired
	GameProgressService gameProgressService;

	Random random = new SecureRandom();

	/**
	 * 3 rounds in total;
	 */
	private int round = 0;

	public void reset() {

		round = 0;
		sceneTileRepository.deleteAll();

		// round 1
		sceneTileRepository.save(SceneTile.builder().sceneType(0).scenePos(0).sceneOption(-1).build());
		sceneTileRepository.save(SceneTile.builder().sceneType(1).scenePos(randomScene(1, 4)).sceneOption(-1).build());
		sceneTileRepository.save(SceneTile.builder().sceneType(2).scenePos(randomScene()).sceneOption(-1).build());
		sceneTileRepository.save(SceneTile.builder().sceneType(2).scenePos(randomScene()).sceneOption(-1).build());
		sceneTileRepository.save(SceneTile.builder().sceneType(2).scenePos(randomScene()).sceneOption(-1).build());
		sceneTileRepository.save(SceneTile.builder().sceneType(2).scenePos(randomScene()).sceneOption(-1).build());

		// round 2
		sceneTileRepository.save(SceneTile.builder().sceneType(2).scenePos(randomScene()).sceneOption(-1).build());

		// round 3
		sceneTileRepository.save(SceneTile.builder().sceneType(2).scenePos(randomScene()).sceneOption(-1).build());

	}

	private int randomScene() {

		return randomScene(-1, -1);

	}

	private int randomScene(int start, int end) {

		int val = -1;

		while (val == -1 || sceneTileRepository.findByScenePos(val) != null) {

			if (start == -1 && end == -1)
				val = random.nextInt(20) + 5;
			else
				val = random.nextInt(end - start + 1) + start;

		}


		return val;

	}

	/**
	 * @return null if all scenes selected
	 */
	public Map getPendingAnalysis() {

		Map map = new HashMap<>();

		List<SceneTile> sceneTiles = sceneTileRepository.findAllByIsReplacedOrderBySceneTileIdAsc(false);

		for (int i = 0; i < sceneTiles.size(); i++) {

			SceneTile sceneTile = sceneTiles.get(i);

			if (sceneTile.getSceneOption() == -1) {
				map.put("scenePos", sceneTile.getScenePos());
				return map;
			}

		}

		return map;

	}

	/**
	 * @return all completed analysis
	 */
	public List<Map> getAvailableAnalysis() {

		List<SceneTile> sceneTiles = sceneTileRepository.findAllByIsReplacedOrderBySceneTileIdAsc(false);

		final List<Map> list = new ArrayList<>();

		for (SceneTile sceneTile : sceneTiles) {

			if (sceneTile.isReplaced())
				continue;

			if (sceneTile.getSceneOption() == -1)
				break;

			Map map = new HashMap<>();
//			map.put("sceneType", sceneTile.getSceneType());
			map.put("scenePos", sceneTile.getScenePos());
			map.put("sceneOption", sceneTile.getSceneOption());
			list.add(map);

		}

		return list;

	}

	public void onSelectedAnalysis(Map body, Principal principal) {
		// TODO Auto-generated method stub

		int sceneOption = (int) body.get("selectedAnalysisId");

		SceneTile sceneTile = sceneTileRepository.findFirstBySceneOptionOrderBySceneTileIdAsc(-1);

		sceneTile.setSceneOption(sceneOption);

		sceneTileRepository.save(sceneTile);

//		int size = sceneTileRepository.findAllBySceneOptionGreaterThan(-1).size();
//		
//		if (round == 0 && size == 6) {
//			
//		}

	}

	public boolean isFullAnalysisGivenForTheRound() {
		// TODO Auto-generated method stub
		List<SceneTile> all = sceneTileRepository.findAll();

		if (round == 0 && all.get(5).sceneOption == -1)
			return false;
		else if (round == 1 && all.get(6).sceneOption == -1)
			return false;
		else if (round == 2 && all.get(7).sceneOption == -1)
			return false;

		return true;
	}

	public void onNextAnalysis(Principal principal) {

		if (round < 2) {
			round += 1;
			timerService.start();
			return;
		}


	}

	public boolean forensicNeedsToRemoveAnalysis() {
		// TODO Auto-generated method stub

		if (round == 0)
			return false;

		List<SceneTile> all = sceneTileRepository.findAllByOrderBySceneTileIdAsc();

		if (round == 1 && all.get(6).getSceneOption() == -1 && sceneTileRepository.findAllByIsReplacedOrderBySceneTileIdAsc(true).size() == 0)
			return true;

		if (round == 2 && all.get(7).getSceneOption() == -1 && sceneTileRepository.findAllByIsReplacedOrderBySceneTileIdAsc(true).size() == 1)
			return true;

		return false;
	}

	public void onRemovedAnalysis(Map body, Principal principal) {

		int selectedAnalysisId = (int) body.get("selectedAnalysisId");

		List<SceneTile> all = sceneTileRepository.findAllByIsReplacedOrderBySceneTileIdAsc(false);

		SceneTile sceneTile = all.get(selectedAnalysisId);
		sceneTile.setReplaced(true);

		sceneTileRepository.save(sceneTile);


	}

	public int getRound() {
		// TODO Auto-generated method stub
		return round;
	}

}