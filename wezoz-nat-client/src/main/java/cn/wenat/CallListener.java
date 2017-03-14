package cn.wenat;

public interface CallListener {

	/**
	 * 状态改变
	 * 
	 * @param info
	 */
	public void statusCall(String info);

	/**
	 * 事件回调
	 * 
	 * @param info
	 */
	public void eventCall(String info);

	/**
	 * 流量统计
	 * 
	 * @param traffic
	 */
	public void trafficCall(long traffic);

	public void speedCall(long speed);

	/**
	 * 关闭
	 */
	public void onClose();

	public void ping(long ms);

}