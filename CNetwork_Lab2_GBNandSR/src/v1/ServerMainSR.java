package v1;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class ServerMainSR {

	public static final int BUFFER_LENGTH = 1026; // 缓冲区大小
	public static final int SEND_WIND_SIZE = 10; // 发送窗口大小
	public static final int SEQ_SIZE = 20; // 序列号个数，从0~19共计20个，注意只有C-S互相传递时，序号为1~20

	/**
	 * 读取要发送给客户端的文件数据
	 * 
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	public static byte[] ReadFile(String filepath) throws IOException {
		File f = new File(filepath);
		ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
		byte[] buffer = new byte[1024]; // 数据小于等于1024字节
		int len = 0;
		while ((len = in.read(buffer, 0, 1024)) != -1) {
			bos.write(buffer, 0, len);
		}
		byte[] data = bos.toByteArray();
		in.close();
		return data;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		Server server = new Server();
		InetAddress clientAddress = null; // 客户端的IP地址
		int clientPort; // 客户端的端口号

		byte[] dataToClientChoice = new byte[BUFFER_LENGTH]; // 发送客户端的数据报
		String strDataToClientChoice = "";

		byte[] dataFromClient = new byte[BUFFER_LENGTH]; // 接收来自给客户端的数据报
		String strDataFromClient = "";
		DatagramPacket packetFromClient = new DatagramPacket(dataFromClient, dataFromClient.length);

		// 握手过程的数据传输，都是通过String然后转化成byte[]; 传送的文件是直接通过byte[]进行传输
		while (true) { // 不断接收来自客户端的请求，非阻塞接收，若没有收到数据，返回值为-1
			Boolean ack[] = new Boolean[SEQ_SIZE]; // 收到ack情况，对应1~20的ack
			for (int i = 0; i < SEQ_SIZE; i++) {// 初始化，所有序号ack都为true
				ack[i] = true;
			}
			int curSeq = 0; // 当前数据包的序列号
			int curAck = 0; // 当前等待确认的ack
			int totalSeq = 0; // 已经发送过的包总数
			double totalPacket; // 需要发送的包总数

			try {
				server.serverSocket.receive(packetFromClient); // 不堵塞接收客户端的请求
				clientAddress = packetFromClient.getAddress();
				clientPort = packetFromClient.getPort();
			} catch (SocketTimeoutException e) {
				Thread.sleep(200);
				continue;
			}
			strDataFromClient = new String(dataFromClient, 0, packetFromClient.getLength());
			System.out.println("receive from client: " + strDataFromClient);
			String cmd = strDataFromClient;

			if (cmd.equals("-time")) {
				strDataToClientChoice = server.getCurTime();
			} else if (cmd.equals("-quit")) {
				strDataToClientChoice = "Good bye!";
			} else if (cmd.equals("-testsr")) {
				System.out.println("Begin to test SR protocol,please don't abort the process");
				System.out.println("Shake hands stage");

				byte[] dataToClientFile = new byte[BUFFER_LENGTH];
				byte[] data = ReadFile("src/v1/data.txt");
				totalPacket = Math.ceil(data.length / 2.0);
				System.out.println(
						"File size is " + data.length + ",each packet is 1024B and packet total num is " + totalPacket);

				int waitCount = 0; // 握手阶段的计时器
				int waitCountFile[] = new int[SEQ_SIZE]; // 每个序号的数据包都设置一个计时器
				int stage = 0; // 设置阶段标识
				boolean runflag = true;
				int flag = 1;
				while (runflag) {
					if (stage == 0) { // 发送205阶段,建立握手
						byte[] dataToClient205 = new byte[BUFFER_LENGTH];
						String strDataToClient205 = "205";
						dataToClient205 = strDataToClient205.getBytes();
						DatagramPacket packetToclient205 = new DatagramPacket(dataToClient205, dataToClient205.length,
								clientAddress, clientPort);
						server.serverSocket.send(packetToclient205); // 向客户端发送205报文，建立握手
						Thread.sleep(100);
						stage = 1;
					} else if (stage == 1) { // 等待接收200阶段，没有收到则计数器+1.超时则放弃此次“连接”，等待从第一步开始
						try {
							server.serverSocket.receive(packetFromClient); // 非堵塞的接收客户端的请求
						} catch (SocketTimeoutException e) {
							flag = 0;// 若没有发来数据，计时器加一
							waitCount++;
							if (waitCount > 20) {
								runflag = false;
								System.out.println("Timeout error!");
							} else {
								Thread.sleep(500);
								continue;
							}
						}
						if (flag == 1) { // 超时次数不大于20次，接收到了客户端的数据
							strDataFromClient = new String(dataFromClient, 0, packetFromClient.getLength());
							if (strDataFromClient.contains("200")) {
								System.out.println("Begin a file transfer!");
								curSeq = 1; // 初始化当前发送的报文段序号为1
								curAck = 1; // 当前等待被确认的ack序号为1
								totalSeq = 0;
								waitCount = 0;
								stage = 2;
							}
						}
					} else if (stage == 2) { // 向客户端发送文件报文

						if (server.seqIsAvailable(curSeq, curAck, ack)) {
							dataToClientFile[0] = (byte) (curSeq); // 发送给客户端的序列号从1开始
							ack[curSeq - 1] = false; // 设置当前发送过去的数据包对应的ack为false
							if (curSeq > totalPacket) {
								dataToClientFile[0] = (byte) ('n'); // 如果服务器已经发完了，则告知为'n'
							}
							if (curAck == totalPacket + 1) { // 服务器的数据报已经传完并都收到
								System.out.println("data is over!");
								flag = 0;
								runflag = false;

								byte[] dataToClientEnd = new byte[BUFFER_LENGTH];
								dataToClientEnd[0] = (byte) (0); // 发送给客户端，告知文件传输完毕
								DatagramPacket packetToClientEnd = new DatagramPacket(dataToClientEnd,
										dataToClientEnd.length, clientAddress, clientPort);
								server.serverSocket.send(packetToClientEnd);
								break;
							} else if (curSeq <= totalPacket) { // 服务器的数据包还没有发送完
								if (curSeq == totalPacket) {
									System.arraycopy(data, (curSeq - 1) * 2, dataToClientFile, 1, (data.length % 2));// 生成服务器发送给客户端的文件数据报
								} else {
									System.arraycopy(data, (curSeq - 1) * 2, dataToClientFile, 1, 2);// 生成服务器发送给客户端的文件数据报
								}
								DatagramPacket packetToClientFile = new DatagramPacket(dataToClientFile,
										dataToClientFile.length, clientAddress, clientPort);
								server.serverSocket.send(packetToClientFile);
								curSeq++;
								curSeq %= SEQ_SIZE;
								totalSeq++;
								Thread.sleep(500);
							}
							System.out.println("send a packet with a seq of " + (int) dataToClientFile[0]);
						}
						// 数据报还没有传完
						// 等待Ack，若没有收到，则返回值为-1，计数器+1
						try {
							int list[] = new int[2];
							server.serverSocket.receive(packetFromClient);
							list = server.ackHandlerSR(dataFromClient[0], curAck, ack, curSeq);
							curSeq = list[0];
							curAck = list[1];
//							System.out.println(curSeq);
//							System.out.println(curAck);
//							System.out.println("=======================");

						} catch (SocketTimeoutException e) {
							flag = 0;
						}
						if (flag == 0) {
							waitCount++;
							System.out.println("count:" + waitCount);
							if (waitCount > 20) { // 超过20次等待ack 则判定为超时，重传
								int list[] = new int[3];
								list = server.timeoutHandlerSR(curSeq, curAck, ack, totalSeq);
								curSeq = list[0];
								curAck = list[1];
								totalSeq = list[2];
								waitCount = 0;
								flag = 1;
								System.out.println(curSeq + "\\" + curAck);
								continue;
							}
						}
						Thread.sleep(500);
					} // each stage
				} // while runflag
			} // -testgbn
			if (!cmd.equals("-testsr")) {
				dataToClientChoice = strDataToClientChoice.getBytes(); // 发送给客户端响应报文
				DatagramPacket packetToClientChoice = new DatagramPacket(dataToClientChoice, dataToClientChoice.length,
						clientAddress, clientPort);
				server.serverSocket.send(packetToClientChoice);
			}
		} // while true
	}

}
