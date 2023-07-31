package com;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class TimerService {
	
	private Instant start;
	private long maxSeconds = 300;

	public void start() {
		start = Instant.now(); 
		
	}
	
	public void setTimerDuration(long value) {
		maxSeconds = value;
	}
	
	public long getTimeRemaining() {
		
		Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long seconds = duration.getSeconds();
        return maxSeconds - seconds;
        
	}
}
