import java.io.IOException;
import java.nio.channels.SocketChannel;


public interface IAcceptorListener {

    void notifyAccept(int port, SocketChannel socketChannel);
    
    void notifyAcceptorException(int port, IOException ioe);
    
}
