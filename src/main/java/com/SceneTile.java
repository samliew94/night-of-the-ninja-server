package com;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SceneTile{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int sceneTileId;
	
	/**
	 * 0 - Cause of death
	 * 1 - Location of Crime
	 * 2 - others
	 */
	int sceneType;
	int scenePos;
	
	/**
	 * what forensic chose
	 */
	int sceneOption;
	
	/**
	 *  basically, don't show this if true
	 */
	boolean isReplaced;
	
}