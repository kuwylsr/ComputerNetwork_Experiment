//package test;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.URLDecoder;
//import java.util.StringTokenizer;
//
//
//public class ProxyServer_temp {
//	ServerSocket pServerSocket = null; // 代理服务器的主套接字
//	int proxyPort; // 代理服务器的端口号
//	
//	
//	public ProxyServer_temp(int port) {
//		try {
//			this.proxyPort = port;
//			// 第一个参数：套接字绑定的端口号；第二个参数：最大连接并发数；第三个参数：被绑定的当地IP地址
//			this.pServerSocket = new ServerSocket(this.proxyPort, 50, null);
//		} catch (IOException e) {
//			System.err.println("创建代理服务器主套接字失败！");
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 解析TCP报文中的Http头部
//	 * @param buffer
//	 * @param httpHeader
//	 * @throws IOException 
//	 */
//	public String ParseHttpHead(BufferedReader in,HttpHeader httpHeader) throws IOException {
//		
//		String line;
//		String data_fromClient = ""; //来自客户机的数据报信息
//		int flag = 0;
//		//继续获取 host cookie等信息
//		while((line = in.readLine()) != null && line.length() != 0) {
////			System.err.println(line);
//			data_fromClient = data_fromClient + line +("\r\n");
//			// 获取url地址
//			if(line.contains("GET")||line.contains("POST")) {
//				flag = 1;
//				String resource[] = line.split(" ");
//				httpHeader.url = URLDecoder.decode(resource[1], "UTF-8");
//				// 获取method方法
//				httpHeader.method = new StringTokenizer(line).nextElement().toString();
//			}else if(line.contains("Host")) {
//				httpHeader.host = line.substring(6, line.length());
//			}else if(line.contains("Cookie")) {
//				httpHeader.cookie = line.substring(8, line.length());
//			}
//		}
//		if(flag == 0) {
//			return null;
//		}
//		return data_fromClient + ("\r\n");
//	}
//	
//    /**
//     * 根据主机创建目标服务器套接字，并连接
//     * @param serverSocket
//     * @param host
//     * @throws IOException
//     */
//    public Socket ConnectToServer(String host) throws IOException {
//		return new Socket(host, 80);
//    }
//}
//
