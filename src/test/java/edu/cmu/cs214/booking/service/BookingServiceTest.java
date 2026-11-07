package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");

    private BookingService newService() {
        return new BookingService(new InMemoryBookingStore());
    }

    @Test
    void bookConfirmsWhenRoomIsFree() {
        BookingService svc = newService();
        BookingResult r = svc.book(roomA, alice, new TimeInterval(600, 660));
        assertInstanceOf(BookingResult.Confirmed.class, r);
    }

    @Test
    void bookWaitlistsWhenSlotIsTaken() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        BookingResult r = svc.book(roomA, bob, new TimeInterval(630, 700));
        assertInstanceOf(BookingResult.Waitlisted.class, r);
    }

    @Test
    void backToBackBookingsAreConfirmed() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        BookingResult r = svc.book(roomA, bob, new TimeInterval(660, 720));
        assertInstanceOf(BookingResult.Confirmed.class, r);
    }

    @Test
    void sameSlotInDifferentRoomsAreBothConfirmed() {
        // Agent:
        // 测试用例预期行为：
        // 在测试 sameSlotInDifferentRoomsAreBothConfirmed 中：
        // 首先在 roomA 预订了时间段 [600, 660]；
        // 接着在另一个不同房间 roomB 预订相同的时间段 [600, 660]。
        // 因为是两个不同的房间，彼此时间段并不冲突，预期两次预订都应成功确认（即期望返回类型为 BookingResult.Confirmed）。
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        BookingResult r = svc.book(roomB, bob, new TimeInterval(600, 660));
        assertInstanceOf(BookingResult.Confirmed.class, r);
    }
    // lab1:
    // [ERROR] Failures:
    // [ERROR]   BookingServiceTest.sameSlotInDifferentRoomsAreBothConfirmed:51 Unexpected type, expected: <edu.cmu.cs214.booking.service.BookingResult.Confirmed> but was: <edu.cmu.cs214.booking.service.BookingResult.Waitlisted>
    // [

    @Test
    void cancelNonExistentBookingDoesNothing() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        Booking first = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(600, 660))).booking();
        Booking second = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomB, bob, new TimeInterval(600, 660))).booking();

        svc.cancelBooking("missing");

        assertEquals(List.of(first, second), store.allBookings());
    }

    @Test
    void cancelBookingFreesSlot() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        TimeInterval interval = new TimeInterval(600, 660);
        Booking original = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, interval)).booking();
        Booking other = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(660, 720))).booking();

        svc.cancelBooking(original.id());

        assertEquals(List.of(other), store.bookingsForRoom(roomA));
        Booking replacement = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, bob, interval)).booking();
        assertEquals(List.of(other, replacement), store.bookingsForRoom(roomA));
    }

    @Test
    void listBookingsReturnsConfirmedBookings() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(660, 720));
        assertEquals(2, svc.listBookings(roomA).size());
    }
}
