/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class Connections {
    public static boolean trustAllUnsafe = false;

    /** Creates an HTTPS connection of the given URL, and then try to connect it.
     * @param url The URL to connect.
     * @param connectTimeout The timeout of the connection (ms).
     * @param readTimeout The timeout of the reading operation (ms).
     * @return The connection instance which has finished connecting.
     * @throws IOException If I/O error occurs. Typically, when a timeout occurred or the response code wasn't like 2XX.
     */
    public static HttpsURLConnection createHttpsConnection(URL url, int connectTimeout, int readTimeout)
            throws IOException {
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            if (trustAllUnsafe) {
                connection.setSSLSocketFactory(TrustUtils.getTrustAnySSLSocketFactory());
                connection.setHostnameVerifier(TrustUtils.getTrustAnyHostnameVerifier());
            }
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.connect();
            HttpResponseCode responseCode = new HttpResponseCode(connection);
            if (responseCode.type != HttpResponseCodeType.SUCCESS)
                throw new HttpResponseCodeException(responseCode);
            return connection;
        } catch (IOException e) {
            try {
                if (connection != null && connection.getInputStream() != null)
                    connection.getInputStream().close();
            } catch (Exception ignored) {
            }
            throw e;
        }
    }

    /** Creates an HTTPS connection of the given URL, and then try to connect it.
     * @param url The URL to connect.
     * @param timeout The timeout for connecting and reading (ms).
     * @return The connection instance which has finished connecting.
     * @throws IOException If I/O error occurs. Typically, when a timeout occurred or the response code wasn't like 2XX.
     */
    public static HttpsURLConnection createHttpsConnection(URL url, int timeout)
            throws IOException {
        return createHttpsConnection(url, timeout, timeout);
    }


    public enum HttpResponseCodeType {
        /** Indicates an invalid HTTP response */
        UNKNOWN,
        /** Indicates a {@code 1xx} HTTP response code */
        INFORMATION,
        /** Indicates a {@code 2xx} HTTP response code */
        SUCCESS,
        /** Indicates a {@code 3xx} HTTP response code */
        REDIRECTION,
        /** Indicates a {@code 4xx} HTTP response code */
        CLIENT_ERROR,
        /** Indicates a {@code 5xx} HTTP response code */
        SERVER_ERROR
    }


    public static class HttpResponseCode {
        public final int code;
        public final String message;
        public final HttpResponseCodeType type;

        public HttpResponseCode(int code, String message) {
            this.code = code;
            this.message = message;
            HttpResponseCodeType type;
            if (100 <= code && code < 200)
                type = HttpResponseCodeType.INFORMATION;
            else if (200 <= code && code < 300)
                type = HttpResponseCodeType.SUCCESS;
            else if (300 <= code && code < 400)
                type = HttpResponseCodeType.REDIRECTION;
            else if (400 <= code && code < 500)
                type = HttpResponseCodeType.CLIENT_ERROR;
            else if (500 <= code && code < 600)
                type = HttpResponseCodeType.SERVER_ERROR;
            else
                type = HttpResponseCodeType.UNKNOWN;
            this.type = type;
        }

        public HttpResponseCode(HttpURLConnection connection)
                throws IOException {
            this(connection.getResponseCode(), connection.getResponseMessage());
        }
    }


    public static class HttpResponseCodeException extends IOException {
        private final HttpResponseCode responseCode;

        public HttpResponseCodeException(HttpResponseCode responseCode) {
            this.responseCode = responseCode;
        }

        public int getCode() {
            return responseCode.code;
        }

        @Override
        public String getMessage() {
            return responseCode.code + ": " + responseCode.message;
        }

        public HttpResponseCodeType getType() {
            return responseCode.type;
        }
    }
}
