package test;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ssin.sqlhelper.SsinDatabaseFactory;
import ssin.sqlhelper.SsinLoggerFactory;
import ssin.sqlhelper.config.ConfigXML;
import test.tables.TbTmp1;


public class TestMultiSqlite {
public static void test1(){
		
		ConfigXML configXML = new ConfigXML();
		SsinLoggerFactory.setConfigXML(configXML);
		final SsinDatabaseFactory dbFactory = new SsinDatabaseFactory(configXML);
		
		final Random random = new Random();
		
		ExecutorService threadPool = Executors.newCachedThreadPool();
		
		for(int i=0;i<20;i++){
			threadPool.execute(new Runnable() {
				public void run() {
					
					long millis = (random.nextInt(6)+1)*1000;
					sleepIt(millis);
					
					dbFactory.startTransaction();
					TbTmp1 po = new TbTmp1();
					po.setStr1(Thread.currentThread().getName());
					po.setStr2("content: "+System.currentTimeMillis());
					po = dbFactory.getSqlOper().save(po);
					if(po == null){
						System.out.println("Save fail. Thread_"+Thread.currentThread().getId()+", sleep: "+millis);
						dbFactory.rollback();
					}else {
						System.out.println("Save succ. id="+po.getId()+". Thread_"+Thread.currentThread().getId()+", sleep: "+millis);
						dbFactory.commit();
					}
				}
			});
		}
		
		sleepIt(50000);
		
		System.out.println("all finished.");
		threadPool.shutdown();
		dbFactory.unInit();
		
	}
	
	private static void sleepIt(long millis){
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
		}
	}
	
	public static void main(String[] args) {
		test1();
//		String customInfo = "中文字符";
//		String str = ByteUtils.byteArrayToHex(customInfo.getBytes());
//		System.out.println(str);
	}
}
