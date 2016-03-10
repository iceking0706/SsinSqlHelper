package ssin.concurrent.myjobs;

import java.util.concurrent.Future;

/**
 * 任务和处理的对象
 * 
 * @author IcekingT420
 * 
 */
public class JobAndFuture<I, O> {
	private Job<I, O> job;
	private Future<Boolean> future;

	public JobAndFuture() {
		
	}

	public JobAndFuture(Job<I, O> job, Future<Boolean> future) {
		this.job = job;
		this.future = future;
	}

	public Job<I, O> getJob() {
		return job;
	}

	public void setJob(Job<I, O> job) {
		this.job = job;
	}

	public Future<Boolean> getFuture() {
		return future;
	}

	public void setFuture(Future<Boolean> future) {
		this.future = future;
	}
}
