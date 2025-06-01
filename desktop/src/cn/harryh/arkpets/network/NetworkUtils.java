/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;


public class NetworkUtils {
    /** Sets the system's proxy property, applying on both HTTP and HTTPS.
     * @param host The proxy host, pass empty string to disable proxy.
     * @param port The proxy port, pass empty string to disable proxy.
     */
    public static void setProxy(String host, String port) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);
    }

    /** Tests the real connection delay of the given URL (with a specified port).
     * @param url The URL to be tested.
     * @param port The port to connect.
     * @param timeoutMillis Timeout (ms).
     * @return The delay (ms). {@code -1} when connection failed or timeout.
     */
    public static int testDelay(String url, int port, int timeoutMillis) {
        Socket socket = new Socket();
        int delayMillis = -1;
        try {
            SocketAddress address = new InetSocketAddress(new URL(url).getHost(), port);
            long start = System.currentTimeMillis();
            socket.connect(address, timeoutMillis);
            long stop = System.currentTimeMillis();
            delayMillis = (int) (stop - start);
        } catch (IOException ignored) {
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        return delayMillis;
    }


    public static class BufferLog {
        protected long[] bufferTimestamps;
        protected int bufferSize;
        protected int bufferTimestampsPointer = 0;
        protected long lastCalculatedResult = 0;
        protected long lastCalculatedTime = 0;

        public BufferLog(int bufferSize, int maxBufferCount) {
            if (bufferSize <= 0 || maxBufferCount <= 0)
                throw new IllegalArgumentException("bufferSize and maxBufferCount should be positive.");
            this.bufferTimestamps = new long[maxBufferCount];
            this.bufferSize = bufferSize;
        }

        public BufferLog(int bufferSize) {
            this(bufferSize, 1024);
        }

        public void receive() {
            bufferTimestamps[bufferTimestampsPointer++] = System.currentTimeMillis();
            bufferTimestampsPointer = bufferTimestampsPointer < bufferTimestamps.length ? bufferTimestampsPointer : 0;
        }

        public long getSpeedPerSecond(int cacheTimeMillis) {
            long currentTimestamp = System.currentTimeMillis();
            if (lastCalculatedTime + cacheTimeMillis <= currentTimestamp) {
                int actualLength;
                for (actualLength = bufferTimestamps.length; actualLength > 0; actualLength--)
                    if (bufferTimestamps[actualLength - 1] != 0)
                        break;
                if (actualLength <= 1)
                    return 0;

                long maxTimestamp = bufferTimestamps[bufferTimestampsPointer != 0 ? bufferTimestampsPointer - 1 : actualLength - 1];
                long minTimestamp = bufferTimestamps[bufferTimestampsPointer < actualLength ? bufferTimestampsPointer : 0];
                if (maxTimestamp - minTimestamp < 100)
                    return 0;

                lastCalculatedResult = (actualLength - 1) * bufferSize * 1000L / (maxTimestamp - minTimestamp);
                lastCalculatedTime = currentTimestamp;
            }
            return lastCalculatedResult;
        }
    }
}
