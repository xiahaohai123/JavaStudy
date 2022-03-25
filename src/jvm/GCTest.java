package jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行需要添加JVM参数-XX:+PrintGCDetails 以打印gc过程
 * 建议在编译后使用java命令加jvm参数以运行，保证打印到控制台
 */
public class GCTest {

    public static void main(String[] args) {
        smallObject2Survive();
    }

    /**
     * 小对象可以进入survive区域时仍然会送进survive区域
     */
    private static void smallObject2Survive() {
        List<byte[]> allocations = new ArrayList<>();
        allocations.add(new byte[10240 * 1024]);
        allocations.add(new byte[10240 * 1024]);
        allocations.add(new byte[10240 * 1024]);
        allocations.add(new byte[10240 * 1024]);
        allocations.add(new byte[10240 * 1024]);
        allocations.add(new byte[10240 * 1024]);
        allocations.add(new byte[10240 * 1024]);
    }

    /**
     * 大对象直接送进老年代
     * allocation1需要直接分配大量内存以填满Eden区的内存，但是不要超过100%。
     * allocation2再分配内存时发现Eden区的内存不足，启动MinorGC。
     * 此时allocation1对象仍然存活，但是因为对象太大无法被送进survive区，所以只能直接送进老年代。
     */
    private static void bigObject2OldSpace() {
        byte[] allocation1, allocation2;
        //allocation1 = new byte[64300 * 1024];
        allocation1 = new byte[61300 * 1024];
        //System.gc();
        allocation2 = new byte[5120 * 1024];
    }
}
