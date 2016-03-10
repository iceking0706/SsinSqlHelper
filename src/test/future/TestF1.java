package test.future;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ssin.concurrent.myjobs.Job;
import ssin.concurrent.myjobs.JobAndFuture;
import ssin.concurrent.myjobs.JobProcessor;

public class TestF1 {
	public static void sleepIt(long mm){
		try {
			Thread.sleep(mm);
		} catch (InterruptedException e) {
			
		}
	}
	
	public static void test1(){
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		Future<Integer> future = threadPool.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				sleepIt(5000);
				throw new Exception("aaaaaaaaa");
				//return new Random().nextInt(100);
			}
		});
		
		while(!future.isDone()){
			System.out.println("is not done. cancelled="+future.isCancelled());
			sleepIt(1000);
		}
		
		System.out.println("future is complete");
		try {
			System.out.println("result="+future.get());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		threadPool.shutdown();
	}
	
	public static void test2(){
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		
		Job<String, String> job = new Job<String, String>("123456") {
			@Override
			public boolean process() {
				sleepIt(5000);
				setOutput(getInput()+",hello");
				return true;
			}
		};
		
		System.out.println(job.getJobID());
		
		Future<Boolean> future = threadPool.submit(job);
		
		JobAndFuture<String, String> jaf = new JobAndFuture<String, String>(job, future);
		
		while(!future.isDone()){
			System.out.println("wait....."+job.isComplete());
			sleepIt(500);
		}
		
		System.out.println(job.isSucc()+" , "+job.getOutput());
		
		
		threadPool.shutdown();
		
	};
	
	public static void test3(){
		JobProcessor<Integer, String> processor = new JobProcessor<Integer, String>();
		
		for(int i=0;i<100;i++){
			Job<Integer, String> job = new Job<Integer, String>(i) {

				@Override
				public boolean process() {
					// TODO Auto-generated method stub
					sleepIt(5000);
					setOutput("myoutput_"+getInput());
					System.out.println("Job finished. threadID="+Thread.currentThread().getId());
					return false;
				}
				
			};
			
			processor.submitJob(job);
		}
		
		
		sleepIt(30000);
		
		
		processor.unInit();
		System.out.println("11111111111111");
	}
	
	public static void test4(){
		JobProcessor<Integer, String> processor = new JobProcessor<Integer, String>();
		Job<Integer, String> job1 = new Job<Integer, String>(250,5000) {

			@Override
			public boolean process() {
				sleepIt(10000);
				return true;
			}
			
		};

		
		String jobId = processor.submitJob(job1);
		
		Job<Integer, String> job2 = processor.fetchJob(jobId);
		if(job2 != null){
			System.out.println(job2.getStatus()+", "+job2.getErrorInfo());
		}
		
		sleepIt(15000);
		
		
		processor.unInit();
		System.out.println("11111111111111");
	}
	
	public static void main(String[] args) {
		test4();
	}
}
