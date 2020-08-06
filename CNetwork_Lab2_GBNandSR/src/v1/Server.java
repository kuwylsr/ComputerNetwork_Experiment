package v1;

import java.io.IOException;
import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {

	public static final int BUFFER_LENGTH = 1026; // 缓冲区大小
	public static final int SEND_WIND_SIZE = 10; // 发送窗口大小
	public static final int SEQ_SIZE = 20; // 序列号个数，从0~19共计20个，注意实际的序列号为1~20

	public static final int SERVER_PORT = 12340; // 端口号
	public static final String SERVER_IP = "0.0.0.0"; // IP地址

	DatagramSocket serverSocket = null; // 服务器套接字

	public Server() throws IOException {
		this.serverSocket = new DatagramSocket(SERVER_PORT);
		this.serverSocket.setSoTimeout(200);
	}

	/**
	 * 获取当前系统时间，结果存入ptime
	 * 
	 * @param ptime
	 */
	public String getCurTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		return df.format(new Date());// new Date()为获取当前系统时间
	}

	/**
	 * 判断当前序列号curSeq是否可用
	 * 
	 * @return
	 */
	public boolean seqIsAvailable(int curSeq, int curAck, Boolean ack[]) {
		int step;
		step = curSeq - curAck;
		if (step < 0) { // 序号循环计数
			step = step + SEQ_SIZE;
		}
		if (step >= SEND_WIND_SIZE) { // 当前要使用的序号已经超出了窗口范围
			return false;
		}
		if (ack[curSeq - 1]) { // 当前请求的需要已经收到了ack
			return true;
		}
		return true;
	}

	/**
	 * 超时重传处理函数，滑动窗口内的数据帧都要重传
	 */
	public int[] timeoutHandler(int curSeq, int curAck, Boolean ack[], int totalSeq) {
		int index;
		int[] list = new int[3];
		for (int i = 0; i < SEND_WIND_SIZE; ++i) { // 把窗口内的序列号都设置为没有接收到ack
			index = (i + curAck - 1) % SEQ_SIZE;
			ack[index] = false;
		}
		totalSeq = totalSeq - SEND_WIND_SIZE;
		curSeq = curAck;
		list[0] = curSeq;
		list[1] = curAck;
		list[2] = totalSeq;
		return list;
	}

	/**
	 * 收到ack，累计确认，取数据帧的第一个字节
	 * 
	 * @param c
	 */
	public int ackHandler(byte c, int curAck, Boolean ack[]) {
		int index = ((int) c);
		System.out.println("Receive a ack of " + index);
		if (curAck <= index) { // 收到的ack大于等于等待接受的ack
			for (int i = curAck; i <= index; i++) {// 将收到ack的序列号之前的ack序列号都标记被true
				ack[i - 1] = true;
			}
			curAck = (index + 1) % SEQ_SIZE;// 等待接受的ack加1
		} else { // 收到的ack小于等待接收的ack
			for (int i = curAck; i <= SEQ_SIZE; i++) {// 将等待接收的ack的序列号之后的序列号都标记为false
				ack[i - 1] = false;
			}
			for (int i = 1; i <= index; i++) {
				ack[i - 1] = true;
			}
			curAck = index + 1;
		}
		return curAck;
	}

	/**
	 * SR协议的超时处理函数
	 * @param curSeq
	 * @param curAck
	 * @param ack
	 * @param totalSeq
	 * @return
	 */
	public int[] timeoutHandlerSR(int curSeq, int curAck, Boolean ack[], int totalSeq) {
		int[] list = new int[3];
		totalSeq = totalSeq - 1;
		curSeq = curAck; // 将下一个数据包的序列号，设置为待接收的ack的序列号
		list[0] = curSeq;
		list[1] = curAck;
		list[2] = totalSeq;
		return list;
	}

	/**
	 * SR协议的接收ack函数
	 * @param c
	 * @param curAck
	 * @param ack
	 * @param curSeq
	 * @return
	 */
	public int[] ackHandlerSR(byte c, int curAck, Boolean ack[], int curSeq) {
		int[] list = new int[2];
		int temp = curAck; //记录先前的ack
		int index = (int) c; //到达的ack序列号
		int flag = 1;
		System.out.println("Receive a ack of " + index);
		ack[index - 1] = true;//将到达的ack序列号设置为true
		if (index > curAck) { // 若收到的ack序列号大于待收到的ack序列号
			for (int i = index + 1; i <= SEQ_SIZE; i++) { // 将收到的ack序列好之后的序列号都标记为false
				ack[i - 1] = false;
			}
		}
		for (int i = 0; i < SEQ_SIZE; i++) { // 将待接收的ack序列号设置为排在最前面的false的ack
			if (!ack[i]) {
				curAck = i + 1;
				if (temp != curAck) { // 若最前面的false的ack序列号不等于之前的等待的ack序列号，那么更新curSeq（下一个要发送的数据包）
					curSeq = curAck;
				}
				flag = 0;
				break;
			}
		}
		if (flag == 1) { //若所有的序列号标签的ack都为true，那就将curAck加一
			curAck = curAck + 1;
		}
		list[0] = curSeq;
		list[1] = curAck;
		return list;
	}

}
