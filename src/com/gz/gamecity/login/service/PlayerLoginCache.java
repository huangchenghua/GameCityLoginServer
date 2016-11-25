package com.gz.gamecity.login.service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.gz.gamecity.bean.Player;



/**
 *Cache.java
 *
 * Created on 2014-1-11 上午11:30:36 by sunzhenchao mychaoyue2011@163.com
 */
public class PlayerLoginCache<K, V> {

    public ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>();
    public DelayQueue<DelayedItem<K>> queue = new DelayQueue<DelayedItem<K>>();
    
    private static PlayerLoginCache<String, Player> instance;
    
    
    
    public static synchronized PlayerLoginCache<String, Player> getInstance() {
    	if(instance == null)
    		instance=new PlayerLoginCache<String, Player>();
		return instance;
	}

	public void put(K k,V v,long liveTime){
        V v2 = map.put(k, v);
        DelayedItem<K> tmpItem = new DelayedItem<K>(k, liveTime);
        if (v2 != null) {
            queue.remove(tmpItem);
        }
        queue.put(tmpItem);
    }
    
	public V getV(K key){
		return map.get(key);
	}
	
    private PlayerLoginCache(){
        Thread t = new Thread(){
            @Override
            public void run(){
                dameonCheckOverdueKey();
            }
        };
//        t.setDaemon(true);
        t.start();
    }
    
    public void dameonCheckOverdueKey(){
        while (true) {
            DelayedItem<K> delayedItem = queue.poll();
            if (delayedItem != null) {
                map.remove(delayedItem.getT());
                System.out.println(System.nanoTime()+" remove "+delayedItem.getT() +" from cache");
            }
        }
    }
    
    
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        int cacheNumber = 10;
        int liveTime = 0;
        PlayerLoginCache<String, Integer> cache = new PlayerLoginCache<String, Integer>();
        
        for (int i = 0; i < cacheNumber; i++) {
            liveTime = random.nextInt(30000);
            System.out.println(i+"  "+liveTime);
            cache.put(i+"", i, random.nextInt(liveTime));
            if (random.nextInt(cacheNumber) > 7) {
                liveTime = random.nextInt(3000);
                System.out.println(i+"  "+liveTime);
                cache.put(i+"", i, random.nextInt(liveTime));
            }
        }

        Thread.sleep(3000);
        System.out.println();
    }

}

class DelayedItem<T> implements Delayed{

    private T t;
    private long liveTime ;
    private long removeTime;
    
    public DelayedItem(T t,long liveTime){
        this.setT(t);
        this.liveTime = liveTime;
        this.removeTime = TimeUnit.NANOSECONDS.convert(liveTime, TimeUnit.NANOSECONDS) + System.nanoTime();
    }
    
    @Override
    public int compareTo(Delayed o) {
        if (o == null) return 1;
        if (o == this) return  0;
        if (o instanceof DelayedItem){
            DelayedItem<T> tmpDelayedItem = (DelayedItem<T>)o;
            if (liveTime > tmpDelayedItem.liveTime ) {
                return 1;
            }else if (liveTime == tmpDelayedItem.liveTime) {
                return 0;
            }else {
                return -1;
            }
        }
        long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return diff > 0 ? 1:diff == 0? 0:-1;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(removeTime - System.nanoTime(), unit);
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
    @Override
    public int hashCode(){
        return t.hashCode();
    }
    
    @Override
    public boolean equals(Object object){
        if (object instanceof DelayedItem) {
            return object.hashCode() == hashCode() ?true:false;
        }
        return false;
    }
    
}
