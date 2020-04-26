package com.github.kline.entity;





import com.github.klib.entity.KEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据辅助类 计算macd rsi等
 */

public class DataHelper {
    public static List<KEntity> getALL(List<KEntity> list) {
        if (list != null) {
            DataHelper.calculate(list);
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * 计算RSI
     *
     * @param datas
     */
    static void calculateRSI(List<KEntity> datas) {
        float rsi1 = 0;
        float rsi2 = 0;
        float rsi3 = 0;
        float rsi1ABSEma = 0;
        float rsi2ABSEma = 0;
        float rsi3ABSEma = 0;
        float rsi1MaxEma = 0;
        float rsi2MaxEma = 0;
        float rsi3MaxEma = 0;
        for (int i = 0; i < datas.size(); i++) {
            KEntity point = datas.get(i);
            final float closePrice = point.getClose();
            if (i == 0) {
                rsi1 = 0;
                rsi2 = 0;
                rsi3 = 0;
                rsi1ABSEma = 0;
                rsi2ABSEma = 0;
                rsi3ABSEma = 0;
                rsi1MaxEma = 0;
                rsi2MaxEma = 0;
                rsi3MaxEma = 0;
            } else {
                float Rmax = Math.max(0, closePrice - datas.get(i - 1).getClose());
                float RAbs = Math.abs(closePrice - datas.get(i - 1).getClose());
                rsi1MaxEma = (Rmax + (6f - 1) * rsi1MaxEma) / 6f;
                rsi1ABSEma = (RAbs + (6f - 1) * rsi1ABSEma) / 6f;

                rsi2MaxEma = (Rmax + (12f - 1) * rsi2MaxEma) / 12f;
                rsi2ABSEma = (RAbs + (12f - 1) * rsi2ABSEma) / 12f;

                rsi3MaxEma = (Rmax + (24f - 1) * rsi3MaxEma) / 24f;
                rsi3ABSEma = (RAbs + (24f - 1) * rsi3ABSEma) / 24f;

                rsi1 = (rsi1MaxEma / rsi1ABSEma) * 100;
                rsi2 = (rsi2MaxEma / rsi2ABSEma) * 100;
                rsi3 = (rsi3MaxEma / rsi3ABSEma) * 100;
            }
            point.setRsi1(rsi1);
            point.setRsi2(rsi2);
            point.setRsi3(rsi3);
        }
    }

    /**
     * 计算kdj
     *
     * @param datas
     */
    static void calculateKDJ(List<KEntity> datas) {
        float k = 0;
        float d = 0;

        for (int i = 0; i < datas.size(); i++) {
            KEntity point = datas.get(i);
            final float closePrice = point.getClose();
            int startIndex = i - 8;
            if (startIndex < 0) {
                startIndex = 0;
            }
            float max9 = Float.MIN_VALUE;
            float min9 = Float.MAX_VALUE;
            for (int index = startIndex; index <= i; index++) {
                max9 = Math.max(max9, datas.get(index).getHighest());
                min9 = Math.min(min9, datas.get(index).getLowest());

            }
            float rsv = 100f * (closePrice - min9) / (max9 - min9);
            if (i == 0) {
                k = rsv;
                d = rsv;
            } else {
                k = (rsv + 2f * k) / 3f;
                d = (k + 2f * d) / 3f;
            }
            point.setK(k);
            point.setD(d);
            point.setJ(3f * k - 2 * d);
        }

    }

    /**
     * 计算macd
     *
     * @param datas
     */
    static void calculateMACD(List<KEntity> datas) {
        float ema12 = 0;
        float ema26 = 0;
        float dif = 0;
        float dea = 0;
        float macd = 0;

        for (int i = 0; i < datas.size(); i++) {
            KEntity point = datas.get(i);
            final float closePrice = point.getClose();
            if (i == 0) {
                ema12 = closePrice;
                ema26 = closePrice;
            } else {
//                EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
//                EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema12 = ema12 * 11f / 13f + closePrice * 2f / 13f;
                ema26 = ema26 * 25f / 27f + closePrice * 2f / 27f;
            }
//            DIF = EMA（12） - EMA（26） 。
//            今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
//            用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26;
            dea = dea * 8f / 10f + dif * 2f / 10f;
            macd = (dif - dea) * 2f;
            point.setDif(dif);
            point.setDea(dea);
            point.setMacd(macd);
        }

    }

    /**
     * 计算 BOLL 需要在计算ma之后进行
     *
     * @param datas
     */
    static void calculateBOLL(List<KEntity> datas) {
        for (int i = 0; i < datas.size(); i++) {
            KEntity point = datas.get(i);
            final float closePrice = point.getClose();
            if (i == 0) {
                point.setMb(closePrice);
                point.setUp(Float.NaN);
                point.setDn(Float.NaN);
            } else {
                int n = 20;
                if (i < 20) {
                    n = i + 1;
                }
                float md = 0;
                for (int j = i - n + 1; j <= i; j++) {
                    float c = datas.get(j).getClose();
                    float m = point.getMA30();
                    float value = c - m;
                    md += value * value;
                }
                md = md / (n - 1);
                md = (float) Math.sqrt(md);
                point.setMb(point.getMA30());
                point.setUp(point.getMb() + 2f * md);
                point.setDn(point.getMb() - 2f * md);
            }
        }

    }

    /**
     * 计算ma
     *
     * @param datas
     */
    public static void calculateMA(List<KEntity> datas) {
        float ma5 = 0;
        float ma10 = 0;
        float ma30 = 0;

        for (int i = 0; i < datas.size(); i++) {
            KEntity point = datas.get(i);
            final float closePrice = point.getClose();

            ma5 += closePrice;
            ma10 += closePrice;
            ma30 += closePrice;
            if (i >= 5) {
                ma5 -= datas.get(i - 5).getClose();
                point.setMA5(ma5 / 5f);
            } else {
                point.setMA5(ma5 / (i + 1f));
            }
            if (i >= 10) {
                ma10 -= datas.get(i - 10).getClose();
                point.setMA10(ma10 / 10f);
            } else {
                point.setMA10(ma10 / (i + 1f));
            }
            if (i >= 30) {
                ma30 -= datas.get(i - 30).getClose();
                point.setMA30(ma30 / 30f);
            } else {
                point.setMA30(ma30 / (i + 1f));
            }
        }
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     *
     * @param list
     */
    static void calculate(List<KEntity> list) {
        calculateMA(list);
        calculateMACD(list);
        calculateBOLL(list);
        calculateRSI(list);
        calculateKDJ(list);
        calculateVolumeMA(list);
    }

    private static void calculateVolumeMA(List<KEntity> entries) {
        float volumeMa5 = 0;
        float volumeMa10 = 0;

        for (int i = 0; i < entries.size(); i++) {
            KEntity entry = entries.get(i);

            volumeMa5 += entry.getVolume();
            volumeMa10 += entry.getVolume();

            if (i >= 5) {

                volumeMa5 -= entries.get(i - 5).getVolume();
                entry.setMa5Volume((volumeMa5 / 5f));
            } else {
                entry.setMa5Volume((volumeMa5 / (i + 1f)));
            }

            if (i >= 10) {
                volumeMa10 -= entries.get(i - 10).getVolume();
                entry.setMa10Volume((volumeMa10 / 5f));
            } else {
                entry.setMa10Volume((volumeMa10 / (i + 1f)));
            }
        }
    }
}
