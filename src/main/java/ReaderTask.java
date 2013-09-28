import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public final class ReaderTask implements Runnable {

    private final IReaderListener listener;
    private final int port;
    private final SocketChannel socketChannel;
    
    public ReaderTask(IReaderListener listener, int port, SocketChannel socketChannel) {
        this.listener = listener;
        this.port = port;
        this.socketChannel = socketChannel;
    }
    
    @Override
    public void run() {
        try {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1500);
                while(socketChannel.read(buffer) != -1) {
                    buffer.rewind();
                    listener.notifyRead(port, buffer);
                    buffer = ByteBuffer.allocate(1500);
                }
            } finally {
                // close socket ignoring any exception
                try {
                    socketChannel.close();
                } catch(IOException ignored) {              
                }
            }
            listener.notifyEndOfStream(port);
        } catch(IOException ioe) {
            listener.notifyReaderException(port, ioe);
        }
    }

}
