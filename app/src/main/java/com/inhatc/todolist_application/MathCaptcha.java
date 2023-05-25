package com.inhatc.todolist_application;
import java.util.ArrayList;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

public class MathCaptcha extends Captcha {

    protected MathOptions options;

    public enum MathOptions{
        PLUS_MINUS,
        PLUS_MINUS_MULTIPLY
    }

    public MathCaptcha(int width, int height, MathOptions opt){
        this.height = height;
        setWidth(width);
        this.options = opt;
        usedColors = new ArrayList<Integer>();
        this.image = image();
    }

    @Override
    protected Bitmap image() {
        int one = 0;
        int two = 0;
        int math = 0;

        // 그라디언트 설정
        LinearGradient gradient = new LinearGradient(0, 0, getWidth() / 2, this.height / 2, color(), color(), Shader.TileMode.MIRROR);
        Paint p = new Paint();
        p.setDither(true);
        p.setShader(gradient);

        // 비트맵 생성 및 캔버스에 그라디언트 적용
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), this.height, Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawRect(0, 0, getWidth(), this.height, p);

        // 글자 그라디언트 설정
        LinearGradient fontGrad = new LinearGradient(0, 0, getWidth() / 2, this.height / 2, color(), color(), Shader.TileMode.CLAMP);
        Paint tp = new Paint();
        tp.setDither(true);
        tp.setShader(fontGrad);
        tp.setTextSize(getWidth() / this.height * 20);

        Random r = new Random(System.currentTimeMillis());
        one = r.nextInt(9) + 1;
        two = r.nextInt(9) + 1;
        math = r.nextInt((options == MathOptions.PLUS_MINUS_MULTIPLY)?3:2);

        // 숫자와 연산자 설정
        if (one < two) {
            Integer temp = one;
            one = two;
            two = temp;
        }

        switch (math) {
            case 0:
                this.answer = (one + two) + "";
                break;
            case 1:
                this.answer = (one - two) + "";
                break;
            case 2:
                this.answer = (one * two) + "";
                break;
        }

        char[] data = new char[]{String.valueOf(one).toCharArray()[0], oper(math), String.valueOf(two).toCharArray()[0]};

        for (int i=0; i<data.length; i++) {
            x += 30 + (Math.abs(r.nextInt()) % 65);
            y = 50 + Math.abs(r.nextInt()) % 50;
            Canvas cc = new Canvas(bitmap);
            if(i != 1)
                tp.setTextSkewX(r.nextFloat() - r.nextFloat());
            cc.drawText(data, i, 1, x, y, tp);
            tp.setTextSkewX(0);
        }

        return bitmap;
    }

    public static char oper(Integer math) {
        switch (math) {
            case 0:
                return '+';
            case 1:
                return '-';
            case 2:
                return '*';
        }
        return '+';
    }
}
