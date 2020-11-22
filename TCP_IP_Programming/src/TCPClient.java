import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

    public static void main(String[] args) {

        String serverName = "localhost";
        int serverPort = 6789;

        try {
            Socket clientSocket = new Socket(serverName, serverPort);
            // clientSocket.connect(null); // 建立TCP连接?

            System.out.println("please input the message : ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //从键盘输入的 缓冲流(字符输入流)
            String sendMessageStr = br.readLine();

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); // 如果client套接字有一个相关联的通道, 那么结果输出流将其所有操作委托给该通道.(字节输出流)
            // 要注意:接收数据方法readLine要读到换行符才认为读一行结束,所以发送过去的消息一定要带上一个换行符 '\n'
            outToServer.writeBytes(sendMessageStr + '\n'); //向server发送消息

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // 如果这个套接字有一个相关联的通道，那么产生的输入流将其所有操作委托给该通道。(字符输入流)
            String receiveMessageStr = inFromServer.readLine();
            System.out.println("received message : " + receiveMessageStr);

            clientSocket.close();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
