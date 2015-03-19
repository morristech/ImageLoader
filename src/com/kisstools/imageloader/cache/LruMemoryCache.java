package com.kisstools.imageloader.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import me.dawson.kisstools.utils.BitmapUtil;
import me.dawson.kisstools.utils.StringUtil;
import me.dawson.kisstools.utils.SystemUtil;
import android.graphics.Bitmap;

public class LruMemoryCache extends BaseCache<Bitmap> {
	public static final String TAG = "LruImageCache";
	private LinkedHashMap<String, Bitmap> cacheMap;
	private long totalSize;

	public LruMemoryCache() {
		maxSize = SystemUtil.getMaxMemory() / 4;
		cacheMap = new LinkedHashMap<String, Bitmap>(0, 0.75f, true);
		totalSize = 0;
	}

	@Override
	public void set(String key, Bitmap value) {
		if (StringUtil.isEmpty(key) || value == null) {
			return;
		}

		// remove old cache content
		remove(key);

		synchronized (this) {
			cacheMap.put(key, value);
			totalSize += BitmapUtil.getImageBytes(value);
		}

		// trim total size to max limit
		trimToSize(maxSize);
	}

	@Override
	public Bitmap get(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}

		if (!cacheMap.containsKey(key)) {
			return null;
		}

		return cacheMap.get(key);
	}

	@Override
	public Bitmap remove(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}

		if (!cacheMap.containsKey(key)) {
			return null;
		}
		synchronized (this) {
			Bitmap bitmap = cacheMap.remove(key);
			totalSize -= BitmapUtil.getImageBytes(bitmap);
			return bitmap;
		}
	}

	@Override
	public boolean contains(String key) {
		return cacheMap.containsKey(key);
	}

	@Override
	public long getSize() {
		return totalSize;
	}

	@Override
	public int getCount() {
		int count = 0;
		synchronized (this) {
			count = cacheMap.size();
		}
		return count;
	}

	@Override
	public void clear() {
		trimToSize(0);
	}

	protected void trimToSize(long maxSize) {
		while (true) {
			synchronized (this) {
				if (totalSize <= maxSize || cacheMap.isEmpty()) {
					break;
				}

				Map.Entry<String, Bitmap> toEvict = cacheMap.entrySet()
						.iterator().next();
				String key = toEvict.getKey();
				Bitmap bitmap = toEvict.getValue();
				cacheMap.remove(key);
				totalSize -= BitmapUtil.getImageBytes(bitmap);
			}
		}
	}
}
