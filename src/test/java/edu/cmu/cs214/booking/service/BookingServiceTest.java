package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
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
    void cancelBookingSkipsConflictingWaitlistEntry() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        Booking cancelled = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(600, 660))).booking();
        Booking remaining = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(660, 720))).booking();
        User carol = new User("u3", "Carol");
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 690)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, carol, new TimeInterval(600, 660)));
        WaitlistEntry conflicting = store.waitlistForRoom(roomA).get(0);

        svc.cancelBooking(cancelled.id());

        List<Booking> bookings = store.bookingsForRoom(roomA);
        assertEquals(2, bookings.size());
        assertEquals(remaining, bookings.get(0));
        assertEquals(carol, bookings.get(1).user());
        assertEquals(new TimeInterval(600, 660), bookings.get(1).interval());
        assertEquals(List.of(conflicting), store.waitlistForRoom(roomA));
    }

    @Test
    void cancelBookingChecksWaitersInSequenceOrder() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        TimeInterval interval = new TimeInterval(600, 660);
        Booking cancelled = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, interval)).booking();
        User carol = new User("u3", "Carol");
        WaitlistEntry later = new WaitlistEntry("later", roomA, bob, interval, 2);
        WaitlistEntry earlier = new WaitlistEntry("earlier", roomA, carol, interval, 1);
        store.addWaitlistEntry(later);
        store.addWaitlistEntry(earlier);

        svc.cancelBooking(cancelled.id());

        assertEquals(1, store.bookingsForRoom(roomA).size());
        assertEquals(carol, store.bookingsForRoom(roomA).get(0).user());
        assertEquals(List.of(later), store.waitlistForRoom(roomA));
    }

    @Test
    void cancelBookingPromotesEarliestWaitlistEntry() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        TimeInterval interval = new TimeInterval(600, 660);
        Booking cancelled = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, interval)).booking();
        User carol = new User("u3", "Carol");
        assertInstanceOf(BookingResult.Waitlisted.class, svc.book(roomA, bob, interval));
        assertInstanceOf(BookingResult.Waitlisted.class, svc.book(roomA, carol, interval));
        WaitlistEntry later = store.waitlistForRoom(roomA).get(1);

        svc.cancelBooking(cancelled.id());

        assertEquals(List.of(new Booking("b2", roomA, bob, interval)), store.bookingsForRoom(roomA));
        assertEquals(List.of(later), store.waitlistForRoom(roomA));
    }

    @Test
    void cancelBookingPromotesAtMostOne() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        Booking cancelled = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(600, 720))).booking();
        User carol = new User("u3", "Carol");
        TimeInterval firstInterval = new TimeInterval(600, 660);
        TimeInterval secondInterval = new TimeInterval(660, 720);
        assertInstanceOf(BookingResult.Waitlisted.class, svc.book(roomA, bob, firstInterval));
        assertInstanceOf(BookingResult.Waitlisted.class, svc.book(roomA, carol, secondInterval));
        WaitlistEntry later = store.waitlistForRoom(roomA).get(1);

        svc.cancelBooking(cancelled.id());

        assertEquals(List.of(new Booking("b2", roomA, bob, firstInterval)), store.bookingsForRoom(roomA));
        assertEquals(List.of(later), store.waitlistForRoom(roomA));

        svc.cancelBooking("b2");

        assertEquals(List.of(new Booking("b3", roomA, carol, secondInterval)), store.bookingsForRoom(roomA));
        assertEquals(List.of(), store.waitlistForRoom(roomA));
    }

    @Test
    void cancelBookingWithNoFittingWaitersPromotesNoOne() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        Booking cancelled = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(600, 660))).booking();
        Booking remaining = assertInstanceOf(BookingResult.Confirmed.class,
                svc.book(roomA, alice, new TimeInterval(660, 720))).booking();
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 690)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(660, 720)));
        List<WaitlistEntry> waiters = store.waitlistForRoom(roomA);

        svc.cancelBooking(cancelled.id());

        assertEquals(List.of(remaining), store.bookingsForRoom(roomA));
        assertEquals(waiters, store.waitlistForRoom(roomA));
    }

    @Test
    void listBookingsReturnsConfirmedBookings() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(660, 720));
        assertEquals(2, svc.listBookings(roomA).size());
    }
}
