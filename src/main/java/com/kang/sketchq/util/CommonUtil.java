package com.kang.sketchq.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CommonUtil {
    public static String[] WORDS = {"당나귀", "개", "고양이"};

    public static String getRandomString(int length){
        StringBuffer temp = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0:
                    // a-z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    // A-Z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    // 0-9
                    temp.append((rnd.nextInt(10)));
                    break;
            }
        }
        return temp.toString();
    }

    public static String getRandomWord(){
        List<String> list = Arrays.asList(WORDS);
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }
}
