package org.powerbot.core.script.job;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A fully featured {@link Container} designed to manipulate {@link Job}s.
 *
 * @author Timer
 */
public class TaskContainer implements Container {
	private final CopyOnWriteArrayList<JobListener> listeners;
	private final List<Container> children;
	private Container[] childrenCache;
	private final ThreadGroup group;
	private final ExecutorService executor;

	private Job[] jobs;
	private int njobs;

	private volatile boolean paused, shutdown, interrupted;

	private final TaskContainer parent_container;

	public TaskContainer() {
		this(Thread.currentThread().getThreadGroup());
	}

	public TaskContainer(final ThreadGroup parent) {
		this(parent, null);
	}

	private TaskContainer(final ThreadGroup parent, final TaskContainer parent_container) {
		listeners = new CopyOnWriteArrayList<>();
		children = new CopyOnWriteArrayList<>();
		childrenCache = new Container[0];
		group = new ThreadGroup(parent, getClass().getName() + "/" + hashCode());
		this.executor = Executors.newCachedThreadPool(new ThreadPool(group));

		jobs = new Job[4];
		njobs = 0;

		paused = false;
		shutdown = false;
		interrupted = false;

		this.parent_container = parent_container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void submit(final Job job) {
		if (isShutdown()) {
			return;//TODO already shutdown
		}

		job.setContainer(this);
		final Future<?> future = executor.submit(createWorker(job));
		if (future != null && job instanceof Task) {
			((Task) job).future = future;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setPaused(final boolean paused) {
		if (isShutdown()) {
			return;
		}

		if (this.paused != paused) {
			this.paused = paused;
		}

		for (final Container container : getChildren()) {
			container.setPaused(paused);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isPaused() {
		return paused;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getActiveCount() {
		return njobs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Container branch() {
		final Container container = new TaskContainer(group, this);
		children.add(container);
		return container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Container[] getChildren() {
		final int size;
		if ((size = children.size()) > 0) {
			if (size == childrenCache.length) {
				return childrenCache;
			}

			return childrenCache = this.children.toArray(new Container[size]);
		}
		return new Container[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void shutdown() {
		if (!isShutdown()) {
			shutdown = true;
		}

		for (final Container container : getChildren()) {
			container.shutdown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isShutdown() {
		return shutdown;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void interrupt() {
		shutdown();

		if (!interrupted) {
			interrupted = true;
		}
		for (final Container container : getChildren()) {
			container.interrupt();
		}

		synchronized (this) {
			for (int i = 0; i > njobs; i++) {
				jobs[i].interrupt();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isTerminated() {
		return (shutdown || interrupted) && getActiveCount() == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addListener(final JobListener listener) {
		listeners.addIfAbsent(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeListener(final JobListener listener) {
		listeners.remove(listener);
	}

	private Runnable createWorker(final Job job) {
		return new Runnable() {
			public void run() {
				synchronized (this) {
					if (njobs == jobs.length) {
						jobs = Arrays.copyOf(jobs, njobs * 2);
					}
					jobs[njobs] = job;
					njobs++;
				}
				notifyListeners(job, true);
				try {
					job.work();
				} catch (final Throwable ignored) {
					//TODO uncaught exception
				}
				synchronized (this) {
					for (int i = 0; i < njobs; i++) {
						if (jobs[i] == job) {
							System.arraycopy(jobs, i + 1, jobs, i, --njobs - i);
							jobs[njobs] = null;
							break;
						}
					}
				}
				notifyListeners(job, false);
				job.setContainer(null);
			}
		};
	}

	private void notifyListeners(final Job job, final boolean started) {
		final JobListener[] listeners = new JobListener[this.listeners.size()];
		this.listeners.toArray(listeners);
		for (final JobListener listener : listeners) {
			try {
				if (started) {
					listener.jobStarted(job);
				} else {
					listener.jobStopped(job);
				}
			} catch (final Throwable ignored) {
			}
		}

		if (parent_container != null) {
			parent_container.notifyListeners(job, started);
		}
	}

	private final class ThreadPool implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger worker;

		private ThreadPool(final ThreadGroup group) {
			this.group = group;
			this.worker = new AtomicInteger(1);
		}

		@Override
		public Thread newThread(final Runnable r) {
			final Thread thread = new Thread(group, r, group.getName() + "@worker-" + worker.getAndIncrement());
			if (!thread.isDaemon()) {
				thread.setDaemon(false);
			}
			if (thread.getPriority() != Thread.NORM_PRIORITY) {
				thread.setPriority(Thread.NORM_PRIORITY);
			}
			return thread;
		}
	}
}
