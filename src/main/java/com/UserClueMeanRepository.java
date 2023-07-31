package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserClueMeanRepository extends JpaRepository<UserClueMean, Integer>{

	List<UserClueMean> findAllByIsConfirmed(boolean isConfirmed);
	
	UserClueMean findByUser(MyUser user);
	
}