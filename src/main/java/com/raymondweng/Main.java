package com.raymondweng;

import net.dv8tion.jda.api.JDABuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.print("Input the token: ");
        new Main(new BufferedReader(new InputStreamReader(System.in)).readLine());
    }

    public Main(String token) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.build();
    }

    public void start(){

    }
}