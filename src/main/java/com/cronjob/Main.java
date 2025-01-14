package com.cronjob;

import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main {

    public static void main(String... args) {
        System.out.println("HELLO WORLD FROM CRONJOB");
        System.exit(0);
    }
}