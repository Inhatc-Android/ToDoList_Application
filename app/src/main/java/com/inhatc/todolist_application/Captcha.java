package com.inhatc.todolist_application;

import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Color;

public abstract class Captcha {
    protected Bitmap image;
    protected String answer = "";
    int width;
    int height;
    protected int x = 0;
    protected int y = 0;
    protected static List<Integer> usedColors;

    protected abstract Bitmap image();

    // 랜덤한 색상을 반환하는 메서드
    public static int color(){
        Random r = new Random();
        int number;
        do{
            number = r.nextInt(9);
        }while(usedColors.contains(number));
        usedColors.add(number);
        switch(number){
            case 0: return Color.BLACK;
            case 1: return Color.BLUE;
            case 2: return Color.CYAN;
            case 3: return Color.DKGRAY;
            case 4: return Color.GRAY;
            case 5: return Color.GREEN;
            case 6: return Color.MAGENTA;
            case 7: return Color.RED;
            case 8: return Color.YELLOW;
            case 9: return Color.WHITE;
            default: return Color.WHITE;
        }
    }

    public int getWidth(){
        return this.width;
    }

    public void setWidth(int width){
        // 너비 값이 유효한 범위인지 확인하고 설정하는 메서드
        if(width > 0 && width < 10000){
            this.width = width;
        }else{
            this.width = 300; // 유효하지 않은 값이면 기본 값으로 설정
        }
    }

    public int getHeight(){
        return this.height;
    }

    public void setHeight(int height){
        // 높이 값이 유효한 범위인지 확인하고 설정하는 메서드
        if(height > 0 && height < 10000){
            this.height = height;
        }else{
            this.height = 100; // 유효하지 않은 값이면 기본 값으로 설정
        }
    }

    public Bitmap getImage() {
        return this.image;
    }

    // 사용자의 답과 정답을 비교하여 맞는지 확인하는 메서드
    public boolean checkAnswer(String ans) {
        return ans.equals(this.answer);
    }
}
