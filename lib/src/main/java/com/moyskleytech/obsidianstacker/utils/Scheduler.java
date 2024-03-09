package com.moyskleytech.obsidianstacker.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public abstract class Scheduler {
    public static interface Task {
        void cancel();
    }

    private static Scheduler instance;

    public static Scheduler getInstance() {
        if (instance == null) {
            if (isFolia()) {
                instance = new FoliaScheduler();
            } else
                instance = new BukkitScheduler();
        }
        return instance;
    }

    public static class FoliaScheduler extends Scheduler {
        public static class FoliaTask implements Task {
            ScheduledTask data;

            public FoliaTask(ScheduledTask innerTask) {
                data = innerTask;
            }

            public FoliaTask(Void innerTask) {
                data = null;
            }

            @Override
            public void cancel() {
                data.cancel();
            }

        }

        @Override
        public void runTask(Plugin p, Runnable r) {
            Bukkit.getServer().getGlobalRegionScheduler().execute(p, r);
        }

        @Override
        public Task runEntityTask(Plugin p, Entity e, long delay, Runnable r) {
            if (delay <= 0)
                return new FoliaTask(e.getScheduler().run(p, (task) -> {
                    r.run();
                }, null));
            if (e != null)
                return new FoliaTask(e.getScheduler().runDelayed(p, (task) -> {
                    r.run();
                }, null, delay));
            return null;
        }

        @Override
        public Task runTaskAsync(Plugin p, Consumer<Task> r) {
            final FoliaTask st = new FoliaTask(
                    Bukkit.getServer().getAsyncScheduler().runNow(p, (task) -> {
                        r.accept(null);
                    }));
            return st;
        }

        @Override
        public Task runChunkTask(Plugin p, Location e, long delay, Runnable r) {
            if (delay == 0) {
                Bukkit.getServer().getRegionScheduler().execute(p, e, () -> {
                    r.run();
                });
                return null;
            }
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getRegionScheduler().runDelayed(p, e, (task) -> {
                        r.run();
                    }, delay));
        }

        @Override
        public Task runChunkTask(Plugin p, Chunk c, long delay, Runnable r) {
            Location center = new Location(c.getWorld(), c.getX() << 4, 64, c.getZ() << 4);
            return runChunkTask(p, center, delay, r);
        }

        @Override
        public Task runTaskLater(Plugin p, Runnable r, long delay) {
            if (delay <= 0) {
                Bukkit.getServer().getGlobalRegionScheduler().execute(p, r);
                return null;
            }
            return new FoliaTask(
                    Bukkit.getServer().getGlobalRegionScheduler().runDelayed(p, (task) -> {
                        r.run();
                    }, delay));
        }

        @Override
        public Task runTaskTimer(Plugin p, Consumer<Task> r, long delay, long period) {
            if (delay <= 0) {
                delay = 1;
            }
            return new FoliaTask(
                    Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(p, (task) -> {
                        r.accept(new FoliaTask(task));
                    }, delay, period));
        }

        @Override
        public Task runTaskTimerAsync(Plugin p, Consumer<Task> r, long delay, long period) {
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getAsyncScheduler().runAtFixedRate(p, (task) -> {
                        r.accept(new FoliaTask(task));
                    }, (long) (delay * 50), (long) (period * 50), TimeUnit.MILLISECONDS));
        }

        @Override
        public Task runEntityTaskTimer(Plugin p, Entity e, long delay, long prerio, Consumer<Task> r) {
            if (delay <= 0)
                delay = 1;
            if (e != null)
                return new FoliaTask(e.getScheduler().runAtFixedRate(p, (task) -> {
                    r.accept(new FoliaTask(task));
                }, null, delay, prerio));
            return null;
        }

        @Override
        public Task runChunkTaskTimer(Plugin p, Location e, long delay, long preriod, Consumer<Task> r) {
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getRegionScheduler().runAtFixedRate(p, e, (task) -> {
                        r.accept(new FoliaTask(task));
                    }, delay, preriod));
        }

        @Override
        public Task runChunkTaskTimer(Plugin p, Chunk c, long delay, long preriod, Consumer<Task> r) {
            Location center = new Location(c.getWorld(), c.getX() << 4, 64, c.getZ() << 4);
            return runChunkTaskTimer(p, center, delay, preriod, r);
        }

    }

    @SuppressWarnings("deprecation")
    public static class BukkitScheduler extends Scheduler {
        public static class NormalTask implements Task {
            BukkitTask data;

            public NormalTask(BukkitTask innerTask) {
                data = innerTask;
            }

            @Override
            public void cancel() {
                data.cancel();
            }

        }

        @Override
        public Task runChunkTask(Plugin p, Chunk c, long delay, Runnable r) {
            Location center = new Location(c.getWorld(), c.getX() << 4, 64, c.getZ() << 4);
            return runChunkTask(p, center, delay, r);
        }

        @Override
        public Task runChunkTaskTimer(Plugin p, Chunk c, long delay, long preriod, Consumer<Task> r) {
            Location center = new Location(c.getWorld(), c.getX() << 4, 64, c.getZ() << 4);
            return runChunkTaskTimer(p, center, delay, preriod, r);
        }

        public static class RunnableTask implements Task {
            BukkitRunnable data;

            public RunnableTask(BukkitRunnable innerTask) {
                data = innerTask;
            }

            @Override
            public void cancel() {
                data.cancel();
            }

        }

        @Override
        public void runTask(Plugin p, Runnable r) {
            Bukkit.getScheduler().runTask(p, r);
        }

        @Override
        public Task runEntityTask(Plugin p, Entity e, long delay, Runnable r) {
            return this.runTaskLater(p, r, delay);
        }

        @Override
        public Task runTaskAsync(Plugin p, Consumer<Task> r) {
            NormalTask nt = new NormalTask(
                    Bukkit.getScheduler().runTaskAsynchronously(p, () -> {
                        r.accept(null);
                    }));
            return nt;
        }

        @Override
        public Task runChunkTask(Plugin p, Location e, long delay, Runnable r) {
            if (delay <= 0) {
                r.run();
                return null;
            }
            return this.runTaskLater(p, r, delay);
        }

        @Override
        public Task runTaskLater(Plugin p, Runnable r, long delay) {
            return new NormalTask(Bukkit.getScheduler().runTaskLater(p, r, delay));
        }

        @Override
        public Task runTaskTimer(Plugin p, Consumer<Task> r, long delay, long period) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    r.accept(new RunnableTask(this));
                }
            };
            return new NormalTask(runnable.runTaskTimer(p, delay, period));
        }

        @Override
        public Task runTaskTimerAsync(Plugin p, Consumer<Task> r, long delay, long period) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    r.accept(new RunnableTask(this));
                }
            };
            return new NormalTask(runnable.runTaskTimerAsynchronously(p, delay, period));
        }

        @Override
        public Task runEntityTaskTimer(Plugin p, Entity e, long delay, long prerio, Consumer<Task> r) {
            return runTaskTimer(p, r, delay, prerio);
        }

        @Override
        public Task runChunkTaskTimer(Plugin p, Location e, long delay, long preriod, Consumer<Task> r) {
            return runTaskTimer(p, r, delay, preriod);
        }

    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public abstract void runTask(Plugin p, Runnable r);

    public abstract Task runTaskLater(Plugin p, Runnable r, long delay);

    public abstract Task runTaskAsync(Plugin p, Consumer<Task> r);

    public abstract Task runTaskTimer(Plugin p, Consumer<Task> r, long delay, long period);

    public abstract Task runTaskTimerAsync(Plugin p, Consumer<Task> r, long delay, long period);

    public abstract Task runEntityTask(Plugin p, Entity e, long delay, Runnable r);

    public abstract Task runEntityTaskTimer(Plugin p, Entity e, long delay, long prerio, Consumer<Task> r);

    public abstract Task runChunkTask(Plugin p, Location e, long delay, Runnable r);

    public abstract Task runChunkTask(Plugin p, Chunk e, long delay, Runnable r);

    public abstract Task runChunkTaskTimer(Plugin p, Location e, long delay, long preriod, Consumer<Task> r);

    public abstract Task runChunkTaskTimer(Plugin p, Chunk e, long delay, long preriod, Consumer<Task> r);

}
