package v1;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Proxy Server is running now...");
		System.out.println("Initializing...");
		
		ProxyServer pServer = new ProxyServer(6003); //创建一个代理服务器
		if(pServer.pServerSocket == null) {
			System.err.println("Initializing failed for socket!");
			System.exit(0);
		}

		System.out.println("Proxy Server is running , the Monitor port is " + pServer.proxyPort);
		Socket pAcceptSocket = null; // 创建一个新套接字来与客户套接字创建连接通道
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();// 定义线程池
		
		while(true) { //代理服务器不断地监听
			CacheProxyParam lpProxyParam = new CacheProxyParam(pServer);//创建一个代理服务器的参数对象（与客户端通信的套接字和与服务器通信的套接字）
			pAcceptSocket = pServer.pServerSocket.accept(); // 监听请求，返回新的套接字
			lpProxyParam.pServerSocket_Client = pAcceptSocket;
			if(pServer.FilterUser(pAcceptSocket) != null) { //进行用户过滤
				cachedThreadPool.execute(new Thread(lpProxyParam));//开启新的线程
				Thread.sleep(200);
			}else {
				return;
			}
			
		}
	}

}
