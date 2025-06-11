package com.myproject.brokagefirmchallenge;

import org.springframework.boot.SpringApplication;

public class TestBrokageFirmChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.from(BrokageFirmChallengeApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
