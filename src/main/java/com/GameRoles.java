package com;
public enum GameRoles{
	
	FORENSIC("FORENSIC"),
	MURDERER("MURDERER"),
	INVESTIGATOR("INVESTIGATOR"),
	ACCOMPLICE("ACCOMPLICE"),
	WITNESS("WITNESS")
	;
	
	private final String value;
	
	private GameRoles(String value) {
		this.value = value;
	}
	
	public String getValue() { return value ;}
	
}