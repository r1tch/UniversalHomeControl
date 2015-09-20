package hu.rits.remotewakeup;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class SimpleTcpClient implements Runnable {
    private Thread thread;
    private Socket socket;
    private String host;
    private int port;
    private State state;
    private PrintWriter outStream;
    private Listener listener;

    public interface Listener {
        void onConnected();

        void onMessage(final String message);

        void onDisconnected(boolean perRequest);
    }

    public SimpleTcpClient(final String host, final int port, Listener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
        this.state = State.DISCONNECTED;
    }

    public void connect() {
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

        outStream.close();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        listener.onDisconnected(true);
        thread = null;
    }

    public boolean isConnected() {
        return state == State.CONNECTED;
    }

    public void send(final String message) {
        outStream.print(message);
        outStream.flush();
    }

    // worker thread
    @Override
    public void run() {
        state = State.CONNECTING;
        try {
            Log.d("SimpleTcpClient", "Connecting to " + host);
            socket = SocketFactory.getDefault().createSocket(host, port);
            Log.d("SimpleTcpClient", "...connected.");
            final boolean autoFlush = false;
            outStream = new PrintWriter(socket.getOutputStream(), autoFlush);
            state = State.CONNECTED;
            listener.onConnected();
        } catch (UnknownHostException e) {
            Log.e("SimpleTcpClient", "Unknown host: " + host);
            e.printStackTrace();
            state = State.DISCONNECTED;
            return;
        } catch (IOException e) {
            e.printStackTrace();
            state = State.DISCONNECTED;
            listener.onDisconnected(false);
            return;
        }

        outStream.println("Hello");
        outStream.flush();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("SimpleTcpClient", "Got line: " + line);
                listener.onMessage(line);
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
        listener.onDisconnected(false);
    }

    private enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }
}
