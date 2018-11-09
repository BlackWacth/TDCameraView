package com.bruce.lib.utils;

import android.util.Log;


/**
 * 日志工具
 */
public class L {

    private static boolean isDebug = true;

    public static void v(String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.v(getTag(element), msg + getFileLine(element));
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.v(getCustomTag(element, tag), msg + getFileLine(element));
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.v(getCustomTag(element, tag), msg + getFileLine(element), tr);
        }
    }

    public static void vv(String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.v(getTag(element), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void vvv(String tag, String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.v(getCustomTag(element, tag), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.d(getTag(element), msg + getFileLine(element));
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.d(getCustomTag(element, tag), msg + getFileLine(element));
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.d(getCustomTag(element, tag), msg + getFileLine(element), tr);
        }
    }

    public static void dd(String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.d(getTag(element), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void ddd(String tag, String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.d(getCustomTag(element, tag), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void i(String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.i(getTag(element), msg + getFileLine(element));
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.i(getCustomTag(element, tag), msg + getFileLine(element));
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.i(getCustomTag(element, tag), msg + getFileLine(element), tr);
        }
    }

    public static void ii(String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.i(getTag(element), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void iii(String tag, String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.i(getCustomTag(element, tag), String.format(format, obj) + getFileLine(element));
        }
    }


    public static void w(String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.w(getTag(element), msg + getFileLine(element));
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.w(getCustomTag(element, tag), msg + getFileLine(element));
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.w(getCustomTag(element, tag), msg + getFileLine(element), tr);
        }
    }

    public static void ww(String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.w(getTag(element), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void www(String tag, String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.w(getCustomTag(element, tag), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.e(getTag(element), msg + getFileLine(element));
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.e(getCustomTag(element, tag), msg + getFileLine(element));
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.e(getCustomTag(element, tag), msg + getFileLine(element), tr);
        }
    }

    public static void ee(String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.e(getTag(element), String.format(format, obj) + getFileLine(element));
        }
    }

    public static void eee(String tag, String format, Object... obj) {
        if (isDebug) {
            StackTraceElement element = getStackTraceElement();
            Log.e(getCustomTag(element, tag), String.format(format, obj) + getFileLine(element));
        }
    }

    /**
     * 获取实际调用行的堆栈信息
     * 这里过滤了一下，非L.java堆栈信息的下一个即为代码中调用处。
     * @return 实际调用行的堆栈信息
     */
    private static StackTraceElement getStackTraceElement() {
        StackTraceElement targetStackTrace = null;
        boolean shouldTrace = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            boolean isLogMethod = stackTraceElement.getClassName().equals(L.class.getName());
            if (shouldTrace && !isLogMethod) {
                targetStackTrace = stackTraceElement;
                break;
            }
            shouldTrace = isLogMethod;
        }
        return targetStackTrace;
    }

    /**
     * 获取行号
     * @param element StackTraceElement
     * @return 行号
     */
    private static String getFileLine(StackTraceElement element) {
        String result = element.toString();
        return " " + result.substring(result.indexOf("("));
    }

    /**
     * 默认TAG, className::methodName
     * @param element StackTraceElement
     * @return className::methodName
     */
    private static String getTag(StackTraceElement element) {
        String className = element.getClassName();
        String clzName = className.substring(className.lastIndexOf(".") + 1);
        String methodName = element.getMethodName();
        return "(" + clzName + "::" + methodName + ")";
    }

    /**
     * 自定义TAG, 这里是追加
     * @param element StackTraceElement
     * @param tag 追加tag
     * @return className::methodName => tag
     */
    private static String getCustomTag(StackTraceElement element, String tag) {
        String className = element.getClassName();
        String clzName = className.substring(className.lastIndexOf(".") + 1);
        String methodName = element.getMethodName();
        return "(" + clzName + "::" + methodName + " => " + tag + ")";
    }
}
