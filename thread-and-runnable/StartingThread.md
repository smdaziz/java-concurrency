## Directly starting a subclass vs. wrapping a subclass inside another `Thread`

| Aspect | Directly start the subclass instance | Wrap the subclass instance inside a new `Thread` |
| --- | --- | --- |
| What you create | One subclass instance (the thread itself) | Two objects: an **outer** wrapper thread and an **inner** subclass instance used as a **target** |
| Which thread actually runs | The subclass instance becomes the running thread | The **outer** wrapper thread runs; it invokes the **inner** target's `run()` **synchronously** |
| Does the inner subclass get started? | Yes (it is the thread) | No. It is **not** started; its `run()` is merely called by the wrapper |
| If the subclass overrides `run()` | That code runs on the subclass thread; its own name is the current thread name | That code runs on the **wrapper** thread; the current thread name is the wrapper's name (not the inner's) |
| If the subclass does **not** override `run()` | Thread starts and does nothing (default `run()` is no-op) | Wrapper starts and calls the inner's default `run()` (no-op) → also does nothing |
| Thread naming you'll see (typical) | Current thread name = the subclass instance's name | Current thread name = wrapper's name (default like "Thread-N" unless you set it); the inner may still *have* a name, but it's not the running thread |
| Clarity/intent | Clear: "this object is the thread" | Confusing: "a Thread is used as a Runnable target for another Thread" |

-------------------------------

## Which is recommended? (and why)

### For these two variants
- **Recommended between the two:** **Directly start the subclass** *if* you already chose to extend `Thread`.
It's clearer and avoids the confusing "Thread-as-Runnable" pattern.
- **Avoid:** wrapping a `Thread` subclass inside another `Thread`. It obscures which thread is actually running and offers no benefit.

### In general (best practice)
- Prefer **composition** over inheritance: implement `Runnable`/`Callable` and run it via:
- a new thread (for simple demos), or better,
- an **executor** (fixed pool, scheduled executor, or **virtual threads** on modern Java).
- Benefits: cleaner separation of "what to run" (task) from "how it runs" (thread/executor), easier testing, easier scaling, safer lifecycle management.

--------------------

## Quick rules of thumb
- If you **extend `Thread`**, **start that instance directly**; don't wrap it inside another `Thread`.
- If you **don't need to customize the thread type**, **don't extend `Thread`** at all---write a `Runnable`/`Callable` and submit it to an executor (or start a thread with that task for learning purposes).
- A `Thread` object used as another `Thread`'s target does **not** become a running thread; its `run()` is just invoked by the wrapper.
