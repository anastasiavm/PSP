package models;

import com.google.gson.Gson;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.Serializable;
import java.util.Arrays;

public class Data implements Serializable {
    // Размер фильтра
    private int filterSize;

    // Матрица пиксселей изображения
    private int[][][] pixels;

    public Data(int filterSize, int[][][] pixels) {
        this.filterSize = filterSize;
        this.pixels = pixels;
    }

    public int getFilterSize() {
        return filterSize;
    }

    public int[][][] getPixels() {
        return pixels;
    }

    public void setPixels(int[][][] pixels) {
        this.pixels = pixels;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                for (int k = 0; k < 3; k++) {
                    builder.append(pixels[i][j][k]);
                    builder.append(",");
                }
            }
        }

        builder.delete(builder.length() - 1, builder.length());
        builder.append("]");

        return builder.toString();
    }


    /**
     * Разбивает картинку, дублирует пиксели на границах по размеру фильтра
     * @param start - номер строки с которой производиться замена
     * @param numLines - количество строк
     * @return
     */
    public int[][][] partOfImage(int start, int numLines) {
        if (numLines == -1) numLines = pixels.length - start;

        int h = filterSize / 2;
        int[][][] result = new int[ numLines + 2*h ][][];
        int a = start - h;
        int b = start + h;
        for (int i = 0; i < result.length; i++) {
            if(a < 0) {
                result[i] = getPixels()[0];
            }
            else if(a + h > pixels.length - 1){
                result[i] = getPixels()[pixels.length-1];
                b--;
            }
            else result[i] = getPixels()[a];
            a++;
        }

        return result;
    }

    /**
     * Замена пикселей исходного изображения
     * @param data - обьект нового изображение изображение
     */
    public void change(Data data) {
        for (int i = 0; i < data.getPixels().length ; i++) {
            pixels[i] = data.getPixels()[i];
        }
    }

    /**
     * Частичная замена пикселей исходного изображения
     * @param start - номер строки с которой производиться замена
     * @param numlines -  количество строк для замены
     * @param data - обьект нового изображения
     */
    public void changeData(int start, int numlines, Data data) {
        if (numlines == -1) numlines = pixels.length - start;
        for (int i = start - filterSize; i < start + numlines + filterSize ; i++) {
            pixels[i] = data.getPixels()[i];
        }
    }

}
