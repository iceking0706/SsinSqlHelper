package ssin.sqlhelper;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * 获得本机的IP地址
 * @author IcekingT420
 *
 */
public class LocalMacAddr {
	
	/**
	 * 只查询一次的
	 */
	private static boolean init;
	
	/**
	 * mac地址
	 */
	private static String mac;
	/**
	 * 当前的ip地址
	 */
	private static String ip;
	/**
	 * 主机名字
	 */
	private static String host;
	
	/**
	 * 获取地址信息
	 */
	private static void initOper(){
		if(init)
			return;
		try {
			InetAddress ia = InetAddress.getLocalHost();
			if(ia == null)
				return;
			ip = ia.getHostAddress();
			host = ia.getHostName();
			byte[] array = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			if(array == null || array.length==0)
				return;
			StringBuffer sb = new StringBuffer("");
			for(int i=0; i<array.length; i++) {
				if(i!=0) {
					sb.append("-");
				}
				//字节转换为整数
				int temp = array[i]&0xff;
				String str = Integer.toHexString(temp);
				if(str.length()==1) {
					sb.append("0"+str);
				}else {
					sb.append(str);
				}
			}
			
			mac = sb.toString().toUpperCase();
			
			init = true;
		} catch (Exception e) {
			e.printStackTrace();
			init = false;
		}
	}

	public static String getMac() {
		initOper();
		return mac;
	}

	public static String getIp() {
		initOper();
		return ip;
	}

	public static String getHost() {
		initOper();
		return host;
	}
	
	/*private static void getLocalMac(InetAddress ia) throws SocketException {
		// TODO Auto-generated method stub
		//获取网卡，获取地址
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		System.out.println("mac数组长度："+mac.length);
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<mac.length; i++) {
			if(i!=0) {
				sb.append("-");
			}
			//字节转换为整数
			int temp = mac[i]&0xff;
			String str = Integer.toHexString(temp);
			System.out.println("每8位:"+str);
			if(str.length()==1) {
				sb.append("0"+str);
			}else {
				sb.append(str);
			}
		}
		System.out.println("本机MAC地址:"+sb.toString().toUpperCase());
	}
	
	public static void main(String[] args) {
		try {
			InetAddress ia = InetAddress.getLocalHost();
			
			System.out.println("ia="+ia);
			System.out.println(ia.getHostAddress());
			System.out.println(ia.getHostName());
			getLocalMac(ia);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/
}
