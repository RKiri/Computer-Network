import java.io.*;
import java.net.ServerSocket;

class RawHTTPServer {

    public static void main(String[] argv) throws IOException {

        ServerSocket socketServer = new ServerSocket(8001);

        // Main Thread
        while (true) {//主线程 派发线程 不断从Linux内核做系统调用 accept
            // Blocking... pending queue没有请求 造成阻塞
            // Thread--->Sleep ---> Other Threads 控制权交给其他线程 若整个进程没有线程在执行 控制权交出去
            //阻塞不代表CPU空转 当前线程停止执行
            var socket = socketServer.accept();
            System.out.println("A socket created");

            //var java11之后 做类型推导 不用定义类型
            var iptStream = new DataInputStream(socket.getInputStream());
            var bfReader = new BufferedReader(new InputStreamReader(iptStream));//buffer增加一层

            var requestBuilder = new StringBuilder();//读请求

            String line = "";

            // Readline -> 读到line end '\n' 停止 读不到阻塞
            while (!(line = bfReader.readLine()).isBlank()) {//读一行 判断是否空
                requestBuilder.append(line + '\n');//换行 防止缩成一行
            }

            var request = requestBuilder.toString();
            System.out.println(request);

            var bfWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bfWriter.write("HTTP/1.1 200 ok\n\nHello World!\n");//1.1 协议版本
            //\n 这一行的换行 \n HTTP要求协议头和body之间要多一个换行符
            bfWriter.flush();
            socket.close();
        }

    }

}