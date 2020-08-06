//package test;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.Socket;
//
//
//public class ProxyParam implements Runnable {
//
//	public static final int MAXSIZE = 65507; // 发送数据报文的最大长度
//	public static final int HTTP_PORT = 80; // http的服务器端口
//
//	ProxyServer pServer_main;
//	Socket pServerSocket_Client = null; // 用于与客户机连接的代理服务器的套接字
//	Socket pServerSocket_Server = null;// 用于与服务器连接的代理服务器的套接字
//
//	public ProxyParam(ProxyServer pServer_main) {
//		this.pServer_main = pServer_main;
//	}
//
//	@Override
//	public void run(){
//		BufferedReader in_fromClientToProxy = null;
//		
//		DataOutputStream out_fromProxyToServer =null;
//		InputStream in_fromServerToProxy =null;
//		OutputStream out_fromProxyToClient =null;
//		try {
//			// 充当服务器，从客户端接收数据
//			in_fromClientToProxy = new BufferedReader(new InputStreamReader(this.pServerSocket_Client.getInputStream()));
//			HttpHeader httpHeader = new HttpHeader();
//			String data_fromClient = this.pServer_main.ParseHttpHead(in_fromClientToProxy, httpHeader);
//			if(httpHeader.host == null || data_fromClient == null) {
//				return;
//			}
//			System.out.println("===============================");
//			System.out.println("代理服务器收到来自客户端的数据报：其中" + "\r\n" +"method:"+httpHeader.method+"\r\n"+"host:"+httpHeader.host+"\r\n"+"url:"+httpHeader.url);
//			System.out.println("数据报为:\r\n"+data_fromClient);
//			System.out.println("===============================");
//			// 充当客户机，向服务器发送数据，并接受服务器的返回信息
//				//代理服务器与目标服务器建立连接
//			this.pServerSocket_Server = this.pServer_main.ConnectToServer(httpHeader.host);
//				//将客户端发送的HTTP数据报文直接转发给目标服务器
//			out_fromProxyToServer = new DataOutputStream(this.pServerSocket_Server.getOutputStream());
//			out_fromProxyToServer.writeBytes(data_fromClient);
//				//等待目标服务器返回数据
//			// 再次充当服务器，返回给客户机目标服务器的数据报
//			in_fromServerToProxy = this.pServerSocket_Server.getInputStream();
//			out_fromProxyToClient = this.pServerSocket_Client.getOutputStream();
//			while(true) {
//				if(in_fromServerToProxy.available() > 0) {
//					out_fromProxyToClient.write(in_fromServerToProxy.read());
//				}
//				
//			}
//			
//		} catch (IOException e) {
//			e.getMessage();
//			e.printStackTrace();
//		} finally {
//			if(in_fromClientToProxy !=null) {
//				try {
//					in_fromClientToProxy.close();
//				} catch (IOException e1) {
//					// TODO: handle exception
//				}
//			}
//			if(out_fromProxyToServer !=null) {
//				try {
//					out_fromProxyToServer.close();
//				} catch (IOException e1) {
//					// TODO: handle exception
//				}
//			}
//			if(in_fromServerToProxy !=null) {
//				try {
//					in_fromServerToProxy.close();
//				} catch (IOException e1) {
//					// TODO: handle exception
//				}
//			}
//			if(out_fromProxyToClient !=null) {
//				try {
//					out_fromProxyToClient.close();
//				} catch (IOException e1) {
//					// TODO: handle exception
//				}
//			}
//			if(this.pServerSocket_Server !=null) {
//				try {
//					this.pServerSocket_Server.close();
//				} catch (IOException e1) {
//					// TODO: handle exception
//				}
//			}
//			if(this.pServerSocket_Client !=null) {
//				try {
//					this.pServerSocket_Client.close();
//				} catch (IOException e1) {
//					// TODO: handle exception
//				}
//			}
//		}
//	
//		
//	}
//}
