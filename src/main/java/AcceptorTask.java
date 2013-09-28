import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public final class AcceptorTask implements Runnable {

    private final IAcceptorListener listener;
    private final int port;
    
    public AcceptorTask(IAcceptorListener listener, int port) {
        this.listener = listener;
        this.port = port;
    }
    
    @Override
    public void run() {
        System.out.println("Attempting to listen on port [" + port + "]");
        try {
            final ServerSocketChannel ssc = ServerSocketChannel.open();
            try {
                ssc.socket().bind(new InetSocketAddress(port), 1);
                System.out.println("Accepting on port [" + port + "]");
                final SocketChannel socketChannel = ssc.accept();
                System.out.println("Accepted connection from [" + socketChannel.socket().getRemoteSocketAddress() + "]");
                listener.notifyAccept(port, socketChannel);
            } finally {
                // close server socket channel (ignore exceptions)
                try {
                    ssc.close();
                } catch(IOException ignored) {                  
                }
            }
        } catch(IOException ioe) {
            System.out.println("Acceptor exception (" + ioe.getMessage() + ")");
            listener.notifyAcceptorException(port, ioe);
        }
    }
    
}
