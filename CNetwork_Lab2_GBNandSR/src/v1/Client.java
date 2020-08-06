package v1;

import java.net.DatagramSocket;
import java.net.SocketException;

public class Client {

	DatagramSocket clientSocket = null;
	
	public static final int BUFFER_LENGTH = 1026; // 缓冲区大小
	public static final int SEND_WIND_SIZE = 10; // 发送窗口大小
	public static final int SEQ_SIZE = 20; // 序列号个数，从0~19共计20个，注意实际的序列号为1~20

	public Client() throws SocketException {
		this.clientSocket = new DatagramSocket();
	}

	public boolean lossInLossRatio(double lossRatio) {
		int lossBound = (int) (lossRatio * 100);
		double r = Math.random() * 100;
		if ((int) r < lossBound) { // 丢失的情况
			return true;
		}
		return false;
	}
	
	//==========================================================================================
	
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

}
