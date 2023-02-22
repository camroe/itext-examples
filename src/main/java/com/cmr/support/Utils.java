package com.cmr.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Random;

public class Utils {
    private static final ObjectMapper staticMapper = new ObjectMapper();
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

    public static String prettyJson ( Object json) {
        String returnValue = "Error in PrettyJson";
        try {
            returnValue = staticMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            return returnValue;
        }
    }

}
