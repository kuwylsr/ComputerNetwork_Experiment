import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class UDPClient {

    public static void main(String[] args) {

        String serverName = "localhost"; // 服务器名字
        int serverPort = 8080; // 服务器目标进程的套接字端口号

        try {
            DatagramSocket clientSocket = new DatagramSocket(); // 定义一个用于UDP传输的客户端套接字
            InetAddress IPAddress = InetAddress.getByName(serverName); // 通过给定的主机名来返回主机的IP地址

            System.out.println("please input the message : ");
            Scanner in = new Scanner(System.in);

            byte[] sendMessage = in.nextLine().getBytes(); // client要发送的信息
            // 将源地址附在分组之上通常并不是由UDP应用程序所为, 而是由底层操作系统自动完成的.
            DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, IPAddress, serverPort); // 生成UDP数据包(包括消息内容,消息长度,目的IP地址,目的端口号)
            clientSocket.send(sendPacket); // client发送数据包

            byte[] receiveMessage = new byte[2048]; // 用于接受消息的字节数组
            DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length); // 用于接收消息的UDP数据包
            clientSocket.receive(receivePacket); //client接收数据包

            receiveMessage = receivePacket.getData();
            System.out.println("Client received the message : " + new String(receiveMessage));

            clientSocket.close();
            in.close();

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
