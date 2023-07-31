package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserClueMeanOptionsRepository extends JpaRepository<UserClueMeanOption, Integer>{

	UserClueMeanOption findByUser(MyUser user);
	List<UserClueMeanOption> findAllByUser(MyUser user);
	List<UserClueMeanOption> findAllByUserOrderByUserClueMeanIdAsc(MyUser user);
	UserClueMeanOption findByClue(int clue);
	UserClueMeanOption findByMean(int mean);
	
	
	
}