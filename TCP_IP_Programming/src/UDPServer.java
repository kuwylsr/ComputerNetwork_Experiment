import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UDPServer {

    public static void main(String[] args) {

        int serverPort = 8080;

        try {
            DatagramSocket serverSocket = new DatagramSocket(serverPort); // 创建服务器端套接字,并且绑定响应的端口号
  
            while (true) {
                byte[] recieveMessage = new byte[2048]; // 用于发送消息的字节数组
                byte[] sendMessage = new byte[2048]; // 用于接收消息的字节数组

                DatagramPacket recievePacket = new DatagramPacket(recieveMessage, recieveMessage.length);
                serverSocket.receive(recievePacket); // 接受Client发来的UDP数据包
                String recieveStr = new String(recievePacket.getData());

                String sendStr = recieveStr.toUpperCase(); // 进行大小写转换
                sendMessage = sendStr.getBytes();

                InetAddress ClientIPAddress = recievePacket.getAddress(); // 通过Client发来的数据包 获取其 IP地址
                int ClientPort = recievePacket.getPort(); // 通过Client发来的数据包 获取其 套接字端口号
                DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, ClientIPAddress, ClientPort); 
                serverSocket.send(sendPacket); // Server 向 Client 发送数据包

                Scanner in = new Scanner(System.in);
                System.out.println("can i close the server ? (Y/N)");
                String s = in.nextLine();
                if(s.equals("Y")){
                    serverSocket.close();
                    in.close();
                    System.out.println("bye~");
                    break;
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
