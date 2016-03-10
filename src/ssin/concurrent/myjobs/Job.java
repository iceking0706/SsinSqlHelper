package ssin.concurrent.myjobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * 用户提交的一个任务 I: 任务开始时候的input数据对象 O: 任务完成后的结果对象
 * 
 * @author IcekingT420
 * 
 */
public abstract class Job<I, O> implements Callable<Boolean>{
	/**
	 * 任务的自动增编号
	 */
	private static long uniqueJobIDGnr = 0;

	/**
	 * 任务的ID格式
	 */
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMdd_HHmmss");

	/**
	 * 获得一个job的id
	 * 
	 * @return
	 */
	private static String gnrAJobId() {
		return dateFormat.format(new Date()) + "_" + (++uniqueJobIDGnr);
	}
	
	/**
	 * 4个状态：
	 * 0：创建
	 * 1：执行中
	 * 2：完成-成功: 根据process的返回值
	 * 3：完成-失败
	 * 4: 完成-异常 call中发生的异常，一般在process中获得到
	 */
	public static final int Status_Create = 0;
	public static final int Status_Processing = 1;
	public static final int Status_Complete_Succ = 2;
	public static final int Status_Complete_Fail = 3;
	public static final int Status_Complete_Exception = 4;
	
	/**
	 * 错误码定义：
	 * 0：正确
	 * 1：一般性错误
	 * 其它，可扩展
	 */
	public static final int ErrorCode_OK = 0;
	public static final int ErrorCode_Error = 1;
	

	/**
	 * 任务的编号，唯一，日期_时间_编号
	 */
	protected String jobID;
	
	/**
	 * 任务的类型
	 */
	protected String type;
	
	/**
	 * 任务的描述
	 * type和mark为用户自定义
	 */
	protected String mark;

	/**
	 * 任务的状态： 0: 创建，未执行 1：执行中 2：完成-成功 3：完成-失败  4：完成-异常
	 */
	protected int status;

	/**
	 * 如果异常完成了，其异常信息 0: 没有异常 可扩展
	 */
	protected int errorCode;
	/**
	 * 错误的信息描述
	 */
	protected String errorInfo;

	/**
	 * 任务的input，初始数据
	 */
	protected I input;

	/**
	 * 任务完成之后的结果
	 */
	protected O output;
	
	/**
	 * 可以设置超时时间
	 * >0表示超时
	 */
	protected long timeout;
	
	/**
	 * 任务开始处理的时间
	 */
	protected long processTime;

	/**
	 * 任务是否完成了
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return status > 1;
	}
	
	/**
	 * 判断是否成功完成了
	 * @return
	 */
	public boolean isSucc(){
		return isComplete() && status==Status_Complete_Succ;
	}
	
	/**
	 * 判断任务是否有错误，必须是完成之后的
	 * 
	 * @return
	 */
	public boolean hasError() {
		return isComplete() && errorCode > 0;
	}
	
	/**
	 * 该任务是否需要判断超时的
	 * @return
	 */
	public boolean checkTimeout(){
		return timeout > 0l;
	}
	
	/**
	 * 判断该任务是否超时了
	 * @return
	 */
	public boolean isTimeout(){
		//不需要判断超时，或者任务没有启动，则表示未超时
		if(!checkTimeout() || processTime == 0l)
			return false;
		return (System.currentTimeMillis()-processTime)>=timeout;
	}
	
	/**
	 * 标记为超时了
	 */
	public void doTimeout(){
		status = Status_Complete_Exception;
		errorCode = ErrorCode_Error;
		errorInfo = "Job Timeout";
	}
	
	/**
	 * 获取处理的耗时
	 * @return
	 */
	public long timeConsume(){
		if(!checkTimeout() || processTime == 0l)
			return 0l;
		return System.currentTimeMillis()-processTime;
	}
	
	public Job(){
		this.jobID = gnrAJobId();
		this.status = Status_Create;
		this.errorCode = ErrorCode_OK;
		this.timeout = 0l;
		this.processTime = 0l;
	}

	/**
	 * 构造函数，产生任务的编号
	 */
	public Job(I input) {
		this();
		this.input = input;
	}
	
	public Job(I input,long timeout) {
		this();
		this.input = input;
		this.timeout = timeout;
	}
	
	@Override
	public Boolean call() throws Exception {
		//设置为执行中
		status = Status_Processing;
		processTime = System.currentTimeMillis();
		try {
			boolean flag = process();
			status = flag?Status_Complete_Succ:Status_Complete_Fail;
			return flag;
		} catch (Exception e) {
			//发生了异常
			status = Status_Complete_Exception;
			errorCode = ErrorCode_Error;
			errorInfo = e.getMessage();
			return false;
		}
	}
	
	/**
	 * 根据input处理得到output的结果
	 */
	public abstract boolean process();

	public String getJobID() {
		return jobID;
	}

	public int getStatus() {
		return status;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public I getInput() {
		return input;
	}

	public O getOutput() {
		return output;
	}

	public void setOutput(O output) {
		this.output = output;
	}

	public void setInput(I input) {
		this.input = input;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

}
