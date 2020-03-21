import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private int port;
    private Selector selector;
    private ArrayList<Client> clients;


    public static void main(String[] args) {
        Server server = new Server(9999);
        server.start();

    }

    public Server(int port){
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start(){
        try {
            selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

           // ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (true){
                int events = selector.select();
               // System.out.println(events);
                if (events > 0){
                    Set<SelectionKey> selectionKeys =  selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey selectionKey = iterator.next();

                        //to avoid duplicate access the selectionkey next time.
                        iterator.remove();
                    //    System.out.println(selectionKey.interestOps());

                        if (selectionKey.isAcceptable()){  //难道accept事件也包含read ， write吗，试试？
                            accept(selectionKey);

                        }else{
                           // System.out.println(selectionKey.isAcceptable());
                            communicate(selectionKey);
                        }
                    }
                }

            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey selectionKey) throws IOException{
        //record socket address of  a new client
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();

        if(socketChannel == null){
            return;
        }

        socketChannel.configureBlocking(false);

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();

        Client client = new Client(socketChannel,inetSocketAddress);
        clients.add(client);

        socketChannel.register(selector,SelectionKey.OP_READ);
        System.out.println("welcome new client " + socketChannel.socket().getInetAddress().getAddress() );


    }

    private  void communicate(SelectionKey selectionKey) throws  IOException{
        //socketchannel, not serversocketchannel.
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();

        //broadcast message to all clients except talker
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int n = socketChannel.read(byteBuffer);
        byteBuffer.flip();

        if (n==0){
            return;
        }else {
            for (int i = 0;i<n;i++){
                System.out.print((char) byteBuffer.get());
            }
        }
       /* System.out.println((char) byteBuffer.get());
        System.out.println(byteBuffer);*/

        for ( Client client : clients){
            if (!client.inetSocketAddress.equals(inetSocketAddress)){

                //read the message again
                byteBuffer.rewind();

                SocketChannel socketChannel1 = client.socketChannel;
                socketChannel1.write(byteBuffer);
            }
        }
    }


}
