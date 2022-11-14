package digital_encode;

public class ShiftTest {
    public static void main(String[] args) {
        ShiftTest shiftTest = new ShiftTest();
        shiftTest.testSignedShift();
        shiftTest.testUnsignedShift();
    }

    private void testSignedShift() {
        System.out.println("signedShift: ");
        int positiveA = 18;  // 0000 0000 0000 0000 0000 0000 0001 0010
        System.out.println("positiveA: " + positiveA);
        int a = 18 >> 2;  // a: 0000 0000 0000 0000 0000 0000 0000 0100
        System.out.println("a: " + a);

        int positiveB = -18;  // 1111 1111 1111 1111 1111 1111 1110 1110
        System.out.println("positiveB: " + positiveB);
        int b = -18 >> 2;  // b: 1111 1111 1111 1111 1111 1111 1111 1011
        System.out.println("b: " + b);
    }

    private void testUnsignedShift() {
        System.out.println("unsignedShift: ");
        int positiveA = 18;  // 0000 0000 0000 0000 0000 0000 0001 0010
        System.out.println("positiveA: " + positiveA);
        int a = 18 >>> 2; // a: 0000 0000 0000 0000 0000 0000 0000 0100
        System.out.println("a: " + a);

        int positiveB = -18;  // 1111 1111 1111 1111 1111 1111 1110 1110
        System.out.println("positiveB: " + positiveB);
        int b = -18 >>> 2; // b: 0011 1111 1111 1111 1111 1111 1111 1011
        System.out.println("b: " + b);
    }
}
