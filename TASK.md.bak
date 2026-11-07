# Milestone 2 Task: Add `cancelBooking`

The service can book and list, but there is no way to cancel a booking. Add one:

```java
public void cancelBooking(String bookingId)
```

## Behavior

- Remove the confirmed booking with that id, freeing its slot.
- If no booking has that id, do nothing.
- After removing it, **promote from the waitlist.** The room may have users waiting.
  Promote the earliest-waiting user (first-come-first-served, by `seq`) whose interval
  is now free (does not overlap any remaining confirmed booking) into a confirmed
  booking, and remove their waitlist entry.
- Promote **at most one** user per cancellation.
- Promotion must not create an overlap. If the earliest waiter's interval still
  conflicts with another booking, skip them and consider the next, and so on. If none
  fit, promote no one.

The store already gives you what you need (`findBooking`, `removeBooking`,
`addBooking`, `waitlistForRoom`, and `removeWaitlistEntry`).

## How to work this milestone

- Use an agent from your AI tool to write the method. Prompt it, then **read its diff before
  you accept it.** Pay attention to whether it handled the promotion, not just the
  removal.
- Implement the change as a few small, reviewable commits, not one large dump.
- Add a test that proves promotion works. Book a slot for one user, put a second user
  on the waitlist for an overlapping slot, cancel the first booking, and confirm the
  second user now holds a confirmed booking.

You will show your TA the commit series and the test you used to verify the change.
