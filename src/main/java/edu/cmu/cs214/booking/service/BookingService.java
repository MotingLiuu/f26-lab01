package edu.cmu.cs214.booking.service;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
import edu.cmu.cs214.booking.repo.BookingStore;
import java.util.List;

/**
 * Coordinates bookings and the waitlist. Enforces the core invariant: a room
 * never holds two confirmed bookings whose intervals overlap. Persistence is
 * delegated to a {@link BookingStore}.
 */
public class BookingService {

    private final BookingStore store;
    private int nextBookingSeq = 1;
    private int nextWaitlistSeq = 1;

    public BookingService(BookingStore store) {
        this.store = store;
    }

    /**
     * Attempts to book {@code room} for {@code user} over {@code interval}. If the
     * room is free over that interval the booking is confirmed; otherwise the user
     * is placed on the room's waitlist.
     */
    public BookingResult book(Room room, User user, TimeInterval interval) {
        for (Booking existing : store.bookingsForRoom(room)) {
            // Question: what does Booking existing: mean?
            // Answer:
            // Java 的“增强型 for 循环（Enhanced for loop / For-each）”，等价于 Python 的 for existing in ...。
            // Booking：循环变量的类型声明。在 C 中你必须写 int i 或 struct Booking *b，Java 是静态类型语言，必须标明迭代出来的元素类型。
            // existing：局部变量名，用来接收当前遍历到的那一个元素。
            // 冒号 :：对应 Python 的 in。
            if (existing.interval().overlaps(interval)) {
                // If an overlap is found:
                // Calls store.waitlistForRoom(room).
                // Calls store.addWaitlistEntry(...).
                // Returns a BookingResult.Waitlisted object.

                // Question: Does this mean java's list's size is changable just like Python's list
                // Answer: Java 中的 List（最常用的实现是 ArrayList）和 Python 的 list 在底层数据结构、内存模型与动态扩容机制上几乎一模一样。

                // Question: What is existing.interval()? why not use existing.interval? interval is a member in existing.
                // Answer:
                // 解耦接口与内部实现（Decoupling Implementation from Interface）
                // 允许未来改变内部实现而不破坏调用者：
                // 假设今天 Booking 内部存的是 TimeInterval interval;。
                // 半年后重构，你发现存 int startTimestamp, int duration（开始时间戳 + 持续时长）更省内存。
                // 如果外部都直接写 existing.interval，改了字段类型所有调用处的代码都会编译崩溃。
                // 但如果是函数 existing.interval()，你只需要修改函数内部逻辑
                int position = store.waitlistForRoom(room).size() + 1;
                int seq = nextWaitlistSeq++;
                store.addWaitlistEntry(new WaitlistEntry("w" + seq, room, user, interval, seq));
                return new BookingResult.Waitlisted(position);
            }
        }
        Booking booking = new Booking("b" + nextBookingSeq++, room, user, interval);
        store.addBooking(booking);
        return new BookingResult.Confirmed(booking);
    }

    /** Cancels the confirmed booking with the given ID, if it exists. */
    public void cancelBooking(String bookingId) {
        store.findBooking(bookingId).ifPresent(booking -> store.removeBooking(bookingId));
    }

    /** Returns the confirmed bookings for {@code room}. */
    public List<Booking> listBookings(Room room) {
        return store.bookingsForRoom(room);
    }
}
