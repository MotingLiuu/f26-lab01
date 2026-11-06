package edu.cmu.cs214.booking.domain;

/**
 * A half-open time interval {@code [start, end)}. Times are integer minutes
 * since midnight (600 = 10:00), which keeps the interval arithmetic easy to read.
 */
public record TimeInterval(int start, int end) {
    // Question: what does the record mean?
    // Answer: similar as
    // struct TimeInterval {
    //    const int start;
    //    const int end;
    // };
    // 内存视角：在堆上分配一个对象头（Object Header，包含类型指针与锁状态等，类似 64 位下的 8~16 字节元数据），后面紧跟两个 4 字节的整数 start 和 end。
    // 只读保证：字段全是 final（相当于 C 的 const），一旦分配初始化后不可修改。
    // 编译器自动为你生成了字段读取函数（相当于 t->start 的函数封装）以及按值比较两个结构体是否完全相等的函数。


    public TimeInterval {
        // Question: what does this mean? what is the type of TimeInterval?
        // Answer:
        // public TimeInterval 是初始化函数（构造函数 Constructor）
        // 执行初始化逻辑，因此没有返回值类型声明
        // 只不过省略了形式参数
        // Java record 允许省略形参列表 (int start, int end) 和赋值语句 this.start = start;
        if (start >= end) {
            throw new IllegalArgumentException(
                // Question: what does throw new mean?
                // Python 的 raise ValueError(...)
                "Interval must be non-empty: [" + start + ", " + end + ")");
        }
    }

    /**
     * Do this interval and {@code other} overlap? Two half-open intervals overlap
     * iff each starts before the other ends. Intervals that merely touch (one ends
     * exactly when the next begins) do <em>not</em> overlap.
     */
    public boolean overlaps(TimeInterval other) {
        return start < other.end && other.start < end;
    }
}
