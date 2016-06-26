# CameraViewer
视频监控系统-监控端
<h1>
	简介
</h1>
<p>
	<span style="white-space:pre"></span><span style="white-space:pre">	</span>该系统分为3个部分，<a target="_blank" href="https://github.com/pwrliang/CameraViewer">监控端</a>、<a target="_blank" href="https://github.com/pwrliang/Camera">被监控端</a>、<a target="_blank" href="https://github.com/pwrliang/Camera/tree/master/CamServer">中转服务器</a>。监控端和被监控端基于Android平台，中转服务器要部署到VPS上。功能有查看单一监控、查看多屏监控、监控录像、录像回放、图像质量调整、过滤相似帧以及扫描二维码一键添加设备等功能。
</p>
<h1>
	原理
</h1>
<h2>
	1.通信过程
</h2>
<p>
	<span style="white-space:pre"></span><span style="white-space:pre">	</span>监控端和被监控端使用TCP传输协议，分为两种情况。当被监控端可以向外网暴露端口，则监控端和被监控端直接连接。当被监控端无法暴露端口，则监控端和被监控端都连接到一个中转服务器上，监控端发出的指令先发给服务器，服务器再中转给被监控端（通过设备ID来区分发给谁）。被监控端给监控端回送消息也需要中转服务器。
</p>
<h2>
	2.数据传输
</h2>
<p>
	<span style="white-space:pre"></span><span style="white-space:pre">	</span>直接传输预览帧，在传输前会将原始YUV压缩、降低分辨率封装到Bitmap类后转换成字节数组后加入到队列，然后开启发送线程直接从队列取出图像封装到一个Data类中。Data类实现了序列化接口，共有3个成员变量：byte[] data，String password，tag。分别代表图像数据、密码、请求类型。
</p>
<h1>
	截图
</h1>
<div>
	<img src="https://raw.githubusercontent.com/pwrliang/CameraViewer/master/screenshot/Screenshot_2016-06-24-11-41-28_com.yjm.cameraviewer.png" width="200" height="320" alt="" /><img src="https://raw.githubusercontent.com/pwrliang/CameraViewer/master/screenshot/Screenshot_2016-06-24-11-41-35_com.yjm.cameraviewer.png" width="200" height="320" alt="" /><img src="https://raw.githubusercontent.com/pwrliang/CameraViewer/master/screenshot/Screenshot_2016-06-24-11-43-03_com.yjm.cameraviewer.png" width="200" height="320" alt="" /><br />
	
</div>
<div>
	<img src="https://raw.githubusercontent.com/pwrliang/CameraViewer/master/screenshot/Screenshot_2016-06-24-11-43-21_com.yjm.cameraviewer.png" width="200" height="320" alt="" /><img src="https://raw.githubusercontent.com/pwrliang/CameraViewer/master/screenshot/Screenshot_2016-06-24-11-43-40_com.yjm.cameraviewer.png" width="200" height="320" alt="" /><img src="https://raw.githubusercontent.com/pwrliang/CameraViewer/master/screenshot/Screenshot_2016-06-24-11-44-40_com.yjm.camera.png" width="200" height="320" alt="" /><br />
	
</div>
