package v1;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ClientMain2 {

	public static final int SERVER_PORT = 12340; // 端口号
	public static final String SERVER_IP = "0.0.0.0"; // IP地址
	public static final int BUFFER_LENGTH = 1026; // 缓冲区大小
	public static final int SEQ_SIZE = 20; // 序列号个数，从0~19共计20个，注意只有C-S互相传递时，序号为1~20

	public static void printTips() {
		System.out.println("***********************************");
		System.out.println("|    -time to get current time    |");
		System.out.println("|       -quit to exit client      |");
		System.out.println("|     -testgbn to test the gbn    |");
		System.out.println("|   -returntrans to test Two-way  |");
		System.out.println("***********************************");
	}

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

		Client client = new Client(); // 生成客户端对象，并为其分配套接字
		InetAddress serverAddress = InetAddress.getByName("localhost");// 获取服务器的地址
		int serverPort = SERVER_PORT;// 获取服务器的端口号
		printTips(); // 打印菜单

		double packetLossRatio = 0.2; // 默认包丢失率 0.2
		double ackLossRatio = 0; // 默认ACK丢失率 0.2

		while (true) { // 不断地向服务器发送请求
			Scanner in = new Scanner(System.in);
			String cmd = in.nextLine();

			byte[] dataToServerChoice = cmd.getBytes();// 生成向服务器发送请求的数据报
			DatagramPacket packetToServerChoice = new DatagramPacket(dataToServerChoice, dataToServerChoice.length,
					serverAddress, serverPort);

			byte[] dataFromServer = new byte[BUFFER_LENGTH];// 用于接收服务器响应报文的包
			DatagramPacket packetFromServer = new DatagramPacket(dataFromServer, dataFromServer.length);

			if (cmd.equals("-testgbn")) { // 进入测试模式
				System.out.println("Begin to test GBN protocol,please don't abort the process!");
				System.out.println(
						"The loss ratio of packet is " + packetLossRatio + ",the loss ratio of ack is " + ackLossRatio);

				int waitCount = 0;
				int stage = 0;
				boolean b;
				String u_code = ""; // 收到的服务器的状态码
				int seq; // 包的序列号
				int recvSeq = 0; // 接收窗口大小为1，已确认的序列号
				int waitSeq = 0; // 等待的序列号

				client.clientSocket.send(packetToServerChoice); // 客户端向服务器发送-test请求

				while (true) { // 建立握手并不断接收服务器的数据包
					client.clientSocket.receive(packetFromServer); // 等待server回复为阻塞模式

					if (stage == 0) { // 等待握手阶段
						u_code = new String(dataFromServer, 0, dataFromServer.length);
						if (u_code.contains("205")) {
							System.out.println("Ready for file transmission!");
							// 生成200回复报文
							byte[] dataToServer200 = new byte[BUFFER_LENGTH];
							String strDataToServer200 = "200";
							dataToServer200 = strDataToServer200.getBytes();
							DatagramPacket packetToServer200 = new DatagramPacket(dataToServer200,
									dataToServer200.length, serverAddress, serverPort);
							client.clientSocket.send(packetToServer200); // 客户端向服务器发送200请求握手

							stage = 1;
							recvSeq = 0;
							waitSeq = 1;
						}

					} else if (stage == 1) { // 等待接收数据阶段
						seq = (int) dataFromServer[0];
						if (seq == 0) {
							break;
						}
						byte[] dataToServerAck = new byte[BUFFER_LENGTH];
						// 随机发模拟包是否丢失
						if (client.lossInLossRatio(packetLossRatio)) {
							System.out.println("The packet with a seq of " + seq + " loss!");
							continue;
						}
						System.out.println("receive a packet with a seq of " + seq);
						// 如果是期待的包，正确接收，正常确认即可
						if ((waitSeq - seq) == 0) {
							waitSeq++;
							if (waitSeq == 21) {
								waitSeq = 1;
							}
							String data = new String(dataFromServer, 1, dataFromServer.length - 1);
							System.out.println("接收到的数据为: " + data); // 输出数据

							recvSeq = seq;
							dataToServerAck[0] = (byte) seq; // 生成确认报文序列号的ACK报文
							dataToServerAck[1] = '\0';
						} else { // 不是期望的包
							if (recvSeq == 0) { // 当前一个包都没有收到，则等待seq为1的数据包
								System.out.println("*************************");
								System.out.println("receive a seq of " + (int) dataFromServer[0] + " is throw");
								System.out.println("i need seq 1");
								System.out.println("*************************");
							}
							dataToServerAck[0] = (byte) recvSeq; // 生成已经确认的报文段的ACK
							dataToServerAck[1] = '\0';
							System.out.println("*************************");
							System.out.println("receive a seq of " + (int) dataFromServer[0] + " is throw");
							System.out.println("send a ack of " + (int) dataToServerAck[0] + " is throw");
							System.out.println("i need seq " + ((int) dataToServerAck[0] + 1));
							System.out.println("*************************");
							continue;
						}
						if (client.lossInLossRatio(ackLossRatio)) {
							System.out.println("The ack of " + (int) dataToServerAck[0] + " loss");
							continue;
						}
						// 发送客户端回复报文（ack）
						DatagramPacket packetToServerAck = new DatagramPacket(dataToServerAck, dataToServerAck.length,
								serverAddress, serverPort);
						client.clientSocket.send(packetToServerAck);
						System.out.println("send a ack of " + (int) dataToServerAck[0]);
					} // stage
					Thread.sleep(500);
				} // while true stage 建立握手并不断接收服务器的数据包
				printTips();
			} else if (cmd.equals("-returntrans")) {

				client.clientSocket.send(packetToServerChoice); // 客户端向服务器发送-test请求

				Boolean ack[] = new Boolean[SEQ_SIZE]; // 收到ack情况，对应1~20的ack
				for (int i = 0; i < SEQ_SIZE; i++) {// 初始化，所有序号ack都为true
					ack[i] = true;
				}
				int curSeq = 1; // 当前数据包的序列号
				int curAck = 1; // 当前等待确认的ack
				int totalSeq = 0; // 已经发送过的包总数
				double totalPacket; // 需要发送的包总数
				boolean runflag = true;

				System.out.println("Begin to test GBN protocol,please don't abort the process");
				System.out.println("Shake hands stage");

				byte[] dataToServerFile = new byte[BUFFER_LENGTH];
				byte[] data = ReadFile("src/v1/data.txt");
				totalPacket = Math.ceil(data.length / 2.0);
				System.out.println(
						"File size is " + data.length + ",each packet is 1024B and packet total num is " + totalPacket);
				int flag = 1;
				while (runflag) {
					if (client.seqIsAvailable(curSeq, curAck, ack)) {
						dataToServerFile[0] = (byte) (curSeq); // 发送给客户端的序列号从1开始
						ack[curSeq - 1] = false; // 设置当前发送过去的数据包对应的ack为false
						if (curSeq > totalPacket) {
							dataToServerFile[0] = (byte) ('n'); // 如果服务器已经发完了，则告知为'n'
						}
						if (curAck == totalPacket + 1) { // 服务器的数据报已经传完并都收到
							System.out.println("data is over!");
							flag = 0;
							runflag = false;

							byte[] dataToServerEnd = new byte[BUFFER_LENGTH];
							dataToServerEnd[0] = (byte) (0); // 发送给客户端，告知文件传输完毕
							DatagramPacket packetToServerEnd = new DatagramPacket(dataToServerEnd,
									dataToServerEnd.length, serverAddress, serverPort);
							client.clientSocket.send(packetToServerEnd);
							break;
						} else if (curSeq <= totalPacket) { // 服务器的数据包还没有发送完
							if (curSeq == totalPacket) {
								System.arraycopy(data, (curSeq - 1) * 2, dataToServerFile, 1, (data.length % 2));// 生成服务器发送给客户端的文件数据报
							} else {
								System.arraycopy(data, (curSeq - 1) * 2, dataToServerFile, 1, 2);// 生成服务器发送给客户端的文件数据报
							}
							DatagramPacket packetToServerFile = new DatagramPacket(dataToServerFile,
									dataToServerFile.length, serverAddress, serverPort);
							client.clientSocket.send(packetToServerFile);
							curSeq++;
							curSeq %= SEQ_SIZE;
							totalSeq++;
							Thread.sleep(500);
						}
						System.out.println("send a packet with a seq of " + (int) dataToServerFile[0]);
					}
					if (flag == 1) { // 数据报还没有传完
						client.clientSocket.receive(packetFromServer);
						curAck = client.ackHandler(dataFromServer[0], curAck, ack);
//						System.out.println(curSeq);
//						System.out.println(curAck);
//						System.out.println("=======================");
						Thread.sleep(500);
					}
				} // while runflag
				printTips();
			} else {// 向服务器发送-time 或者 -quit 请求
				client.clientSocket.send(packetToServerChoice);
				client.clientSocket.receive(packetFromServer);// 收到服务区的回复报文
				String data = new String(dataFromServer, 0, dataFromServer.length);
				System.out.println(data);
				if (data.contains("Good bye!")) {
					break;
				}
				printTips();
			}
		} // while true 不断地向服务器发送请求
		client.clientSocket.close();
		return;
	}

}
