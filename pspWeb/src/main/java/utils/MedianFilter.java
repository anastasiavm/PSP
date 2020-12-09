package utils;

import models.Data;

import java.util.Arrays;

public class MedianFilter {

    /**
     * Метод для линейной фильтрации целого изображения
     * на границах изображения ипользуется центр ядра фильтра
     * @param data - Изображение для фильрации
     */
    public static void filter(Data data) {
        int[][][] result = new int[data.getPixels().length][][];

        int h = data.getFilterSize() / 2;

        for (int i = 0; i < data.getPixels().length; i++) {
            result[i] = new int[data.getPixels()[i].length][];

            for (int j = 0; j < data.getPixels()[i].length; j++) {
                result[i][j] = new int[data.getPixels()[i][j].length];

                for (int k = 0; k < data.getPixels()[i][j].length; k++) {
                    int[] filter = new int[data.getFilterSize() * data.getFilterSize()];

                    int count = 0;

                    for (int l = -h; l <= h; l++) {
                        for (int m = -h; m <= h; m++) {
                            if (i + l < 0
                                    || i + l >= data.getPixels().length
                                    || j + m < 0
                                    || j + m >= data.getPixels()[i].length) {
                                filter[count++] = data.getPixels()[i][j][k];
                            } else {
                                filter[count++] = data.getPixels()[i + l][j + m][k];
                            }
                        }
                    }

                    Arrays.sort(filter);
                    result[i][j][k] = filter[data.getFilterSize() + 1];
                }
            }
        }

        data.setPixels(result);
    }

    /**
     * Метод для фильтрации части изображения, изображение
     * @param data - Изображение для фильрации
     */
    public static void clientFilter(Data data) {
        int[][][] result = new int[data.getPixels().length][][];

        int h = data.getFilterSize() / 2;

        for (int i = 0; i < data.getPixels().length; i++) {
            result[i] = new int[data.getPixels()[i].length][];

            for (int j = 0; j < data.getPixels()[i].length; j++) {
                result[i][j] = new int[data.getPixels()[i][j].length];

                for (int k = 0; k < data.getPixels()[i][j].length; k++) {
                    int[] filter = new int[data.getFilterSize() * data.getFilterSize()];

                    int count = 0;

                    for (int l = -h; l <= h; l++) {
                        for (int m = -h; m <= h; m++) {
                            if (i + l < 0
                                    || i + l >= data.getPixels().length
                                    || j + m < 0
                                    || j + m >= data.getPixels()[i].length) {
                                filter[count++] = 128;
                            } else {
                                filter[count++] = data.getPixels()[i + l][j + m][k];
                            }
                        }
                    }

                    Arrays.sort(filter);
                    result[i][j][k] = filter[data.getFilterSize() + 1];
                }
            }
        }

        data.setPixels(result);
    }
}
