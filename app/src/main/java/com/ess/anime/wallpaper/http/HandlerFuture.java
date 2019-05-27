package com.ess.anime.wallpaper.http;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;

import java.util.Objects;

public class HandlerFuture<T> {

    private static final String TAG = "Handler";
    private HandlerThread workThread;

    private T value;

    private IO io;

    private Handler.Callback pending;

    private final Stop stop;


    private HandlerFuture(T value, IO io, Stop stop) {
        this.value = value;
        this.io = io;
        this.stop = stop;
    }


    public static <V> HandlerFuture<V> ofUI(V value) {
        return new HandlerFuture<>(value, IO.UI, new Stop());
    }

    public static <V> HandlerFuture<V> ofWork(V value) {
        return new HandlerFuture<>(value, IO.WORK, new Stop());
    }


    public HandlerFuture<T> runOn(IO io) {
        this.io = io;
        return this;
    }

    public <V> HandlerFuture<V> applyThen(Function<T, V> fun) {
        return applyThen(fun, throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    public <V> HandlerFuture<V> applyThen(Function<T, V> fun, Function<Throwable, ? extends V> except) {
        Objects.requireNonNull(fun);
        Objects.requireNonNull(except);
        IO workIs = io == IO.WORK ? IO.WORK : IO.UI;
        HandlerFuture<V> future = new HandlerFuture<>(null, workIs, stop);
        //构建一个任务
        FunctionTask<V> task = new FunctionTask<>(future, val -> {
            try {
                return fun.apply(val);
            } catch (Exception e) {
                return except.apply(e);
            }
        });
        if (value != null) {
            //如果当前有值则直接执行任务
            executeTask(task);
        } else {
            //否则延期直到可以执行任务的时候在执行
            pending = task;
        }
        return future;
    }

    public HandlerFuture<Void> applyThen(Consumer<T> fun) {
        return applyThen(fun, throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    public HandlerFuture<Void> applyThen(Consumer<T> fun, Function<Throwable, ? extends Void> except) {
        Objects.requireNonNull(fun);
        Objects.requireNonNull(except);
        IO workIs = io == IO.WORK ? IO.WORK : IO.UI;
        HandlerFuture<Void> future = new HandlerFuture<>(null, workIs, stop);
        //构建一个任务
        ConsumerTask task = new ConsumerTask(future, val -> {
            try {
                fun.accept(val);
            } catch (Exception e) {
                except.apply(e);
            }
        });
        if (value != null) {
            //如果当前有值则直接执行任务
            executeTask(task);
        } else {
            //否则延期直到可以执行任务的时候在执行
            pending = task;
        }
        return future;
    }

    public <V> HandlerFuture<V> applyThen(Supplier<V> fun) {
        return applyThen(fun, throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    public <V> HandlerFuture<V> applyThen(Supplier<V> fun, Function<Throwable, ? extends V> except) {
        Objects.requireNonNull(fun);
        Objects.requireNonNull(except);
        IO workIs = io == IO.WORK ? IO.WORK : IO.UI;
        HandlerFuture<V> future = new HandlerFuture<>(null, workIs, stop);
        //构建一个任务
        SupplierTask<V> task = new SupplierTask<>(future, () -> {
            try {
                return fun.get();
            } catch (Exception e) {
                return except.apply(e);
            }
        });
        if (value != null) {
            //如果当前有值则直接执行任务
            executeTask(task);
        } else {
            //否则延期直到可以执行任务的时候在执行
            pending = task;
        }
        return future;
    }


    public void exits() {
        stop.stop = true;
    }

    public void complete(T value) {
        if (stop.stop || value == null) {
            return;
        }
        this.value = value;
        if (pending != null) {
            executeTask(pending);
        }
    }

    private void closeThreadIfExist() {
        if (workThread != null && workThread.isAlive()) {
            workThread.quit();
            workThread = null;
        }
    }


    private void executeTask(Handler.Callback callback) {
        if (io == IO.WORK && workThread == null) {
            workThread = new HandlerThread("asyncHandler");
            workThread.start();
        }
        Handler handler = new Handler(io == IO.UI ? Looper.getMainLooper() : workThread.getLooper(), callback);
        Message message = new Message();
        message.setTarget(handler);
        message.sendToTarget();
    }


    private class FunctionTask<V> implements Handler.Callback {

        private Function<T, V> fun;
        private HandlerFuture<V> chain;

        FunctionTask(HandlerFuture<V> chain, Function<T, V> fun) {
            this.chain = chain;
            this.fun = fun;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (!stop.stop) {
                V obj = fun.apply(value);
                //现在可以执行下一个任务
                chain.complete(obj);
            }
            closeThreadIfExist();
            return true;
        }

    }

    private class ConsumerTask implements Handler.Callback {

        private Consumer<T> fun;
        private HandlerFuture<Void> chain;

        ConsumerTask(HandlerFuture<Void> after, Consumer<T> fun) {
            this.chain = after;
            this.fun = fun;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (!stop.stop) {
                fun.accept(value);
                //现在可以执行下一个任务
                chain.complete(Void.TYPE.cast(null));
            }
            closeThreadIfExist();
            return true;
        }


    }

    private class SupplierTask<V> implements Handler.Callback {

        private Supplier<V> fun;
        private HandlerFuture<V> chain;

        SupplierTask(HandlerFuture<V> chain, Supplier<V> fun) {
            this.chain = chain;
            this.fun = fun;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (!stop.stop) {
                V obj = fun.get();
                //现在可以执行下一个任务
                chain.complete(obj);
            }
            closeThreadIfExist();
            return true;
        }

    }

    private static class Stop {
        private boolean stop;
    }

    public enum IO {
        /**
         * 使任务运行在工作线程，不会堵塞UI线程
         */
        WORK,
        /**
         * 使任务运行在UI线程
         */
        UI
    }
}