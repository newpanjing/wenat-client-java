>WeNAT是一个穿透内网的工具，利用wezoz的服务器，和本地电脑，建立一条专属的通道，并将外网的请求转发到本地，从而实现穿透。
>常见应用：支付接口回调调试、微信接口、个人电脑搭建网站
####示例
>本地地址为： `http://localhost:8080/abc`
>远程地址为：`http://github.wezoz.com/abc`
>当访问`http://github.wezoz.com/abc`的时候，请求会被转发到`http://localhost:8080/abc`，实现内网可外网访问。

####特性
1. 支持post、get、以及postBody(json/xml),文件上传暂时不支持，比较耗带宽
2. 基于socket.io，支持断线重连
3. 自定义域名(wezoz.com二级域名)

####网站
>https://www.wezoz.com
