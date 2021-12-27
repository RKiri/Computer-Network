package server.Step4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class Step4Server {

    ServerSocketChannel ssc;

    public void listen(int port) throws IOException {
        ssc = ServerSocketChannel.open();//工厂方法 open相当于赋值了
        ssc.bind(new InetSocketAddress(port));

        // Reactive / Reactor
        ssc.configureBlocking(false);

        var selector = Selector.open();
        //负责把所有socket注册在他上，一旦socket发生了变动
        // 如读取完成 通知对应工作线程
        //提供了个注册消息的地方 公布消息 把消息通知工作线程

        ssc.register(selector, ssc.validOps(), null);
        //selector 注册到serverSocketChannel上
        //允许所有的操作 读取、写入

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 16);//读数据 16k

        for (; ; ) {//和while(true)比节省一次判断
            int numOfKeys = selector.select();
            //通过select方法 请求进来能拿到一个key
            Set selectedKeys = selector.selectedKeys();
            Iterator it = selectedKeys.iterator();//迭代器

            while (it.hasNext()) {
                var key = (SelectionKey) it.next();
                //有多种情况

                if (key.isAcceptable()) {
                    var channel = ssc.accept();//channel里面代表的是文件描述符或socket
                    if (channel == null) {//异步编程模型 再判断下
                        continue;
                    }

                    // Kernel -> mmap(内存映射 buffer) -> Channel -> User(Buffer)
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                    //注册到selector上 发生OP_READ时通知 外层for循环读到对应的key
                } else {
                    var channel = (SocketChannel) key.channel();

                    // _ _ _ _ _ _ _
                    //         P(position)指针告诉现在buffer在什么位置
                    //         L
                    buffer.clear();//想重复利用 position清零 O(1)的操作
                    channel.read(buffer);
                    String request = new String(buffer.array());
                    // Logic... 如超过buffer大小
                    buffer.clear();
                    buffer.put("HTTP/1.1 200 ok\n\nHello NIO!!".getBytes());
                    // H T T P / 1 ... ! _  _
                    //                   P(L)
                    // P                 L (limit buffer里到底有多少数据 计数指针) 正常写入position和limit相同
                    buffer.flip();
                    channel.write(buffer);//传回给内核
                    channel.close();
                }
            }
        }
    }

    public static void main(String[] argv) throws IOException {
        var server = new Step4Server();
        server.listen(8001);
    }
}



