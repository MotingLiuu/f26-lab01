# Milestone 2 Plan: Add `cancelBooking`

## Overview
Implement `public void cancelBooking(String bookingId)` in `BookingService` to allow cancellation of confirmed bookings and automatic promotion of eligible waitlisted users while preserving the core invariant: **a room never holds two confirmed bookings whose intervals overlap**.

Development follows an incremental test-driven approach: for each sub-task, write its corresponding unit tests in `BookingServiceTest.java`, implement the required logic in `BookingService.java`, and verify immediately via `mvn test`.

---

## Sub-tasks Decomposition

### Sub-task 1: Cancellation & Non-existent Booking Handling
- **Implementation**:
  - Look up the booking using `store.findBooking(bookingId)`.
  - If no booking with `bookingId` exists, do nothing and return cleanly.
  - If found, remove it via `store.removeBooking(bookingId)`, freeing its slot.
- **Corresponding Unit Tests**:
  - `cancelNonExistentBookingDoesNothing`: Verify that cancelling a non-existent booking ID leaves all existing bookings intact and does not throw any exceptions.
  - `cancelBookingFreesSlot`: Verify that cancelling an existing confirmed booking removes it from `store.bookingsForRoom(room)` and allows subsequent bookings in that freed slot to be confirmed.
- **Verification**:
  - Run `mvn test -Dtest=BookingServiceTest` to ensure Sub-task 1 tests compile and pass.

### Sub-task 2: Waitlist Candidate Selection & Conflict Checking (FCFS Ordering & Conflict Skipping)
- **Implementation**:
  - Retrieve waitlist entries for the room using `store.waitlistForRoom(room)`.
  - Sort candidates by arrival order (`WaitlistEntry.seq`) ascending (First-Come, First-Served).
  - Retrieve remaining confirmed bookings for the room via `store.bookingsForRoom(room)`.
  - Check whether a candidate waiter's interval overlaps any remaining confirmed booking.
  - If a candidate conflicts with an existing booking, skip that waiter and inspect the next candidate in sequence.
- **Corresponding Unit Tests**:
  - `cancelBookingSkipsConflictingWaitlistEntry`: Setup where the earliest waiter's interval still conflicts with another remaining confirmed booking, but a second waiter in line does not conflict. Verify the earliest conflicting waiter is skipped in favor of the first eligible, non-conflicting waiter.
  - `cancelBookingWithNoFittingWaitersPromotesNoOne`: Setup where all waiters in the queue conflict with remaining confirmed bookings. Verify no waiters are promoted and waitlist remains unchanged.
- **Verification**:
  - Run `mvn test -Dtest=BookingServiceTest` to ensure conflict-checking and skipping tests pass.

### Sub-task 3: Waitlist Promotion & Single-Promotion Constraint
- **Implementation**:
  - Once the first non-conflicting candidate is identified:
    1. Create a confirmed `Booking` using `"b" + nextBookingSeq++`, the room, the candidate's user, and interval.
    2. Save the new booking via `store.addBooking(booking)`.
    3. Remove the entry from the room's waitlist via `store.removeWaitlistEntry(waiter.id())`.
    4. Terminate the promotion loop immediately to ensure **at most one** waiter is promoted per cancellation.
  - If the waitlist is empty or no candidates fit, do nothing further.
- **Corresponding Unit Tests**:
  - `cancelBookingPromotesEarliestWaitlistEntry`: Verify that cancelling a booking promotes the earliest waitlist entry (`seq` order) to a confirmed booking and removes it from the waitlist.
  - `cancelBookingPromotesAtMostOne`: Setup multiple waiters that could both fit into the freed interval or non-conflicting slots. Verify that exactly one waiter is promoted per cancellation and the other remains on the waitlist.
- **Verification**:
  - Run `mvn test -Dtest=BookingServiceTest` to ensure single-waiter promotion and ordering tests pass.

### Sub-task 4: End-to-End & Invariant Regression Verification
- **Implementation & Scope**:
  - Run the entire test suite (`mvn test`) across all modules (`BookingServiceTest`, `TimeIntervalTest`).
  - Verify that the core invariant holds under all cancellation & promotion scenarios: a room never holds two confirmed bookings whose intervals overlap.
  - Clean up any unused imports or temporary comments.
- **Verification**:
  - Execute full `mvn clean test` with 0 failures and 0 errors.

---

## Execution Steps

1. **Step 1 (Sub-task 1)**:
   - Add tests `cancelNonExistentBookingDoesNothing` and `cancelBookingFreesSlot` in `BookingServiceTest.java`.
   - Implement cancellation logic in `cancelBooking` in `BookingService.java`.
   - Run `mvn test` to verify.

2. **Step 2 (Sub-task 2 & 3 - Iterative Promotion Logic)**:
   - Add tests for FCFS promotion (`cancelBookingPromotesEarliestWaitlistEntry`, `cancelBookingPromotesAtMostOne`, `cancelBookingSkipsConflictingWaitlistEntry`, `cancelBookingWithNoFittingWaitersPromotesNoOne`) in `BookingServiceTest.java`.
   - Implement waitlist lookup, sorting by `seq`, conflict checking, and single-waiter promotion in `BookingService.java`.
   - Run `mvn test` to verify.

3. **Step 3 (Sub-task 4 - Final Regression)**:
   - Execute `mvn clean test` to ensure all tests across the project pass cleanly.
