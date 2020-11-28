package com.kang.sketchq.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class CommonUtil {
    public static String[] WORDS = {"당나귀", "개", "고양이", "손님", "컴퓨터", "노트북", "청소기", "선풍기",
            "달", "태양", "휴지", "핸드폰", "삼성", "애플", "책", "청바지", "츄리닝", "공룡", "트와이스",
            "에어컨", "여름", "쿠팡", "나이키", "충전기", "배트맨", "조커", "세종대왕", "이순신", "거북이",
            "이재명", "쿠키", "창문", "봉준호", "기생충", "살인", "모자", "커피", "달고나", "리모컨", "모모",
            "전기장판", "부엉이", "분무기", "필라테스", "농구", "축구", "야구", "조던", "결혼식", "바다",
            "자전거", "기린", "코끼리", "사자", "호랑이", "까마귀", "까치", "모기", "거미", "새우", "꽃게",
            "골프", "테니스", "고래", "목성", "바지락", "조개", "로봇", "족제비", "일본", "프랑스", "한국",
            "미국", "농구공", "축구공", "해운대", "서울", "북한", "김정은", "김국진", "김구라", "유재석",
            "조세호", "돼지", "소", "총기", "쯔위", "아이유"};

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

    /**
     * 현재 시간을 반환
     * @return yyyyMMddHHmmss
     */
    public static String getNowDateTime(String format){
        SimpleDateFormat simpleDateFormatformat = new SimpleDateFormat( format);
        Calendar time = Calendar.getInstance();

        return simpleDateFormatformat.format(time.getTime());
    }
}
