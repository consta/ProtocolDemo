package com.ccl.protocoldemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtils {
	private ExecutorService service;

	private ThreadPoolUtils() {
		int num = Runtime.getRuntime().availableProcessors();
		service = Executors.newFixedThreadPool(num * 2);
		service = Executors.newCachedThreadPool();
	}

	private static final ThreadPoolUtils threadPool = new ThreadPoolUtils();

	public static ThreadPoolUtils getInstance() {
		return threadPool;
	}

	public void addTask(Runnable runnable) {
		service.execute(runnable);
	}
}
