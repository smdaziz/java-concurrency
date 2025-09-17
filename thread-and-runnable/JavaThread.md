**When you do:**

```
Thread thread = new Thread();
thread.start();
```

- there is **no separate “target”** (`Runnable`) provided, 
- the `Thread` instance itself is the **actual thread** that will run.

**So is it target or wrapper?**
- it is **just a thread** — not a wrapper, not a separate target.
- in this case, the `Thread` object is _both_:
  - the thread being started (the “wrapper”), **and** 
  - the runnable target (because `Thread` implements `Runnable` and its default `run()` does nothing).

**What happens at runtime**
- `thread.start()` tells the JVM to create a new OS-level thread, and then call `thread.run()`.
- since you didn’t override `run()` or pass in a `Runnable`, nothing happens.

**✅ Rule of thumb:**
- **Wrapper thread** → when you create a new `Thread(runnable)` and the runnable supplies the `run()` code. 
- **Target thread** → when the thread itself is the runnable (subclass of `Thread`, or default `Thread` with empty `run()`). 
- **Your example** → `thread` is a plain thread with no target, so it’s best described as _just a thread object_ that runs but does nothing.
