import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client {
    SocketChannel socketChannel;
    InetSocketAddress inetSocketAddress;
    public Client(SocketChannel socketChannel, InetSocketAddress inetSocketAddress){
        this.socketChannel = socketChannel;
        this.inetSocketAddress = inetSocketAddress;
    }
}
