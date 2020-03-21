import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;

public class Client {
    private int serverPort;
    private String serverIp;
    private SocketChannel socketChannel;


    public static void main(String[] args) {
        Client client = new Client(9999,"127.0.0.1");
        client.start();

    }

    private Client(int serverPort, String serverIp){
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        socketChannel = null;
    }

    private void start(){
        try{
            //create socket that connect to server
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverIp,serverPort)) ;
            if (socketChannel.finishConnect()) {
                communicate();
            }else {
                System.out.println("not connect");
            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void communicate()throws  IOException{

        talk();

        listen();

    }

    private void talk() {
        new Thread(()->{
            //get the input of client from windows
            InputStream inputStream = new BufferedInputStream(System.in);
            byte[] bytes = new byte[1024];
            while (true) {
                try {
                    int n = inputStream.read(bytes);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    byteBuffer.put(Arrays.copyOf(bytes, n));
                    byteBuffer.flip();
                    socketChannel.write(byteBuffer);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void listen() throws  IOException{

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (true){

            byteBuffer.clear();
            int n = socketChannel.read(byteBuffer);

            if (n > 0){

                byteBuffer.flip();

                for (int i = 0; i < n; i++){
                    System.out.print((char) byteBuffer.get());
                }
                System.out.println();
            }
        }
    }
}
