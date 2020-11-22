import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class TCPServer {

    public static void main(String[] args) {

        int ServerPort = 6789;
        try {
            ServerSocket welcomeSocket = new ServerSocket(ServerPort); //侦听与此套接字的连接并且接受它. 在建立连接之前,该方法将阻塞.
 
            while (true) {
                Socket serverSocket = welcomeSocket.accept();
                System.out.println( "the client hostname is : " +serverSocket.getInetAddress().getHostName());
                System.out.println("the client port is : " +serverSocket.getPort());

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(serverSocket.getInputStream())); //字符输入流
                
                // 要注意:接收数据方法readLine要读到换行符才认为读一行结束,所以发送过去的消息一定要带上一个换行符 '\n'
                String receiveMessageStr = inFromClient.readLine(); //接收Client发来的数据
                System.out.println("receive message : " + receiveMessageStr);
                String sendMessageStr = receiveMessageStr.toUpperCase(); //转换成大写

                DataOutputStream outToClient = new DataOutputStream(serverSocket.getOutputStream()); //字节输出流
                outToClient.writeBytes(sendMessageStr+ '\n');

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

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
