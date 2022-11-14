package digital_encode;

import java.math.BigInteger;
import java.util.Arrays;

public class DecodeFromByteTest {

    public static void main(String[] args) {
        int a = 50066;
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (a >>> 8);
        bytes[1] = (byte) (a & 0xff);
        System.out.println(Arrays.toString(bytes));

        System.out.println("parse by biginteger: " + new BigInteger(1, bytes).intValue());
        System.out.println("parse by my function: " + byte2int(bytes));
    }

    private static int byte2int(byte[] src) {
        // &0xff保证补码一致性
        // 数值类型从小到大扩展时，扩展部分填充值为符号位，如果需要保证二进制值不变，则需要&0xff来将在负数情况下填充为1的扩展位置零
        int a = ((int) src[0] & 0xff) << 8;
        int b = src[1] & 0xff;
        return a + b;
    }
}
