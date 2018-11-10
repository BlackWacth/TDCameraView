package com.bruce.lib.base;

import android.util.ArrayMap;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 管理Size对象的Map集合类
 */
public class SizeMap {

    private final ArrayMap<AspectRatio, SortedSet<Size>> mRatios = new ArrayMap<>();

    /**
     * 把Size对象，存入以宽高比例为键，以SortedSet为值的ArrayMap中
     * 相同的宽高比，在SortedSet以w*h乘积（面积）为比较值，进行排序的Set集合中
     * @param size size
     * @return 添加成功为true, 已经存在size,不用添加，则为false。
     */
    public boolean add(Size size) {
        for (AspectRatio ratio : mRatios.keySet()) {
            if (ratio.matches(size)) {
                final SortedSet<Size> sizes = mRatios.get(ratio);
                if (sizes.contains(size)) {
                    return false;
                } else {
                    sizes.add(size);
                    return true;
                }
            }
        }
        SortedSet<Size> sizes = new TreeSet<>();
        sizes.add(size);
        mRatios.put(AspectRatio.of(size.getWidth(), size.getHeight()), sizes);
        return true;
    }

    public void remove(AspectRatio ratio) {
        mRatios.remove(ratio);
    }

    /**
     * Key是按升序排列
     * @return Set<AspectRatio>
     */
    public Set<AspectRatio> ratios() {
        return mRatios.keySet();
    }

    public SortedSet<Size> sizes(AspectRatio ratio) {
        return mRatios.get(ratio);
    }

    public void clear() {
        mRatios.clear();
    }

    public boolean isEmpty() {
        return mRatios.isEmpty();
    }
}
