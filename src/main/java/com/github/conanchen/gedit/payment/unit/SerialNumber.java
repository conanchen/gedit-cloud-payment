package com.github.conanchen.gedit.payment.unit;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by machine on 2017/7/10.
 */


public class SerialNumber {

    private final static String[] stringArray = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

    private final static Integer[] numberArray= { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String digitalSerialNumber(int length){
        List<Integer> integerList = new ArrayList<>(Arrays.asList(numberArray));
        Collections.shuffle(integerList);

        List<Integer> randomList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        Random random = new Random();
        while (randomList.size() < length){
            if(integerList.size()==0){
                integerList = new ArrayList<>(Arrays.asList(numberArray));
                Collections.shuffle(integerList);
            }
            int index = random.nextInt(integerList.size());
            int thisRan = integerList.get(index);
            randomList.add(thisRan);
            stringBuffer.append(thisRan);
            integerList.remove(index);
        }
        return stringBuffer.toString();
    }
    /**
     * 无参数生成固定20位长度无时间
     * @return
     */
    public  static String digitalSerialNumber(){
        return digitalSerialNumber(20);
    }

    public  static String digitalSerialString(int length){
        List<String> stringList = new ArrayList<>(Arrays.asList(stringArray));
        Collections.shuffle(stringList);

        StringBuffer stringBuffer = new StringBuffer();
        List<String> randomList = new ArrayList<>();
        Random random = new Random();
        while (randomList.size() < length){
            if(stringList.size()==0){
                stringList = new ArrayList<>(Arrays.asList(stringArray));
                Collections.shuffle(stringList);
            }
            int index = random.nextInt(stringList.size());
            String  thisRan = stringList.get(index);
            randomList.add(thisRan);
            stringBuffer.append(thisRan);
            stringList.remove(index);
        }
        return stringBuffer.toString();
    }
    /**
     * 17位时间之后还需要加上length位随机字符(随机字符仅是数字)
     * @param length
     * @return
     */
    private  static String digitalSerialNumberAndDate(int length){
        String date = dateFormat.format(new Date());
        return date+digitalSerialNumber(length);
    }

    /**
     * 无参数生成固定20位长度-有时间
     * @return
     */
    public  static Long digitalSerialNumberAndDate(){
        String date = dateFormat.format(new Date());
        return Long.valueOf(date + digitalSerialNumber(5).toString());
    }
    /**
     * 13位时间之后还需要加上length位随机字符(随机字符不仅是数字)
     * @param length
     * @return
     */
    public  static String digitalSerialStringAndDate(int length){
            String date = dateFormat.format(new Date());
            return date+digitalSerialString(length);
    }

//    public static String getCheckCodeByUUID(int length){
//        String uuid = UUIDUtils.creatUUID();
//        StringBuffer sb = new StringBuffer();
//        Random random = new Random();
//        for (int i = 0; i < length;i ++){
//            int index = random.nextInt(uuid.length());
//            String  indexUuid;
//            if(index  == 32){
//                indexUuid = uuid.substring(index-1,index);
//            }else{
//                indexUuid = uuid.substring(index,index+1);
//            }
//
//            sb.append(indexUuid);
//        }
//        return  sb.toString();
//    }
}
