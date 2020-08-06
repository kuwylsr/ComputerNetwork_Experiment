package v1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheProxyParam implements Runnable {

	public static final int MAXSIZE = 65507; // 发送数据报文的最大长度
	public static final int HTTP_PORT = 80; // http的服务器端口

	ProxyServer pServer_main;
	Socket pServerSocket_Client = null; // 用于与客户机连接的代理服务器的套接字
	Socket pServerSocket_Server = null;// 用于与服务器连接的代理服务器的套接字

	public CacheProxyParam(ProxyServer pServer_main) {
		this.pServer_main = pServer_main;
	}
	
	/**
	 * 代理服务器将客户端的请求报文转发给目标服务器
	 * @param data_fromClient
	 * @param out_fromProxyToServer
	 * @throws IOException
	 */
	public void OutFromProxyToServer(String data_fromClient,DataOutputStream out_fromProxyToServer) throws IOException {
		out_fromProxyToServer = new DataOutputStream(this.pServerSocket_Server.getOutputStream());
		out_fromProxyToServer.writeBytes(data_fromClient);
	}
	
	/**
	 * 代理服务器接收目标服务器的响应报文
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
	 * 将代理服务器收到的响应报文转发给客户端
	 * @param data_fromServer
	 * @param out_fromProxyToClient
	 * @throws IOException
	 */
	public void OutFromProxyToClient(byte[] data_fromServer,OutputStream out_fromProxyToClient) throws IOException {
		out_fromProxyToClient = this.pServerSocket_Client.getOutputStream();
		out_fromProxyToClient.write(data_fromServer, 0, data_fromServer.length);
	}
	
	/**
	 * 判断请求的相应在缓存中是否存在
	 * @param CacheContent
	 * @param url
	 * @return 是否存在对应的相应报文
	 */
	public boolean IsCache(String CachePath,String url) {
		File file = new File(CachePath);
		File[] tempList = file.listFiles();
		for (int i = 0; i < tempList.length; i++) {
			if(tempList[i].getName().equals(replaceSpecStr(url))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 将url中的特殊字符进行过滤
	 * @param orgStr
	 * @return 过滤后的字符串
	 */
	public String replaceSpecStr(String orgStr) {
		String regEx = "[\\s~·`!！@#￥$%^……&*（()）\\-——\\-_=+【\\[\\]】｛{}｝\\|、\\\\；;：:‘'“”\"，,《<。.》>、/？?]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(orgStr);
		return m.replaceAll("");
	}
	
	/**
	 * 将报文写进缓存中
	 * @param CachePath
	 * @param httpHeader
	 * @param data_fromServer
	 * @param CacheContent
	 * @throws IOException
	 */
	public void WriteInCache(String CachePath,HttpHeader httpHeader,byte[] data_fromServer) throws IOException {
		File file = new File(CachePath+replaceSpecStr(httpHeader.url));
		OutputStream out = new FileOutputStream(file);
		out.write(data_fromServer);
		out.close();
	}
	
	/**
	 * 从缓存中读取响应报文
	 * @param CachePath
	 * @param httpHeader
	 * @return 返回缓存中的响应报文
	 * @throws IOException
	 */
	public byte[] ReadOutCache(String CachePath,HttpHeader httpHeader) throws IOException {
		File f = new File(CachePath+replaceSpecStr(httpHeader.url));
		ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
		byte[] buffer = new byte[1024];
		int len = 0;
		while((len = in.read(buffer,0,1024)) != -1) {
			bos.write(buffer, 0, len);
		}
		byte[] data_fromCache = bos.toByteArray();
		in.close();
		return data_fromCache;
	}
	
	@Override
	public void run(){
		BufferedReader in_fromClientToProxy = null;
		DataOutputStream out_fromProxyToServer =null;
		InputStream in_fromServerToProxy =null;
		OutputStream out_fromProxyToClient =null;
		try {
			//建立缓存文件目录
			String CachePath = "src/v1/CacheFile/";
			// 充当服务器，从客户端接收数据
			in_fromClientToProxy = new BufferedReader(new InputStreamReader(this.pServerSocket_Client.getInputStream()));
			HttpHeader httpHeader = new HttpHeader();
			String data_fromClient = this.pServer_main.ParseHttpHead(in_fromClientToProxy, httpHeader);
			// 判断客户端发来的请求是否为空
			if(httpHeader.host == null || data_fromClient == null||this.pServer_main.FilterWebSite(httpHeader) == null) {
				return;
			}
			// 判断所请求的网站是不是要被钓鱼
			if(this.pServer_main.GoFishing(this.pServerSocket_Client,httpHeader) == null) {
				return;
			}
			System.out.println("====================");
			System.out.println("客户端的发送的数据报: ");
			System.out.println("====================");
			String resource[] = data_fromClient.split("\r\n");
 			System.out.println(resource[0]+"\r\n"+resource[1]+"\r\n"+resource[2]+"\r\n");
			//代理服务器与目标服务器建立连接
			this.pServerSocket_Server = this.pServer_main.ConnectToServer(httpHeader.host);
			if(!IsCache(CachePath,httpHeader.url)) { //没有缓存过所要请求的报文
				System.out.println("====="+"针对:"+httpHeader.url+"=====");
				System.out.println("=====没有缓存过！====="+"\r\n");
				//将客户端发送的HTTP数据报文直接转发给目标服务器
				OutFromProxyToServer(data_fromClient,out_fromProxyToServer);
				// 目标服务器返回给代理服务器数据
				byte[] data_fromServer = InFromServerToProxy(in_fromServerToProxy);
				// 返回给客户机目标服务器的数据报
				OutFromProxyToClient(data_fromServer,out_fromProxyToClient);
				System.out.println("====="+"针对:"+httpHeader.url+"=====");
				System.out.println("=====收到服务器的数据报并将其写入缓存！====="+"\r\n");
				// 将响应报文写入缓存文件
				WriteInCache(CachePath, httpHeader, data_fromServer);
			}else {
				System.out.println("====="+"针对:"+httpHeader.url+"=====");
				System.out.println("=====存在缓存！====="+"\r\n");
				//解析缓存报文的头部的Last-modified
				BufferedReader anaylize_Cache = new BufferedReader(new FileReader(CachePath+replaceSpecStr(httpHeader.url)));
				String data_ConditionalGET = this.pServer_main.MakeConditionalGET(data_fromClient, anaylize_Cache);
				if(data_ConditionalGET.equals("NoDataModified")) {
					System.out.println("====="+"针对:"+httpHeader.url+"=====");
					System.out.println("=====缓存报文中无日期要求！====="+"\r\n");
					// 从缓存中读取文件
					byte[] data_fromCache = ReadOutCache(CachePath, httpHeader);
					// 返回给客户机目标服务器的数据报
					OutFromProxyToClient(data_fromCache,out_fromProxyToClient);
					System.out.println("====="+"针对:"+httpHeader.url+"=====");
					System.out.println("=====收到缓存中无日期要求的数据报！====="+"\r\n");
				}else {
					// 将条件GET请求发送给目标服务器
					OutFromProxyToServer(data_ConditionalGET,out_fromProxyToServer);
					// 目标服务器返回给代理服务器数据
					byte[] data_fromServer = InFromServerToProxy(in_fromServerToProxy);
					String temp = this.pServer_main.ParseLastModified(data_fromServer);
					if(temp.equals("NoChange")) {
						System.out.println("====="+"针对:"+httpHeader.url+"=====");
						System.out.println("=====收到缓存中数据报仍为最新！====="+"\r\n");
						// 从缓存中读取文件
						byte[] data_fromCache = ReadOutCache(CachePath, httpHeader);
						// 返回给客户机目标服务器的数据报
						OutFromProxyToClient(data_fromCache,out_fromProxyToClient);
						System.out.println("====="+"针对:"+httpHeader.url+"=====");
						System.out.println("=====收到服务器的数据报，缓存不用更新！====="+"\r\n");
					}else if(temp.equals("Change")){
						System.out.println("====="+"针对:"+httpHeader.url+"=====");
						System.out.println("=====收到缓存中数据报需要更新！====="+"\r\n");
						// 返回给客户机目标服务器的数据报
						OutFromProxyToClient(data_fromServer,out_fromProxyToClient);
						// 将最新日期的响应报文写入缓存文件
						WriteInCache(CachePath, httpHeader, data_fromServer);
						System.out.println("====="+"针对:"+httpHeader.url+"=====");
						System.out.println("=====更新缓存中数据报！====="+"\r\n");
					}
				}	
			}
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
