MozzAndroidUtils
===================
作者：hsllany@163.com, everyknoxkyo@gmail.com
HttpUtils及DownloadHttpUtils 用法
-------------------
普通网络请求，可用HttpUtils即可。若涉及到文件下载，应用DownloadHttpUtils.

HttpUtils和DownloadHttpUtils均为异步机制，每个任务执行时是属于不同线程，所以不应在HttpListener或HttpDownloadListener中操纵UI，应运用Handler。

###get 方法###
```java
HttpUtils httpUtils = new HttpUtils();
httpUtils.get("http://www.baidu.com", new HttpListener() {

		@Override
		public void onSuccess(HttpResponse response) {
		if (response.status == HTTP_OK)
			System.out.println(response.html);
	}

		@Override
		public void onFail(HttpResponse response) {
			// TODO Auto-generated method stub
		}
	});
```

###post 方法###
```java
//装入Post参数
Map<String, String> postData = new HashMap<String, String>();
postData.put("email", "test@t.com");

httpUtils.post("http://www.baidu.com", new HttpListener() {

		@Override
		public void onSuccess(HttpResponse response) {
		if (response.status == HTTP_OK)
			System.out.println(response.html);
	}

		@Override
		public void onFail(HttpResponse response) {
			// TODO Auto-generated method stub
		}
	}, postData);
```

###文件下载###
```java
DownloaderHttpUtils downloader = new DownloaderHttpUtils();
downloader.download("http://www.test.com/a.file", new HttpDownloadListener() {
			
	@Override
	public void onDownloadSuccess() {
		//下载成功
				
	}
			
	@Override
	public void onStartDownload(int fileSize) {
		//开始下载，fileSize为要下载文件的大小
				
	}
			
	@Override
	public void onDownloadFailed() {
		//下载失败
				
	}
			
	@Override
	public void onDownloading(int downloadSize) {
		//正则下载，downloadSize为已经下载的大小
				
	}
}, SDCard.sdCardDir() + "/saveDir");
```

DB用法
--------------------
**由Mozz框架运行的表中，必须含有字段"id",表示主键。**

首先，继承Eloquent（代表数据库中的表）, 类名的规则是：表名 + Eloquent。注意命名应和数据库中表明对应。

```java
class StudentsEloquent extends Eloquent<Student>{

}
```
之后，运用如下：

###查询所有：###
```java
StudentsEloquent studentTable = new StudentsEloquent();
Cursor cursor = students.all();
```

###带Where的查找###
```java
//获取所有数据
List<Model> result = studentTable
						.where(new String[]{'name'},new String[]{'zhangdao'})
						.get();

//获取单条数据
Model model = studentTable.where("grade = 3").first();
```

###查找id,并更新###
```java
Model student = studentTable.find(1);
student.set("name","zhangdao");
studentTable.save(student);
```

###插入新数据###
```java
Model student = new Student();
student.set("name","zhangdao");
studentTable.save(student);
```

###删除数据###
```java
studentTable.delete(student);
```

###创建表###
```java
Eloquent.create("student", new String[] { "name", "age" },
				new COLUMN_TYPE[] { COLUMN_TYPE.TYPE_TEXT,
						COLUMN_TYPE.TYPE_INTEGER }, this);
```

###从Model中获取值###
```java
Model student = studentTable.find(7);
System.out.println(student.get("id"));
System.out.println(student.get("name"));
```

客户端升级Upgrader
--------------------
使用Upgrader，可灵活对客户端进行升级。

服务器应对应配置升级用json，示例如下：
```json
{"versionCode":3, "versionName":"1.1.1", "des":"2015年的新版本", "downloadurl":"http://test.com/test.apk"}
```
这里code为版本对应编号，应大于AndroidManifest。xml中的versionCode，否则不会触发onCheckNewVersion中hasNew为true的情况，具体见下：
```java
		//定义upgrader,传入升级网址
		final Upgrader upgrader = new Upgrader(
				"http://test.com/upgrade.json", this);
				
		//定义回调接口，处理升级
		upgrader.setOnUpgradeListener(new UpgradeListener() {
			@Override
			public void onNewVersion(int serverVersionCode,
					String serverVersion, String serverVersionDescription) {

				try {
					upgrader.download(null,
							MozzConfig.getAppAbsoluteDir(MainActivity.this),
							"newVersion.apk");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onNoNewVersion() {
				Log.d("Upgrader", "onNoNewVersion");
			}

			@Override
			public void onCheckFailed() {
				Log.d("Upgrader", "onCheckFailed:");
			}
			
		});

		//检查新版本
		upgrader.checkNewVersion();
```
检测完新版本后，如果确认有新版本，可调用upgrader.download()直接发起下载。**但若无新版本，调用download()，会触发IllegalAccessException**
```java
try {
			//确保在检测到新版本后发起
			upgrader.download(new HttpDownloadListener() {

				@Override
				public void onDownloading(int downloadSize) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onDownloadSuccess() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onDownloadStart(int fileSize) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onDownloadFailed() {
					// TODO Auto-generated method stub

				}
			}, "/AppDir", "newVersion.apk");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
```