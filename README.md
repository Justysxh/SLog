# SLog
SLog 只是对android Log库作了简单封装, 增加日志文件保存功能, 格式化功能, 日志等级限制功能, 全局tag

用例: SLog.init(getApplicationContext()); //初始化, 全局只需要初始化一次, 如果没有初始化, 不能保存日志文件

SLog.setDefaultTag("MyTag"); //设置全局tag

SLog.setEnableLogLevel(Log.DEBUG); //日志等级从Log.DEBUG到Log.ASSERT 如果当前等级小于此等级,则不会被打印, 即Log.DEBUG是全部打印, //>Log.ASSERT 是禁用打印

//SLog.setEnable(false); //可使用此函数全局开启或者关闭日志

SLog.setEnableSaveToFile(false);//可使用此函数全局开启或者关闭日志写入文件的功能

SLog.setLogFile("/sxh","sxhlog.txt"); //设置日志文件保存路径和文件名

SLog.setMaxLogFileSize(1024*1024); //设置日志文件大小限制,大于则清空

SLog.i("test"); //使用全局tag打印日志

SLog.i("tag2","this is tag2 msg"); //使用指定tag打印日志

SLog.i("the current time mills: %d", System.currentTimeMillis()); //支持格式化的日志, 其它等级的日志类似

日志文件功能: 因为写SD卡一般比较耗时,所以内部使用了缓存机制, 只有当缓存满时才会写入SD卡一次, 避免频繁写入SD卡 当然,为了尽量不丢失日志,内部使用延迟写入机制,即日志缓存会在5秒内自动写入SD卡一次, 既使缓存没有满.
