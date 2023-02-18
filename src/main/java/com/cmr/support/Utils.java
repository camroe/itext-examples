package com.cmr.support;

import java.util.Random;

public class Utils {

    public static int rnd1To(int limit) {
        //Random number between 1 and Limit
        Random rn = new Random();
        int answer = rn.nextInt(limit) + 1;
        return answer;
    }

    public static int rndBetween(int lowerLimit,  int upperLimit) {
        //Random number between lowerLimit and upperLimit
        Random rn = new Random();
        int answer = rn.nextInt(upperLimit) + lowerLimit+1;
        return answer;
    }
}
