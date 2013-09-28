import java.io.IOException;
import java.nio.ByteBuffer;


public interface IReaderListener {

    void notifyRead(int port, ByteBuffer buffer);
    
    void notifyEndOfStream(int port);
    
    void notifyReaderException(int port, IOException ioe);
    
}
