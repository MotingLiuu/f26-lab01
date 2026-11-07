package edu.cmu.cs214.booking.service;

import edu.cmu.cs214.booking.domain.Booking;

/** The outcome of a booking attempt: either confirmed, or placed on the waitlist. */
public sealed interface BookingResult {
    // Sealed restricts which types can implement this interface. Here, the nested
    // Confirmed and Waitlisted records are implicitly the permitted types.

    /** The room was free; the booking is confirmed. */
    record Confirmed(Booking booking) implements BookingResult {
        // A record is a concise data-carrying class. Java generates a constructor,
        // booking() accessor, equals(), hashCode(), and toString() for it.
        // Implements means Confirmed is a BookingResult and fulfills its contract.
    }

    /** The room was taken; the user was placed on the waitlist at {@code position}. */
    record Waitlisted(int position) implements BookingResult {
    }
}
