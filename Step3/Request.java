package Step3;

import org.apache.commons.httpclient.HttpParser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Request {
    static Pattern methodRegex = Pattern.compile("(GET|PUT|POST|DELETE|OPTIONS|TRACE|HEAD)");//可能的方法

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    private final String body;
    private final String method;
    private final HashMap<String, String> headers;

    public Request(Socket socket) throws IOException {
        // DataInputStream -> primitives(Char, Float)
        // 各种各样数据接口 继承于InputStream 之上做了些封装 提升了能力
        // InputStream -> bytes 只有
        var iptStream = new DataInputStream(socket.getInputStream());
        var bfReader = new BufferedReader(new InputStreamReader(iptStream));
        //建buffer 读取时形成缓冲区 提升读取效率 readLine能力需要建立在buffer上

        // GET /path HTTP/1.1
        var methodLine = HttpParser.readLine(iptStream, "UTF-8");
        //读出一行 提出get post方法 正则表达式匹配
        var m = methodRegex.matcher(methodLine);
        m.find();//找到一个
        var method = m.group();//到底是什么方法

        // Content-Type:xxxx    冒号的Key-Value形式
        // Length : xxx
        var headers = HttpParser.parseHeaders(iptStream, "UTF-8");//提供了解析header的能力
        var headMap = new HashMap<String, String>();
        for(var h : headers) {
           headMap.put(h.getName(), h.getValue());
        }

        var bufferReader = new BufferedReader(new InputStreamReader(iptStream));
        var body = new StringBuilder();
        char[] buffer = new char[1024];

        while(iptStream.available() > 0) {//如果还有 可用的 字符
            bufferReader.read(buffer);
            body.append(buffer);
        }

        this.body = body.toString();
        this.method = method;
        this.headers = headMap;

    }
}
