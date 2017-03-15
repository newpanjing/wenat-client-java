package cn.wenat;

import cn.wenat.form.MainForm;
import cn.wenat.utils.HttpClientUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.util.*;

public class LocalServer {

    private static final String HTTP_REQUEST = "httpRequest";

    private static final String HTTP_RESPONSE = "httpResponse";

    private static final String BIND_DOMAIN = "bindDomain";

    private static final String BIND_DOMAIN_NOTICE = "bindDomainNotice";

    private static final String PING = "clientPing";

    private static final String PING_NOTICE = "clientPingNotice";

    private Logger logger = Logger.getLogger(getClass());

    private Socket socket;

    private String server;

    private String forward;

    private String domain;

    private long traffic = 0;// 流量统计

    private long speed = 0;// 实时网速统计

    public void setDomain(String domain) {

        this.domain = domain;
    }

    public void setForward(String forward) {

        this.forward = forward;
    }

    public void setServer(String server) {

        this.server = server;
    }

    private CallListener callListener;

    public void setCallListener(CallListener callListener) {

        this.callListener = callListener;
    }

    public void ping() {

        if (socket == null) {
            return;
        }
        socket.emit(PING, System.currentTimeMillis());
    }

    public void bindDomain() {

        socket.emit(BIND_DOMAIN, domain);

    }

    public void start() throws Exception {

        Options opts = new Options();
        opts.transports = new String[]{"websocket", "polling"};
        socket = IO.socket(server, opts);

        Map<String, String> eventMapper = new HashMap<String, String>();

        eventMapper.put(Socket.EVENT_DISCONNECT, "断开连接");
        eventMapper.put(Socket.EVENT_ERROR, "断开错误");
        eventMapper.put(Socket.EVENT_CONNECTING, "正在连接服务器");
        eventMapper.put(Socket.EVENT_CONNECT_TIMEOUT, "连接服务器超时");
        eventMapper.put(Socket.EVENT_RECONNECTING, "自动重连服务器");
        eventMapper.put(Socket.EVENT_RECONNECT, "准备重连服务器");
        eventMapper.put(Socket.EVENT_CONNECT, "连接服务器成功");
        eventMapper.put(HTTP_REQUEST, null);
        eventMapper.put(BIND_DOMAIN_NOTICE, null);
        eventMapper.put(PING_NOTICE, null);
        Set<String> keys = eventMapper.keySet();
        // 注册事件和提示信息
        for (String k : keys) {
            SocketListener socketListener = new SocketListener() {

                @Override
                public void eventCall(String eventName, String message, Object... args) {

                    if (null != message) {
                        callListener.eventCall("[" + eventName + "]：" + message);
                    }
                    switch (eventName) {
                        case Socket.EVENT_CONNECT:
                            callListener.statusCall("连接服务器成功");
                            LocalServer.this.bindDomain();
                            LocalServer.this.ping();
                            break;
                        case HTTP_REQUEST:
                            handlerRequest(args);
                            break;
                        case BIND_DOMAIN_NOTICE:
                            handlerBindDomain(args);
                            break;
                        case PING_NOTICE:
                            handlerPing(args);
                            break;
                        default:
                            break;
                    }
                }

            };
            socketListener.setEventName(k);
            socketListener.setMessage(eventMapper.get(k));
            socket.on(k, socketListener);
        }
        socket.connect();

        // 注册回调事件，实时统计网络速率

        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                long temp = speed;
                speed = 0;
                callListener.speedCall(temp);

            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 1000);
        new Timer().scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                ping();
            }
        }, 0, 10000);
    }

    private void handlerPing(Object[] args) {

        long time = (long) args[0];
        long ms = System.currentTimeMillis() - time;
        callListener.ping(ms);
    }

    private void handlerBindDomain(Object... args) {

        try {
            org.json.JSONObject jsonObject = (org.json.JSONObject) args[0];
            int code = jsonObject.getInt("code");
            String msg = jsonObject.getString("msg");
            if (code == 1000) {
                // 弹出地址
            } else {
                // 断开链接
                socket.close();
                callListener.onClose();
            }
            callListener.eventCall("[绑定域名]:" + msg);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * 处理远程的http请求
     *
     * @param args
     */
    private void handlerRequest(Object... args) {

        org.json.JSONObject object = (org.json.JSONObject) args[0];
        JSONObject request = JSON.parseObject(object.toString());
        String url = request.getString("url");
        Map<String, Object> headers = request.getJSONObject("headers");
        headers.remove("content-length");
        String method = request.getString("method");
        String eventName = request.getString("eventName");

        Map<String, Object> params = request.getJSONObject("params");

        String reqUrl = forward + url;
        logger.info("收到请求： methon=" + method + " url=" + reqUrl);
        if (callListener != null) {
            callListener.eventCall("[远程请求]: methon=" + method + " url=" + reqUrl + " params:" + params + " headers:" + headers);
        }
        Response response = null;
        // 发送请求
        // 替换host
        Set<String> keys = headers.keySet();
        if ("POST".equals(method.toUpperCase())) {

            // 查看是否是json或者xml请求一类
            String encoding = "utf-8";
            String contentType = null;
            if (headers.get("content-type") != null) {
                contentType = headers.get("content-type").toString();
                String[] array = contentType.split(";");
                contentType = array[0];
                if (array.length > 1) {
                    String[] charsets = array[1].split("=");
                    if (charsets.length > 1) {
                        encoding = charsets[1];
                    }
                }
            }
            HttpPost post = new HttpPost(reqUrl);
            for (String k : keys) {
                post.addHeader(k, String.valueOf(headers.get(k)));
            }

            try {
                // 默认类型application/x-www-form-urlencoded
                if (contentType == null) {
                    contentType = "application/x-www-form-urlencoded";
                }

                // 兼容普通post和json/xml post
                if (contentType.equals("application/x-www-form-urlencoded")) {
                    response = HttpClientUtils.post(post, request.getJSONObject("body"));
                } else {
                    //其他的全部postBody
                    response = HttpClientUtils.postBody(post, request.getString("body"), encoding);
                }
            } catch (Exception e) {
                response = new Response();
                response.setStatusCode(500);
                response.setStatusMessage("本地服务器报错：" + e.getMessage());
            }

        } else if ("GET".equals(method.toUpperCase())) {
            HttpGet get = new HttpGet(reqUrl);
            for (String k : keys) {
                get.addHeader(k, String.valueOf(headers.get(k)));
            }
            try {
                response = HttpClientUtils.get(get);
            } catch (Exception e) {
                response = new Response();
                response.setStatusCode(500);
                response.setStatusMessage("本地服务器报错：" + e.getMessage());
            }
        } else {
            // 提示请求不支持
            response = new Response();
            response.setStatusCode(500);
            response.setEncoding("utf-8");
            response.setStatusMessage(method + "请求类型暂时不支持！");
        }
        org.json.JSONObject jsonObject = new org.json.JSONObject();
        try {

            // 处理重定向
            if (response.getStatusCode() == 302 || response.getStatusCode() == 307 || response.getStatusCode() == 303) {
                // 处理地址
                String localtion = response.getHeaders().get("Location");
                localtion.replace(server, domain);
                response.getHeaders().put("Location", localtion);
            }

            byte[] bytes = (byte[]) response.getBody();
            if (bytes == null) {
                bytes = new byte[]{};
            }
            long length = bytes.length;
            this.traffic += length;
            this.speed += length;
            jsonObject.put("body", response.getBody());
            jsonObject.put("headers", response.getHeaders());
            jsonObject.put("statusCode", response.getStatusCode());
            jsonObject.put("encoding", response.getEncoding());
            jsonObject.put("statusMessage", response.getStatusMessage());
            jsonObject.put("eventName", eventName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        logger.info("请求响应：" + response);
        if (callListener != null) {
            callListener.eventCall("[目标响应]:" + response);
        }
        if (socket == null) {
            callListener.eventCall("[服务关闭] The service has been closed.");
            return;
        }
        socket.emit(HTTP_RESPONSE, jsonObject);
        // 通知界面显示流量
        callListener.trafficCall(this.traffic);
    }

    public void stop() {

        if (socket != null) {
            socket.close();
            socket = null;
        }
        callListener.ping(0l);
    }

    public static void main(String[] args) throws Exception {

        new MainForm().setVisible(true);

    }
}
