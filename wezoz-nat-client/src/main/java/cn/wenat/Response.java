package cn.wenat;

import java.io.Serializable;
import java.util.Map;

public class Response implements Serializable{

	private static final long serialVersionUID = 5431808741731247591L;

	private int statusCode = 200;// 状态码

	private String statusMessage = "ok";// 状态消息

	private Map<String, String> headers;// 请求头

	private String encoding;// 编码

	private Object body;// 响应内容

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "Response [statusCode=" + statusCode + ", statusMessage=" + statusMessage + ", headers=" + headers + ", encoding=" + encoding + ", body={}]";
	}

}
