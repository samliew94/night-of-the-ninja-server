package com;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UserClueMeanOption{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userClueMeanId;
	
	@ManyToOne
	@JoinColumn(name = "username")
	private MyUser user;
	private int clue;
	private int mean;
	private boolean isConfirmed;
	
}