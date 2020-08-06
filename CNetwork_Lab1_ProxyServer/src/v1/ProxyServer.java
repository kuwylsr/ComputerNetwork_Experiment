package v1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ProxyServer {
	ServerSocket pServerSocket = null; // 代理服务器的主套接字
	int proxyPort; // 代理服务器的端口号
	
	
	public ProxyServer(int port) {
		try {
			this.proxyPort = port;
			// 第一个参数：套接字绑定的端口号；第二个参数：最大连接并发数；第三个参数：被绑定的当地IP地址
			this.pServerSocket = new ServerSocket(this.proxyPort, 50, null);
		} catch (IOException e) {
			System.err.println("创建代理服务器主套接字失败！");
			e.printStackTrace();
		}
	}
	
	/**
	 * 解析TCP报文中的Http头部
	 * @param in
	 * @param httpHeader
	 * @throws IOException 
	 */
	public String ParseHttpHead(BufferedReader in,HttpHeader httpHeader) throws IOException {
		
		String line = null;
		String data_fromClient = ""; //来自客户机的数据报信息
		//继续获取 host cookie等信息
		while((line = in.readLine()) != null && line.length() != 0) {
			data_fromClient = data_fromClient + line +("\r\n");
			// 获取url地址
			if(line.contains("GET")||line.contains("POST")||line.contains("CONNECT")) {
				String resource[] = line.split(" ");
				httpHeader.url = resource[1];
				// 获取method方法
				httpHeader.method = resource[0];
			}else if(line.contains("Host")) {
				String resource[] = line.split(" ");
				httpHeader.host = resource[1];
			}else if(line.contains("Cookie")) {
				String resource[] = line.split(" ");
				httpHeader.cookie = resource[1];
			}
			
		}
		
		return data_fromClient+("\r\n");
	}
	/**
	 * 构造条件性GET的报文段
	 * @param data_fromClient
	 * @param in
	 * @return 条件性GET报文
	 * @throws IOException
	 */
	public String MakeConditionalGET(String data_fromClient,BufferedReader in) throws IOException {
		String data_ConditionalGET = "";
		String IfModifiedSince = "";
		String line;
		int flag = 0;
		while((line = in.readLine()) != null) {
			if(line.contains("Last-Modified")) {
				flag = 1;
				IfModifiedSince = line.replace("Last-Modified", "If-Modified-Since");
				break;
			}
		}
		if(flag == 0) {
			return "NoDataModified";
		}
		String resource[] = data_fromClient.split("\r\n");
		data_ConditionalGET = data_ConditionalGET + resource[0] +("\r\n");
		data_ConditionalGET = data_ConditionalGET + resource[1] +("\r\n");
		data_ConditionalGET = data_ConditionalGET + IfModifiedSince+("\r\n");
		return data_ConditionalGET + ("\r\n");
	}
	
	/**
	 * 解析服务器响应报文中的日期字段与缓存中的日期对比
	 * @param data_fromServer
	 * @return 缓存中日期是否为最新
	 */
	public String ParseLastModified(byte[] data_fromServer) {
		String data = new String(data_fromServer);
		String temp = "";
		for(int i =0 ; i < data.length() ; i++) {
			char ch = data.charAt(i);
			if(ch != '\r' && ch != '\n') {
				temp = temp + ch;
			}else {
				break;
			}
		}
		String resource[] = temp.split(" ");
		if(resource[1].equals("304")) {
			return "NoChange";
		}
		return "Change";
	}
	
	/**
	 * 过滤指定的网站
	 * @param httpHeader
	 * @return 是否被过滤
	 */
	public String FilterWebSite(HttpHeader httpHeader) {
		if(httpHeader.url.toLowerCase().contains("google")||httpHeader.url.toLowerCase().contains("eclipse")||httpHeader.url.toLowerCase().contains("baidu")) {
			System.out.println("不允许的url:"+httpHeader.url+"已被过滤！"+"\r\n");
			return null;
		}
		if(httpHeader.method.toLowerCase().equals("connect")) {
			System.out.println("不允许的方法method:"+httpHeader.method+"已被过滤！"+"\r\n");
			return null;
		}
		return "normal";
	}
	
	/**
	 * 过滤指定的用户
	 * @param pAcceptSocket
	 * @return 是否被过滤
	 */
	public String FilterUser(Socket pAcceptSocket) {
		String hostIP = pAcceptSocket.getInetAddress().getHostAddress();
		if(hostIP.equals("x.x.x.x")) {
			System.out.println("不允许的用户IP:"+"+hostIP+"+"已被过滤！"+"\r\n");
			return null;
		}
		return "normal";
	}
	
	/**
	 * 进行钓鱼
	 * @param pServerSocket_Client
	 * @param httpHeader
	 * @return 是否被钓鱼
	 * @throws IOException
	 */
	public String GoFishing(Socket pServerSocket_Client, HttpHeader httpHeader) throws IOException{
		String gofishweb = "http://today.hit.edu.cn/"; //定向的网站
		DataOutputStream out_fishing = new DataOutputStream(pServerSocket_Client.getOutputStream());
		if(httpHeader.url.equals("http://www.sogou.com/")) {
			System.out.println("====="+"针对:"+httpHeader.url+"=====");
			System.out.println("=====被钓鱼到:"+gofishweb+"=====");
			String data_Fishing = "";
			data_Fishing = data_Fishing + "HTTP/1.1 302 Moved Temporarily" + "\r\n";
			data_Fishing =data_Fishing + "Location: " + gofishweb + "\r\n" + "\r\n";
			out_fishing.writeBytes(data_Fishing);
			return null;
		}else {
			return "normal";
		}
		
	}
	
    /**
     * 根据主机创建目标服务器套接字，并连接
     * @param serverSocket
     * @param host
     * @throws IOException
     */
    public Socket ConnectToServer(String host) throws IOException {
		return new Socket(host, 80);
    }
}
