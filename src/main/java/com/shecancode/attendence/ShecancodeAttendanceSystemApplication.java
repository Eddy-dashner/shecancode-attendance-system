package com.shecancode.attendence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//trying to push and solve main auth
@SpringBootApplication
@EnableScheduling
public class ShecancodeAttendanceSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShecancodeAttendanceSystemApplication.class, args);
	}

}
