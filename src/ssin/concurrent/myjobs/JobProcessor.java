package ssin.concurrent.myjobs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



/**
 * 任务job的服务方，提供多线程的任务服务
 * @author IcekingT420
 *
 */
public class JobProcessor<I, O> {
	/**
	 * 线程池的大小，可以设置，默认50
	 */
	private int poolSize = 50;
	
	/**
	 * 线程池
	 */
	private ExecutorService threadPool;
	
	/**
	 * 任务的map，
	 * k: job的ID
	 * v: JobAndFuture
	 */
	private ConcurrentMap<String, JobAndFuture<I, O>> map;
	
	public JobProcessor(){
		init();
	}
	
	public JobProcessor(int poolSize){
		this.poolSize = poolSize>0?poolSize:50;
		init();
	}
	
	private void init(){
		threadPool = Executors.newFixedThreadPool(poolSize);
		map = new ConcurrentHashMap<String, JobAndFuture<I, O>>();
	}
	
	public void unInit(){
		map.clear();
		threadPool.shutdown();
	}
	
	/**
	 * 提交一个任务
	 * 返回这个任务的ID号
	 * @return
	 */
	public String submitJob(Job<I, O> job){
		//判断该任务是否已经提交了
		if(map.containsKey(job.getJobID()))
			return job.getJobID();
		
		//线程运行该任务
		Future<Boolean> future = threadPool.submit(job);
		//保存到map中
		map.put(job.getJobID(), new JobAndFuture<I, O>(job, future));
		
		return job.getJobID();
	}
	
	/**
	 * 判断任务是否已经完成了
	 * 完成的任务再从map中获取出来
	 * @param jobID
	 * @return
	 */
	public boolean isJobComplete(String jobID){
		if(map.isEmpty())
			return false;
		 JobAndFuture<I, O> jaf = map.get(jobID);
		 if(jaf == null)
			 return false;
		 
		 //任务还在处理中，要判断 一下是否超时了
		 if(!jaf.getFuture().isDone()){
			 if(jaf.getJob().checkTimeout() && jaf.getJob().isTimeout()){
				 jaf.getJob().doTimeout();
				 jaf.getFuture().cancel(true);
				 return true;
			 }
			 return false;
		 }
			 
		 return true;
	}
	
	/**
	 * 获得一个任务
	 * @param jobID
	 * @return
	 */
	public Job<I, O> getJob(String jobID){
		if(map.isEmpty())
			return null;
		 JobAndFuture<I, O> jaf = map.get(jobID);
		 if(jaf == null)
			 return null;
		 return jaf.getJob();
	}
	
	/**
	 * 删除一个任务
	 * @param jobID
	 * @return
	 */
	public Job<I, O> removeJob(String jobID){
		if(map.isEmpty())
			return null;
		 JobAndFuture<I, O> jaf = map.remove(jobID);
		 if(jaf == null)
			 return null;
		 return jaf.getJob();
	}
	
	/**
	 * 获得任务，一直等待，知道拿到为止
	 * @param jobID
	 * @return
	 */
	public Job<I, O> fetchJob(String jobID){
		if(map.isEmpty())
			return null;
		 JobAndFuture<I, O> jaf = map.remove(jobID);
		 if(jaf == null)
			 return null;
		 while(!jaf.getFuture().isDone()){
			 sleepIt(200);
			 if(jaf.getJob().checkTimeout() && jaf.getJob().isTimeout()){
				 jaf.getJob().doTimeout();
				 jaf.getFuture().cancel(true);
				 break;
			 }
		 }
		 return jaf.getJob();
	}

	public int getPoolSize() {
		return poolSize;
	}
	
	/**
	 * 线程休眠
	 * @param mm
	 */
	public void sleepIt(long mm){
		try {
			Thread.sleep(mm);
		} catch (InterruptedException e) {
			
		}
	}
}
