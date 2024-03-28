package cat.michal.catbase.common.data;

import cat.michal.catbase.common.data.time.SystemNanoTimeSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ListKeeper {

    private ListKeeper() {
        this.timer = new Timer(true);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                houseKeep();
            }
        }, 5 * 60 * 1000, 5 * 60 * 1000);
    }

    private final Timer timer;

    private static final AtomicInteger threadId = new AtomicInteger(0);
    private static ListKeeper instance;
    private final List<WeakReference<TimedList<?>>> lists = new ArrayList<>();
    private final ExecutorService keeperPool = new ThreadPoolExecutor(0, 10,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("List-Keeper-Worker-" + threadId.incrementAndGet());
        return thread;
    });

    public static ListKeeper getInstance() {
        if (instance == null) {
            instance = new ListKeeper();
        }
        return instance;
    }

    public <V> TimedList<V> createDefaultTimeList() {
        var list = new TimedList<V>()
                .setTimeout(TimeUnit.NANOSECONDS.convert(5, TimeUnit.MINUTES))
                .setTimeSource(new SystemNanoTimeSource());
        lists.add(new WeakReference<>(list));
        return list;
    }

    public void houseKeep() {
        lists.removeIf(it -> it.get() == null);
        lists.forEach(it -> {
            var list = it.get();
            if (list != null) {
                keeperPool.submit(list::removeStale);
            }
        });
    }

    public void shutdown() {
        timer.cancel();
        keeperPool.shutdown();
    }
}
