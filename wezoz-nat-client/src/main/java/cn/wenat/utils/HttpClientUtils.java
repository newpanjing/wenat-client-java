package com.wezoz.nat.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.wezoz.nat.Response;

/**
 * http请求工具 httpclient4.5
 * 
 * @author panjing
 * @project wootop-common
 * @date 2016年4月19日 上午11:20:43
 */
@SuppressWarnings("deprecation")
public class HttpClientUtils {

	private static Log log = LogFactory.getLog(HttpClientUtils.class);


	public final static String DEFAULT_ENCODING = "UTF-8";

	public final static String CONTENT_TYPE = "Content-Type";

	public final static String TEXT_HTML = "text/html";

	public final static int STATUS_CODE_SUCCESS = 200;

	private static CloseableHttpClient httpClient;

	private static PoolingHttpClientConnectionManager connMgr;

	private static RequestConfig requestConfig;

	private static final int MAX_TIMEOUT = 7000;

	static {
		// 设置连接池
		connMgr = new PoolingHttpClientConnectionManager();
		// 设置连接池大小
		connMgr.setMaxTotal(100);
		connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());

		RequestConfig.Builder configBuilder = RequestConfig.custom();
		// 设置连接超时
		configBuilder.setConnectTimeout(MAX_TIMEOUT);
		// 设置读取超时
		configBuilder.setSocketTimeout(MAX_TIMEOUT);
		// 设置从连接池获取连接实例的超时
		configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
		// 在提交请求之前 测试连接是否可用
		configBuilder.setStaleConnectionCheckEnabled(true);
		requestConfig = configBuilder.build();
	}

	public static CloseableHttpClient getClient() {

		if (httpClient == null) {
			//不重定向处理
			RequestConfig config = RequestConfig.custom().setConnectTimeout(100000).setConnectionRequestTimeout(100000).setSocketTimeout(100000).setRedirectsEnabled(false).build();
			
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			
		}
		return httpClient;
	}

	/**
	 * get请求
	 * 
	 * @param url
	 * @return
	 */
	public static Response get(String url) {

		HttpGet httpGet = new HttpGet(url);
		return get(httpGet);
	}

	/**
	 * get请求
	 * 
	 * @param httpGet
	 * @return
	 */
	public static Response get(HttpGet httpGet) {
		Response response = new Response();
		try {
			CloseableHttpResponse res = getClient().execute(httpGet);
			response = getContent(res);

		} catch (SocketTimeoutException e) {
			log.error("Read timed out:" + httpGet.getURI());
			response.setStatusCode(502);
			response.setStatusMessage("读取超时");
		} catch (Exception e) {
			response.setStatusCode(500);
			response.setStatusMessage("请求本地服务器出错");
			log.error(e);
		}
		return response;
	}

	/**
	 * post请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static Response post(String url, Map<String, Object> params) {

		HttpPost httpPost = new HttpPost(url);

		return post(httpPost, params);
	}

	public static Response postSSL(String url, Map<String, Object> params) {

		HttpPost httpPost = new HttpPost(url);
		return postSSL(httpPost, params);
	}

	/**
	 * post请求
	 * 
	 * @param httpPost
	 * @param params
	 * @return
	 */
	public static Response post(HttpPost httpPost, Map<String, Object> params) {
		Response res = null;
		try {
			if (params != null) {
				List<BasicNameValuePair> basicNameValuePairs = new ArrayList<BasicNameValuePair>();

				Set<String> keySet = params.keySet();
				for (String key : keySet) {
					Object obj = params.get(key);
					String value = null;
					if (obj != null) {
						value = String.valueOf(obj);
					}
					basicNameValuePairs.add(new BasicNameValuePair(key, value));
				}

				HttpEntity httpEntity = new UrlEncodedFormEntity(basicNameValuePairs, DEFAULT_ENCODING);

				httpPost.setEntity(httpEntity);
			}
			CloseableHttpResponse response = getClient().execute(httpPost);
			res = getContent(response);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return res;
	}

	public static Response postBody(HttpPost httpPost, String body,String encoding) {
		Response res = null;
		try {
			httpPost.setEntity(new StringEntity(body, encoding));
			CloseableHttpResponse response = getClient().execute(httpPost);
			res = getContent(response);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return res;
	}

	public static Response gzipPost(String url, Map<String, Object> params) {
		HttpPost httpPost = new HttpPost(url);
		return gzipPost(httpPost, params);
	}

	public static Response gzipPost(HttpPost httpPost, Map<String, Object> params) {

		Response res = null;
		try {
//			httpPost.addHeader("User-Agent", DEFAULT_USER_AGENT);
			httpPost.addHeader("Content-Encoding", "gzip");
			if (params != null) {
				List<BasicNameValuePair> basicNameValuePairs = new ArrayList<BasicNameValuePair>();

				Set<String> keySet = params.keySet();
				for (String key : keySet) {
					Object obj = params.get(key);
					String value = null;
					if (obj != null) {
						value = String.valueOf(obj);
					}
					basicNameValuePairs.add(new BasicNameValuePair(key, value));
				}

				// HttpEntity httpEntity = new
				// UrlEncodedFormEntity(basicNameValuePairs, DEFAULT_ENCODING);
				String str = JSON.toJSONString(params);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gos = new GZIPOutputStream(baos);
				gos.write(str.getBytes("utf-8"));
				gos.flush();

				byte[] bs = baos.toByteArray();
				HttpEntity httpEntity = new ByteArrayEntity(bs);

				httpPost.setEntity(httpEntity);
			}
			CloseableHttpResponse response = getClient().execute(httpPost);
			res = getContent(response);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return res;
	}

	public static Response postSSL(HttpPost httpPost, Map<String, Object> params) {

		Response res = null;
		try {
//			httpPost.addHeader("User-Agent", DEFAULT_USER_AGENT);
			List<BasicNameValuePair> basicNameValuePairs = new ArrayList<BasicNameValuePair>();

			Set<String> keySet = params.keySet();
			for (String key : keySet) {
				Object obj = params.get(key);
				String value = null;
				if (obj != null) {
					value = String.valueOf(obj);
				}
				basicNameValuePairs.add(new BasicNameValuePair(key, value));
			}
			HttpEntity httpEntity = new UrlEncodedFormEntity(basicNameValuePairs, DEFAULT_ENCODING);

			httpPost.setEntity(httpEntity);
			URI uri = httpPost.getURI();
			new SSLHttpClient().registerSSL(uri.getHost(), "https", uri.getPort(), uri.getScheme());
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			res = getContent(response);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return res;
	}

	private static SSLConnectionSocketFactory createSSLConnSocketFactory() {

		SSLConnectionSocketFactory sslsf = null;
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {

					return true;
				}
			}).build();
			sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

				@Override
				public boolean verify(String arg0, SSLSession arg1) {

					return true;
				}

				@Override
				public void verify(String host, SSLSocket ssl) throws IOException {

				}

				@Override
				public void verify(String host, X509Certificate cert) throws SSLException {

				}

				@Override
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {

				}
			});
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		return sslsf;
	}

	// 功能: postBody形式发送数据
	// @param urlPath 对方地址
	// @param json 要传送的数据
	// @return
	// @throws Exception
	public static String postBody(String urlPath, String data) throws Exception {

		// Configure and open a connection to the site you will send the
		// request
		URL url = new URL(urlPath);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		// 设置doOutput属性为true表示将使用此urlConnection写入数据
		urlConnection.setDoOutput(true);
		// 定义待写入数据的内容类型，我们设置为application/x-www-form-urlencoded类型
		urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
		// 得到请求的输出流对象
		OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
		// 把数据写入请求的Body
		out.write(data);
		out.flush();
		out.close();
		// 从服务器读取响应
		InputStream inputStream = urlConnection.getInputStream();
		String encoding = urlConnection.getContentEncoding();
		String body = IOUtils.toString(inputStream, encoding);
		if (urlConnection.getResponseCode() != 200) {
			throw new Exception(body);
		}

		return body;
	}

	public static String getEncoding(CloseableHttpResponse response) {

		String encoding = null;

		Header[] headers = response.getHeaders(CONTENT_TYPE);
		if (headers != null && headers.length > 0) {
			Header header = headers[0];
			String value = header.getValue();
			value = value.replaceAll(" ", "");
			value = value.toLowerCase();
			int index = 0;
			if ((index = value.lastIndexOf("charset")) != -1) {
				String[] array = value.substring(index).split("=");
				if (array.length > 1) {
					encoding = array[1];
				}
			}
		}

		return encoding;
	}

	/**
	 * 获取响应内容
	 * 
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static Response getContent(CloseableHttpResponse response) throws ParseException, IOException {

		Response res = new Response();
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return res;
		}

		byte[] bytes = readBytes(entity.getContent());
		String encoding = getEncoding(response);
		if (encoding == null) {
			encoding = "utf-8";
		}
		res.setBody(bytes);
		res.setEncoding(encoding);
		res.setStatusCode(response.getStatusLine().getStatusCode());
		
		Map<String, String> headersMap = new HashMap<String, String>();
		Header[] headers = response.getAllHeaders();
		for (Header header : headers) {
			headersMap.put(header.getName(), header.getValue());
		}
		res.setHeaders(headersMap);

		// content = new String(bytes, encoding);
		EntityUtils.consume(response.getEntity());
		response.close();

		return res;
	}

	/**
	 * 将流读取为字节数组
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytes(InputStream in) throws IOException {

		BufferedInputStream bufin = new BufferedInputStream(in);
		int buffSize = 1024;
		ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize);

		byte[] temp = new byte[buffSize];
		int size = 0;
		while ((size = bufin.read(temp)) != -1) {
			out.write(temp, 0, size);
		}
		bufin.close();

		byte[] content = out.toByteArray();
		return content;
	}

	public static void main(String[] args) {
		System.out.println(get("http://localhost:8080/wootop-doctor/"));
	}
}
