package Step3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;

public class Response {
    Socket socket;
    private int status;

    static HashMap<Integer, String> codeMap;
    public Response(Socket socket) {
        this.socket = socket;
        if(codeMap == null) {//单例
            codeMap = new HashMap<>();
            codeMap.put(200, "OK");
        }

    }

    public void send(String msg) throws IOException {//给客户端发送msg
        var resp = "HTTP/1.1 " + this.status + " " + this.codeMap.get(this.status) + "\n";
        //当前状态和状态码
        resp += "\n";
        resp += msg;
        this.sendRaw(resp);
    }

    public void sendRaw(String msg) throws IOException {//更原始
        var bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedWriter.write(msg);
        bufferedWriter.flush();
        socket.close();//HTTP请求 发一次就close
    }

}
