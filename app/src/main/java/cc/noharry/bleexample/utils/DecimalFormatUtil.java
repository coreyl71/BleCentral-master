package cc.noharry.bleexample.utils;

import java.math.BigDecimal;

public class DecimalFormatUtil {

    /**
     * 对小数位数进行转换
     *
     * @param decimal 保留小数的位数
     * @param obj     待转换的目标
     * @return 返回的格式化数据
     */
    public static String formatPriceNAmount(String decimal, String obj) {

        String result = "- -";

        if (null == obj) {
            return result;
        }

        if (null == decimal) {
            return result;
        }

        try {
            if (Double.parseDouble(obj) == 0) {
                return result;
            }
        } catch (NumberFormatException e) {
            return result;
        }


        if (null != obj && null != decimal) {

//            try {
//                double lastPriceF = Double.parseDouble(obj);
//                result = String.format("%." + decimal + "f", lastPriceF);
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            }

            try {

                BigDecimal bigDecimal = new BigDecimal(obj);
                L.i("formatPriceNAmount---bigDecimal = " + bigDecimal);

                result = bigDecimal.setScale(Integer.parseInt(decimal), BigDecimal.ROUND_DOWN).toString();
                L.i("formatPriceNAmount---result = " + result);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }


        }

        return result;

    }

}
