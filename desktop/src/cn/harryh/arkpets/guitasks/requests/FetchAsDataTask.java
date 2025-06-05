/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.network.Connections;
import cn.harryh.arkpets.network.NetworkUtils;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Objects;

import static cn.harryh.arkpets.Const.httpBufferSizeDefault;
import static cn.harryh.arkpets.Const.httpTimeoutDefault;


/** The task fetches the given remote content and parses it as JSON API response data.
 * @implNote Implement the {@link #onReceivedData(JSONObject)} to retrieve the downloaded data.
 */
abstract public class FetchAsDataTask extends GuiTask {
    private final ByteArrayOutputStream response;

    public FetchAsDataTask(StackPane parent, GuiTaskStyle style) {
        super(parent, style);
        this.response = new ByteArrayOutputStream();
    }

    /** Returns the remote path from which the data will be fetched.
     * @return The remote path to be fetched.
     */
    abstract protected String getRemotePath();

    /** Called when the data has been successfully fetched and parsed.
     * @param json The fetched data object, already parsed to JSON.
     */
    abstract protected void onReceivedData(JSONObject json);

    @Override
    protected final Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                URL remoteURL = new URL(getRemotePath());

                Logger.info("Network", "Fetching " + remoteURL);
                this.updateMessage("正在尝试建立连接");

                NetworkUtils.BufferLog log = new NetworkUtils.BufferLog(httpBufferSizeDefault);
                HttpsURLConnection connection = Connections.createHttpsConnection(remoteURL, httpTimeoutDefault);
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream os = response;
                response.reset();
                BufferedInputStream bis = new BufferedInputStream(is, httpBufferSizeDefault);
                BufferedOutputStream bos = new BufferedOutputStream(os, httpBufferSizeDefault);

                try (bis; bos; is; os) {
                    int len = httpBufferSizeDefault;
                    long sum = 0;
                    long max = connection.getContentLengthLong();
                    byte[] bytes = new byte[len];
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        sum += len;
                        log.receive();
                        long speed = log.getSpeedPerSecond(500);
                        this.updateMessage("当前已下载：" + StringUtils.getFormattedSizeString(sum) +
                                (speed != 0 ? " (" + StringUtils.getFormattedSizeString(speed) + "/s)" : ""));
                        this.updateProgress(sum, max);
                        if (this.isCancelled()) {
                            this.updateMessage("下载进程已被取消");
                            break;
                        }
                    }
                    this.updateProgress(max, max);
                    bos.flush();
                    Logger.info("Network", "Fetched " + remoteURL + " , size: " + sum);
                }
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
    }

    @Override
    protected void onSucceeded(boolean result) {
        String responseString;
        try {
            if (response.size() == 0)
                throw new IOException("Empty response");
            responseString = response.toString(Const.charsetDefault);
            onReceivedData(Objects.requireNonNull(JSONObject.parseObject(responseString)));
        } catch (IOException | JSONException | NullPointerException e) {
            Logger.error("Network", "Failed to parse response as JSON, details see below.", e);
        }
    }
}
