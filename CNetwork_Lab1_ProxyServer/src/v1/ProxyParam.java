package v1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class ProxyParam implements Runnable {

	public static final int MAXSIZE = 65507; // 发送数据报文的最大长度
	public static final int HTTP_PORT = 80; // http的服务器端口

	ProxyServer pServer_main;
	Socket pServerSocket_Client = null; // 用于与客户机连接的代理服务器的套接字
	Socket pServerSocket_Server = null;// 用于与服务器连接的代理服务器的套接字

	public ProxyParam(ProxyServer pServer_main) {
		this.pServer_main = pServer_main;
	}
	
	/**
	 * @param data_fromClient
	 * @param out_fromProxyToServer
	 * @throws IOException
	 */
	public void OutFromProxyToServer(String data_fromClient,DataOutputStream out_fromProxyToServer) throws IOException {
		out_fromProxyToServer = new DataOutputStream(this.pServerSocket_Server.getOutputStream());
		out_fromProxyToServer.writeBytes(data_fromClient);
	}
	
	/**
	 * @param in_fromServerToProxy
	 * @throws IOException
	 */
	public byte[] InFromServerToProxy(InputStream in_fromServerToProxy) throws IOException {
		in_fromServerToProxy = this.pServerSocket_Server.getInputStream();
		byte[] data_fromServer = new byte[0];
		byte[] temp = new byte[1024];
		int len_init= in_fromServerToProxy.read(temp);
		while(len_init != -1) {
			int len_temp = data_fromServer.length;
			data_fromServer = Arrays.copyOf(data_fromServer, len_temp + len_init);
			System.arraycopy(temp, 0, data_fromServer, len_temp, len_init);
			len_init = in_fromServerToProxy.read(temp);
		}
		return data_fromServer;
	}
	
	/**
	 * @param data_fromServer
	 * @param out_fromProxyToClient
	 * @throws IOException
	 */
	public void OutFromProxyToClient(byte[] data_fromServer,OutputStream out_fromProxyToClient) throws IOException {
		out_fromProxyToClient = this.pServerSocket_Client.getOutputStream();
		out_fromProxyToClient.write(data_fromServer, 0, data_fromServer.length);
	}

	@Override
	public void run(){
		BufferedReader in_fromClientToProxy = null;
		
		DataOutputStream out_fromProxyToServer =null;
		InputStream in_fromServerToProxy =null;
		OutputStream out_fromProxyToClient =null;
		try {
			// 充当服务器，从客户端接收数据
			in_fromClientToProxy = new BufferedReader(new InputStreamReader(this.pServerSocket_Client.getInputStream()));
			HttpHeader httpHeader = new HttpHeader();
			String data_fromClient = this.pServer_main.ParseHttpHead(in_fromClientToProxy, httpHeader);
			// 判断所请求的网站是不是要被钓鱼
			if(this.pServer_main.GoFishing(this.pServerSocket_Client,httpHeader) == null) {
				return;
			}
			// 判断客户端发来的请求是否为空
			if(httpHeader.host == null || data_fromClient == null||this.pServer_main.FilterWebSite(httpHeader) == null) {
				return;
			}
			System.out.println("====================");
			System.out.println("客户端的发送的数据报: ");
			System.out.println("====================");
			String resource[] = data_fromClient.split("\r\n");
 			System.out.println(resource[0]+"\r\n"+resource[1]+"\r\n"+resource[2]+"\r\n");
				//代理服务器与目标服务器建立连接
			this.pServerSocket_Server = this.pServer_main.ConnectToServer(httpHeader.host);
				//将客户端发送的HTTP数据报文直接转发给目标服务器
			OutFromProxyToServer(data_fromClient,out_fromProxyToServer);
			// 目标服务器返回给代理服务器数据
			byte[] data_fromServer = InFromServerToProxy(in_fromServerToProxy);
			// 返回给客户机目标服务器的数据报
			OutFromProxyToClient(data_fromServer,out_fromProxyToClient);
			System.out.println("====="+"针对:"+httpHeader.url+"=====");
			System.out.println("=====收到服务器的数据报！====="+"\r\n");
		} catch (IOException e) {
			e.getMessage();
			e.printStackTrace();
		} finally {
			if(in_fromClientToProxy !=null) {
				try {
					in_fromClientToProxy.close();
				} catch (IOException e1) {
					// TODO: handle exception
				}
			}
			if(out_fromProxyToServer !=null) {
				try {
					out_fromProxyToServer.close();
				} catch (IOException e1) {
					// TODO: handle exception
				}
			}
			if(in_fromServerToProxy !=null) {
				try {
					in_fromServerToProxy.close();
				} catch (IOException e1) {
					// TODO: handle exception
				}
			}
			if(out_fromProxyToClient !=null) {
				try {
					out_fromProxyToClient.close();
				} catch (IOException e1) {
					// TODO: handle exception
				}
			}
			if(this.pServerSocket_Server !=null) {
				try {
					this.pServerSocket_Server.close();
				} catch (IOException e1) {
					// TODO: handle exception
				}
			}
			if(this.pServerSocket_Client !=null) {
				try {
					this.pServerSocket_Client.close();
				} catch (IOException e1) {
					// TODO: handle exception
				}
			}
		}
	
		
	}
}
