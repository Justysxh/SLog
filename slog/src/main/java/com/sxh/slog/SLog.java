package com.sxh.slog;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sxh on 2016/10/8.
 */
public class SLog
{
    private static final int MAX_LOG_BUFFER = 4 * 1024;//4k  //日志内存缓冲区长度
    private static  int MAX_LOG_FILE_SIZE = 4*1024*1024;//1M  日志文件大小
    private static final int MSG_WHAT = 92;  //消息id
    private static String mDefaultTag = "SLog"; //默认全局tag
    private static int mEnableLogLevel = Log.VERBOSE; //默认日志等级
    private static StringBuffer mLogBuffer = new StringBuffer(); //日缓冲区
    private static boolean mEnableSaveToFile = true; //是否允许保存文件
    private static String mLogFilePath="/SLog"; //文件路径  注意需要/开头
    private static String mLogFileName="slog.txt"; //日志文件名
    private static Handler mHandler;
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS"); //日期格式化
    private static Date mDate=new Date();

    /**
     * 初始化,如果未初始化, 只能输出到日志系统, 而不能保存文件
     * @param context
     */
    public static void init(Context context)
    {
        synchronized (SLog.class)
        {
            if(mHandler==null)
            {
                mHandler = new Handler(Looper.getMainLooper())//延时写入文件(即一条日志最多隔一段时间就必须要写入日志文件)
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        if(msg.what==MSG_WHAT)
                        {
                            writeBufferToFile();
                        }
                    }
                };
            }
        }
    }


    /**
     * 设置全局的tag
     * @param tag
     */
    public static void setDefaultTag(String tag)
    {
        mDefaultTag = tag;
    }

    /**
     * 设置日志等级
     * @param logLevel
     */
    public static void setEnableLogLevel(int logLevel)
    {
        mEnableLogLevel = logLevel;
    }

    /**
     * 启用或者禁用日志
     * @param bEnable
     */
    public static void setEnable(boolean bEnable)
    {
        setEnableLogLevel(bEnable ? Log.VERBOSE : Log.ASSERT + 1);
    }

    /**
     * 启用或者禁用文件保存
     * @param bEnable
     */
    public static void setEnableSaveToFile(boolean bEnable)
    {
        mEnableSaveToFile = bEnable;
    }

    /**
     * 设置日志文件路径和文件名
     * @param path 文件路径
     * @param name 文件名
     */
    public static void setLogFile(String path, String name)
    {
        mLogFilePath = path;
        mLogFileName = name;
    }


    /**
     * 设置日志文件大小
     * @param size 最小为1K
     */
    public static void setMaxLogFileSize(int size)
    {
        if(size<1024)
        {
            size = 1024;
        }
        MAX_LOG_FILE_SIZE = size;
    }


    public static void v(String msg)
    {
        v(mDefaultTag,msg);
    }
    public static void v(String tag,String msg)
    {
        log(Log.VERBOSE,tag,msg);
    }
    public static void v(String msgFormat, Object... args)
    {
        v(mDefaultTag,msgFormat,args);
    }
    public static void v(String tag,String msgFormat, Object... args)
    {
        log(Log.VERBOSE,tag,msgFormat,args);
    }

    public static void d(String msg)
    {
        d(mDefaultTag,msg);
    }
    public static void d(String tag,String msg)
    {
        log(Log.DEBUG,tag,msg);
    }
    public static void d(String msgFormat, Object... args)
    {
        d(mDefaultTag,msgFormat,args);
    }
    public static void d(String tag,String msgFormat, Object... args)
    {
        log(Log.DEBUG,tag,msgFormat,args);
    }


    public static void i(String msg)
    {
        i(mDefaultTag,msg);
    }
    public static void i(String tag,String msg)
    {
        log(Log.INFO,tag,msg);
    }
    public static void i(String msgFormat, Object... args)
    {
        i(mDefaultTag,msgFormat,args);
    }
    public static void i(String tag,String msgFormat, Object... args)
    {
        log(Log.INFO,tag,msgFormat,args);
    }

    public static void w(String msg)
    {
        w(mDefaultTag,msg);
    }
    public static void w(String tag,String msg)
    {
        log(Log.WARN,tag,msg);
    }
    public static void w(String msgFormat, Object... args)
    {
        w(mDefaultTag,msgFormat,args);
    }
    public static void w(String tag,String msgFormat, Object... args)
    {
        log(Log.WARN,tag,msgFormat,args);
    }


    public static void e(String msg)
    {
        e(mDefaultTag,msg);
    }
    public static void e(String tag,String msg)
    {
        log(Log.ERROR,tag,msg);
    }
    public static void e(String msgFormat, Object... args)
    {
        e(mDefaultTag,msgFormat,args);
    }
    public static void e(String tag,String msgFormat, Object... args)
    {
        log(Log.ERROR,tag,msgFormat,args);
    }

    public static void wtf(String msg)
    {
        e(mDefaultTag,msg);
    }
    public static void wtf(String tag,String msg)
    {
        log(Log.ASSERT,tag,msg);
    }
    public static void wtf(String msgFormat, Object... args)
    {
        wtf(mDefaultTag,msgFormat,args);
    }
    public static void wtf(String tag,String msgFormat, Object... args)
    {
        log(Log.ASSERT,tag,msgFormat,args);
    }

    public static void e(Throwable t)
    {
        e(mDefaultTag,getStackTraceString(t) );
        //有异常发生时, 强制马上写入
        writeBufferToFile();
    }



    private static void log(int logLevel, String tag, String msgFormat, Object... args)
    {
        String msg = String.format(msgFormat, args);
        log(logLevel, tag, msg);
    }

    private static void log(int logLevel, String tag, String msg)
    {
        if (logLevel < mEnableLogLevel)//禁用某些logLevel
        {
            return;
        }
        if (logLevel < Log.VERBOSE || logLevel > Log.ASSERT)//无效的LogLevel
        {
            return;
        }
        Log.println(logLevel, tag, msg);
        logToFile(logLevel, tag, msg);
    }

    private static void logToFile(int logLevel, String tag, String msg)
    {
        if (!mEnableSaveToFile)
        {
            return;
        }
        mDate.setTime(System.currentTimeMillis());
        String str = String.format("%s [%s] [%s] %s\n",  sDateFormat.format(mDate),logLevelToString(logLevel),tag,msg);

        int newLen = str.length();
        boolean bWriteFile=false;
        synchronized (mLogBuffer)
        {
            newLen += mLogBuffer.length();
            mLogBuffer.append(str);
            if(newLen>=MAX_LOG_BUFFER)
            {
                bWriteFile = true;
            }
        }
        if(mHandler!=null)
        {
            if( mHandler.hasMessages(MSG_WHAT))
            {
                mHandler.removeMessages(MSG_WHAT);
            }
            if(bWriteFile==false)
            {
                Message message = mHandler.obtainMessage();
                message.what = MSG_WHAT;
                mHandler.sendMessageDelayed(message,5000);
            }
        }
        if(bWriteFile)
        {
            writeBufferToFile();
        }

    }

    private static void writeBufferToFile()
    {
        String tStr="";
        synchronized (mLogBuffer)
        {
            int len = mLogBuffer.length();
            if(len>0)
            {
                tStr = mLogBuffer.toString();
                mLogBuffer.delete(0,len-1);
            }
        }
        if(tStr.length()>0)
        {
            if (Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState()))
            {
                File floder = new File(Environment.getExternalStorageDirectory()+mLogFilePath);
                FileOutputStream fileOutputStream = null;
                OutputStreamWriter outputStreamWriter = null;

                if (!floder.exists())
                {
                    floder.mkdirs();
                }
                File file = new File(floder, mLogFileName);
                if (!file.exists())
                {
                    try
                    {
                        file.createNewFile();
                    } catch (IOException e)
                    {
                        Log.e("MobileUtil", "创建文件失败");
                    }
                } else
                {
                    if (file.length() >=MAX_LOG_FILE_SIZE)
                    {
                        Log.i("sxh","log file full");
                        File old = new File(file.getParent()+"/old.txt");
                        if(old.exists())
                        {
                            old.delete();
                        }
                        file.renameTo(old);
                        file.delete();
                        try
                        {
                            file.createNewFile();
                        } catch (IOException e)
                        {
                            Log.e("MobileUtil", "创建文件失败");
                        }
                    }
                }
                try
                {
                    fileOutputStream = new FileOutputStream(file, true);
                    outputStreamWriter = new OutputStreamWriter(fileOutputStream,"UTF-8");
                    outputStreamWriter.write(tStr);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    outputStreamWriter=null;
                    fileOutputStream.close();
                    fileOutputStream=null;
                } catch (Exception e)
                {
                    Log.e("MobileUtil", "文件写入出错");
                } finally
                {
                    try
                    {
                        if (outputStreamWriter != null)
                        {
                            outputStreamWriter.close();
                        }
                        if (fileOutputStream != null)
                        {
                            fileOutputStream.close();
                        }
                    } catch (IOException e)
                    {
                        Log.e("MobileUtil", "关闭输出流出错");
                    }
                }
            }
        }
    }

    private static String logLevelToString(int logLevel)
    {
        String tStr = "unknow";
        switch(logLevel)
        {
            case Log.VERBOSE:
                tStr="VERBOSE";
                break;
            case Log.DEBUG:
                tStr = "DEBUG";
                break;
            case Log.INFO:
                tStr = "INFO";
                break;
            case Log.WARN:
                tStr = "WARN";
                break;
            case Log.ERROR:
                tStr = "ERROR";
                break;
            case Log.ASSERT:
                tStr = "ASSERT";
                break;
            default:
                break;
        }
        return tStr;
    }

    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        pw.close();
        pw.close();
        return sw.toString();
    }

}
