//package test;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.net.Socket;
//import java.util.Arrays;
//
//public class ProxyParam_temp implements Runnable {
//
//	public static final int MAXSIZE = 65507; // 发送数据报文的最大长度
//	public static final int HTTP_PORT = 80; // http的服务器端口
//
//	ProxyServer pServer_main;
//	Socket pServerSocket_Client = null; // 用于与客户机连接的代理服务器的套接字
//	Socket pServerSocket_Server = null;// 用于与服务器连接的代理服务器的套接字
//
//	public ProxyParam_temp(ProxyServer pServer_main) {
//		this.pServer_main = pServer_main;
//	}
//
//	@Override
//	public void run(){
//		System.out.println(pServerSocket_Client.getInetAddress());
//		try {
//			// 充当服务器，从客户端接收数据
//			BufferedReader in_fromClientToProxy = new BufferedReader(new InputStreamReader(this.pServerSocket_Client.getInputStream()));
//			HttpHeader httpHeader = new HttpHeader();
//			String data_fromClient = this.pServer_main.ParseHttpHead(in_fromClientToProxy, httpHeader);
//			if(data_fromClient == null) {
//				this.pServerSocket_Client.close();
//				return;
//			}
//
//			// 充当客户机，向服务器发送数据，并接受服务器的返回信息
//				//代理服务器与目标服务器建立连接
//			this.pServerSocket_Server = this.pServer_main.ConnectToServer(httpHeader.host);
//				//将客户端发送的HTTP数据报文直接转发给目标服务器
////			BufferedWriter out_fromProxyToServer = new BufferedWriter(new OutputStreamWriter(this.pServerSocket_Server.getOutputStream()));
////			out_fromProxyToServer.write(data_fromClient);
//			DataOutputStream out_fromProxyToServer = new DataOutputStream(this.pServerSocket_Server.getOutputStream());
//			out_fromProxyToServer.writeBytes(data_fromClient);
//				//等待目标服务器返回数据
//			InputStream in_fromServerToProxy = this.pServerSocket_Server.getInputStream();
//			OutputStream out_fromProxyToClient = this.pServerSocket_Client.getOutputStream();
//			while(true) {
//				out_fromProxyToClient.write(in_fromServerToProxy.read());
//			}
////			DataInputStream in_fromServerToProxy = new DataInputStream(this.pServerSocket_Server.getInputStream());
////			byte[] data_fromServer = new byte[MAXSIZE];
////			int len = in_fromServerToProxy.read(data_fromServer);
//			
////			byte[] data_fromServer = new byte[0];
////			byte[] temp = new byte[1024];
////			
////			int len_init= in_fromServerToProxy.read(temp);
////			while(len_init != -1) {
////				int len_temp = data_fromServer.length;
////				data_fromServer = Arrays.copyOf(data_fromServer, len_temp + len_init);
////				System.arraycopy(temp, 0, data_fromServer, len_temp, len_init);
////				len_init = in_fromServerToProxy.read(temp);
////			}
//
//			// 再次充当服务器，返回给客户机目标服务器的数据报
////			BufferedWriter out_fromProxyToClient = new BufferedWriter(new OutputStreamWriter(this.pServerSocket_Client.getOutputStream()));
////			out_fromProxyToClient.write(data_fromServer);
//			
////			DataOutputStream out_fromProxyToClient = new DataOutputStream(this.pServerSocket_Client.getOutputStream());
////			out_fromProxyToClient.write(data_fromServer, 0, len);
//			
//		} catch (IOException e) {
//			System.err.println("关闭套接字！");
////			try {
////				Thread.sleep(200);
////				this.pServerSocket_Client.close();
////				this.pServerSocket_Server.close();
////				
////			} catch (InterruptedException | IOException e1) {
////				e1.printStackTrace();
////			}
//			e.printStackTrace();
//		} finally {
//			
//		}
//	
//		
//	}
//}
