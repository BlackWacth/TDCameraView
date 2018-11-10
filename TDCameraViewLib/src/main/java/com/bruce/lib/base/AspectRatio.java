package com.bruce.lib.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

/**
 * 用于描述宽度和高度之间的比例关系的不可变类。
 */
public class AspectRatio implements Comparable<AspectRatio>, Parcelable {

    private final static SparseArrayCompat<SparseArrayCompat<AspectRatio>> sCache = new SparseArrayCompat<>(16);

    private final int mX;
    private final int mY;

    public AspectRatio(int x, int y) {
        mX = x;
        mY = y;
    }

    public static final Creator<AspectRatio> CREATOR = new Creator<AspectRatio>() {
        @Override
        public AspectRatio createFromParcel(Parcel in) {
            int x = in.readInt();
            int y = in.readInt();
            return new AspectRatio(x, y);
        }

        @Override
        public AspectRatio[] newArray(int size) {
            return new AspectRatio[size];
        }
    };

    /**
     * 1、x, y分别除以(x, y)的最大公约数。
     * 2、创建x, y最简比例的AspectRatio对象
     * 3、创建SparseArrayCompat集合，以y为键，存储AspectRadio对象
     * 4、以x为键，把以y为键的集合存入,sCache集合中
     * @param x width
     * @param y height
     * @return x, y比例的AspectRatio的实例
     */
    public static AspectRatio of(int x, int y) {
        int gcd = gcd(x, y);
        x /= gcd;
        y /= gcd;

        SparseArrayCompat<AspectRatio> arrayX = sCache.get(x);
        if (arrayX == null) {
            AspectRatio ratio = new AspectRatio(x, y);
            arrayX = new SparseArrayCompat<>();
            arrayX.put(y, ratio);
            sCache.put(x, arrayX);
            return ratio;
        } else {
            AspectRatio ratio = arrayX.get(y);
            if (ratio == null) {
                ratio = new AspectRatio(x, y);
                arrayX.put(y, ratio);
            }
            return ratio;
        }
    }

    /**
     * 求最大公约数（辗转相除法）
     * 如果 a < b, 第一次循环会交换a和b
     * 所有参数 a > b，会少一次循环
     * @param a a
     * @param b b
     * @return a和b的最大公约数
     */
    private static int gcd(int a, int b) {
        int c;
        while (b != 0) {
            c = b;
            b = a % b;
            a = c;
        }
        return a;
    }

    /**
     * 比例是否相等
     * @param size size
     * @return size的宽高比与该实例宽高比相同，返回true
     */
    public boolean matches(Size size) {
        int gcd = gcd(size.getWidth(), size.getHeight());
        int x = size.getWidth() / gcd;
        int y = size.getHeight() / gcd;
        return mX == x && mY == y;
    }

    /**
     * 把宽高比例字符串("16:9", "4:3")转换为AspectRatio对象
     * @param s 宽高比例字符串,格式如"16:9", "4:3"
     * @return AspectRatio对象
     */
    public static AspectRatio parse(String s) {
        int position = s.indexOf(':');
        if (position == -1) {
            throw new IllegalArgumentException("格式错误的宽高比: " + s);
        }
        try {
            int x = Integer.parseInt(s.substring(0, position));
            int y = Integer.parseInt(s.substring(position + 1));
            return AspectRatio.of(x, y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("格式错误的宽高比: " + s, e);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mX);
        dest.writeInt(mY);
    }

    @Override
    public int compareTo(@NonNull AspectRatio o) {
        if (equals(o)) {
            return 0;
        } else if (toFloat() - o.toFloat() > 0) {
            return 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof AspectRatio) {
            AspectRatio ratio = (AspectRatio) obj;
            return mX == ratio.mX && mY == ratio.mY;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mY ^ ((mX << (Integer.SIZE / 2)) | (mX >>> (Integer.SIZE / 2)));
    }

    @Override
    public String toString() {
        return mX + " : " + mY + " => mX / mY = " + toFloat();
    }

    private float toFloat() {
        return (float) mX / mY;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }
}
