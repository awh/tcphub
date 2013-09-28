import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class TCPHub implements IAcceptorListener, IReaderListener {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    
    private final int northboundPort;
    private final int southboundPort;
    
    private final Map<Integer, SocketChannel> socketChannelMap = new HashMap<Integer, SocketChannel>(); 

    public TCPHub(int northboundPort, int southboundPort) {
        this.northboundPort = northboundPort;
        this.southboundPort = southboundPort;
    }
    
    public synchronized void start() {
        executorService.execute(new AcceptorTask(this, northboundPort));
        executorService.execute(new AcceptorTask(this, southboundPort));
    }
    
    public synchronized void stop() {
        executorService.shutdownNow();
    }

    @Override
    public synchronized void notifyAccept(int port, SocketChannel socketChannel) {
        socketChannelMap.put(port, socketChannel);
        executorService.execute(new ReaderTask(this, port, socketChannel));
    }

    @Override
    public synchronized void notifyAcceptorException(int port, IOException ioe) {
        System.out.println("Relaunching acceptor in 10s");
        try {
            Thread.sleep(10000);
            executorService.execute(new AcceptorTask(this, port));
        } catch(InterruptedException ignored) {         
        }
    }


    @Override
    public void notifyEndOfStream(int port) {
        socketChannelMap.remove(port);
        executorService.execute(new AcceptorTask(this, port));
    }
    
    @Override
    public synchronized void notifyRead(int port, ByteBuffer buffer) {
        final int oppositePort = port == northboundPort ? southboundPort : northboundPort;
        final SocketChannel socketChannel = socketChannelMap.get(oppositePort);
        
        if(socketChannel != null) {
            try {
                socketChannel.write(buffer);
            } catch(IOException ignored) {
            }
        }
    }

    @Override
    public synchronized void notifyReaderException(int port, IOException ioe) {
        socketChannelMap.remove(port);
        executorService.execute(new AcceptorTask(this, port));
    }
    
    public static void main(String[] args) throws Exception {       
        final TCPHub tcpHub = new TCPHub(4000, 4001);
        
        tcpHub.start();
        
        Thread.sleep(60000);
        
        tcpHub.stop();
        
    }
    
}
