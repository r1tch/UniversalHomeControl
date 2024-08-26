package hu.evolver.uhc.comm;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class SimpleTcpClient implements Runnable {
    private enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private Thread thread;
    private Socket socket;
    private String host;
    private int port;
    private State state;
    private PrintWriter outStream = null;
    private Listener listener;

    public interface Listener {
        void onTcpConnected();

        void onTcpMessage(final String message);

        void onTcpDisconnected(boolean perRequest);
    }

    public SimpleTcpClient(Listener listener) {
        this.host = "";
        this.port = 0;
        this.listener = listener;
        this.state = State.DISCONNECTED;
    }

    public void connect(final String host, final int port) {
        this.host = host;
        this.port = port;

        if (state != State.DISCONNECTED)
            return;

        thread = new Thread(this);
        thread.start();
    }

    public void disconnect() {
        if (state == State.DISCONNECTED)
            return;

        state = State.DISCONNECTED;
        thread.interrupt();

        if (outStream != null)
            outStream.close();

        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        listener.onTcpDisconnected(true);
        thread = null;
    }

    public boolean isConnected() {
        return state == State.CONNECTED;
    }

    public void send(final String message) {
        if (outStream == null)
            return;

        outStream.print(message);
        outStream.flush();
    }

    // worker thread
    @Override
    public void run() {
        state = State.CONNECTING;
        try {
            Log.d("SimpleTcpClient", "Thread" +  Thread.currentThread().getId() + " Connecting to " + host);
            socket = SocketFactory.getDefault().createSocket(host, port);
            Log.d("SimpleTcpClient", "...onTcpConnected - localport: " + socket.getLocalPort());
            final boolean autoFlush = false;
            outStream = new PrintWriter(socket.getOutputStream(), autoFlush);
            state = State.CONNECTED;
            listener.onTcpConnected();
        } catch (UnknownHostException e) {
            Log.e("SimpleTcpClient", "Unknown host: " + host);
            e.printStackTrace();
            state = State.DISCONNECTED;
            return;
        } catch (IOException e) {
            e.printStackTrace();
            state = State.DISCONNECTED;
            listener.onTcpDisconnected(false);
            return;
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("SimpleTcpClient", "thread:"  + Thread.currentThread().getId() + " Got line: " + line);
                listener.onTcpMessage(line);
            }


        } catch (SocketException e) {
            Log.d("SimpleTcpClient", "SocketException on " + host + ":" + e.getMessage());
            if (Thread.interrupted())        // means disconnected() was called
                return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SimpleTcpClient", "Host " + host + " disconnected.");
        state = State.DISCONNECTED;
        listener.onTcpDisconnected(false);
    }

}
