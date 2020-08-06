package v1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientMainSR {

	public static final int SERVER_PORT = 12340; // 端口号
	public static final String SERVER_IP = "0.0.0.0"; // IP地址
	public static final int BUFFER_LENGTH = 1026; // 缓冲区大小
	public static final int SEQ_SIZE = 20; // 序列号个数，从0~19共计20个，注意只有C-S互相传递时，序号为1~20

	public static void printTips() {
		System.out.println("***********************************");
		System.out.println("|    -time to get current time    |");
		System.out.println("|       -quit to exit client      |");
		System.out.println("|     -testsr to test the gbn    |");
		System.out.println("***********************************");
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		Client client = new Client(); // 生成客户端对象，并为其分配套接字
		InetAddress serverAddress = InetAddress.getByName("localhost");// 获取服务器的地址
		int serverPort = SERVER_PORT;// 获取服务器的端口号
		printTips(); // 打印菜单

		int interval = 1; // 收到数据包之后返回ack的间隔，默认为1标识每个都返回ack，0或者负数均表示所有的都不返回ack
		double packetLossRatio = 0.1; // 默认包丢失率 0.2
		double ackLossRatio = 0; // 默认ACK丢失率 0.2

		while (true) { // 不断地向服务器发送请求
			Scanner in = new Scanner(System.in);
			String cmd = in.nextLine();

			byte[] dataToServerChoice = cmd.getBytes();// 生成向服务器发送请求的数据报
			DatagramPacket packetToServerChoice = new DatagramPacket(dataToServerChoice, dataToServerChoice.length,
					serverAddress, serverPort);

			byte[] dataFromServer = new byte[BUFFER_LENGTH];// 用于接收服务器响应报文的包
			DatagramPacket packetFromServer = new DatagramPacket(dataFromServer, dataFromServer.length);

			if (cmd.equals("-testsr")) { // 进入测试模式
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
							waitSeq = 1;
						}

					} else if (stage == 1) { // 等待接收数据阶段
						seq = (int) dataFromServer[0];
						if (seq == 0) {
							break;
						}
						byte[] dataToServerAck = new byte[BUFFER_LENGTH];
						// 随机模拟包是否丢失
						if (client.lossInLossRatio(packetLossRatio)) {
							System.out.println("The packet with a seq of " + seq + " loss!");
							continue;
						}
						// 只要收到服务器的报文，就响应一个ack
						System.out.println("receive a packet with a seq of " + seq);
						String data = new String(dataFromServer, 1, dataFromServer.length - 1);
						System.out.println("接收到的数据为: " + data); // 输出数据
						dataToServerAck[0] = (byte) seq; // 生成确认报文序列号的ACK报文
						dataToServerAck[1] = '\0';
						// 随机模拟ack是否丢失
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
