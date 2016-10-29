package cn.sunline.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.sunline.redis.util.JedisUtil;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.BinaryClient.LIST_POSITION;

public class JedisUtilTest {
	private Jedis jedis;

	@Before
	public void init() {
		jedis = JedisUtil.getJedis();
		System.out.println("Begin刷新数据库缓存:" + jedis.flushDB());
	}

	@After
	public void destory() {
		System.out.println("End刷新数据库缓存:" + jedis.flushDB());
		JedisUtil.realeseResources(jedis);
	}

	@Test
	public void testGetJedis() {
		System.out.println(jedis);
	}

	/**
	 * 对key的基本操作
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void operateKey() throws InterruptedException {
		// 2.添加key
		jedis.set("key1", "value1");
		jedis.set("key2", "value2");
		jedis.set("key3", "value3");
		jedis.set("key4", "value4");
		jedis.set("key5", "value5");
		// 3.第一次迭代输出key
		System.out.println("第一次迭代输出key");
		Iterator<String> iterator = jedis.keys("ke*").iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			System.out.println(key + "==>" + jedis.get(key));
		}
		// 4.判断某个key是否存在
		System.out.println("key1是否存在?" + jedis.exists("key1"));
		// 5.设置key的生命周期位10s
		System.out.println("设置key1的生命周期位10s==>" + jedis.expire("key1", 10));
		// 6.查看key的当前生命周期时间
		while (jedis.exists("key1")) {
			Thread.sleep(1000);
			System.out.println("key1生命周期==>" + jedis.ttl("key1"));
		}
		// 7.第二次迭代输出key
		System.out.println("第二次迭代输出key");
		Iterator<String> iterator2 = jedis.keys("ke*").iterator();
		while (iterator2.hasNext()) {
			String key = iterator2.next();
			System.out.println(key + "==>" + jedis.get(key));
		}
		// 8.删除key
		System.out.println("删除key2==>" + jedis.del("key2"));
		// 9.修改key
		System.out.println("修改key3==>" + jedis.set("key3", "updated value3"));
		// 10.第三次迭代输出key
		System.out.println("第三次迭代输出key");
		Iterator<String> iterator3 = jedis.keys("ke*").iterator();
		while (iterator3.hasNext()) {
			String key = iterator3.next();
			System.out.println(key + "==>" + jedis.get(key));
		}
	}

	/**
	 * 对字符串String的操作
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void JedisOperateString() throws InterruptedException {
		System.out.println("**********追加内容**********************");
		jedis.mset("key", "value", "key1", "value1", "key2", "value2");
		System.out.println("追加前:" + jedis.get("key"));
		System.out.println("追加后的长度==>" + jedis.append("key", " append"));
		System.out.println("追加前:" + jedis.get("key"));
		System.out.println("**********获取字符串中[start, stop]范围的值**********************");
		System.out.println(jedis.getrange("key", 0, -1));

		System.out.println("**********一次获取多个值**********************");
		List<String> mget = jedis.mget("key", "key1", "key2");
		for (String value : mget) {
			System.out.println("value===>" + value);
		}
		System.out.println("**********获取并返回旧值，在设置新值**********************");
		System.out.println("old key value is ? " + jedis.getSet("key", "new value"));
		System.out.println("new key value is ? " + jedis.get("key"));

		System.out.println("**********跳2自增**********************");
		jedis.set("number", "0");
		while (!jedis.get("number").equals("10")) {
			System.out.println("number===>"+jedis.incrBy("number", 2));
			Thread.sleep(1000);
		}
		
		System.out.println("**********设置key对应的值value，并设置有效期为time秒**********************");
		System.out.println("setex operate result ?" + jedis.setex("key", 10, "set time and value"));
		System.out.println("new key value is ?" + jedis.get("key"));
		while (jedis.exists("key")) {
			System.out.println("key life remain ===>" + jedis.ttl("key"));
			Thread.sleep(1000);
		}
		System.out.println("fianlly key value is ?" + jedis.get("key"));
	}

	@Test
	public void JedisOperateList() {
		
		System.out.println("**********链表头部插入**********************");
			jedis.lpush("list", "value0");
			jedis.lpush("list", "value1","value2","value3","value4","value5");
			List<String> lrange = jedis.lrange("list", 0, -1);
			System.out.println(lrange);
			System.out.println("**********链表尾部部插入**********************");
			jedis.rpush("list", "value6","value0","value0","value0","value10");
			lrange = jedis.lrange("list", 0, -1);
			System.out.println(lrange);
			System.out.println("**********头部删除，尾部删除**********************");
			jedis.lpop("list");
			jedis.rpop("list");
			lrange = jedis.lrange("list", 0, -1);
			System.out.println(lrange);
			System.out.println("**********指定范围(-2:表示反向删除两个)，方向删除**********************");
			System.out.println("范围删除是否成功? "+jedis.lrem("list", -2, "value0"));
			lrange = jedis.lrange("list", 0, -1);
			System.out.println(lrange);
			
			System.out.println("**********剪切key对应的字符串，切[start, stop]一段并把改制重新赋给key**********************");
			jedis.ltrim("list", 0, 3);
			lrange = jedis.lrange("list", 0, -1);
			System.out.println(lrange);
			
			System.out.println("**********返回index索引上的值**********************");
			String lindex = jedis.lindex("list", 2);
			System.out.println("索引2的value is ?"+lindex);
			
	}
	/**
	 * Jedis 操作hash(有序的map)
	 */
	@Test
	public void JedisOperateHash(){
		System.out.println("************设置值HashMap********************");
		jedis.hset("hashset", "key1", "value1");
		System.out.println("设置不存在的值?"+jedis.hsetnx("hashset", "key0", "value0"));
		System.out.println("设置存在的值?"+jedis.hsetnx("hashset", "key0", "value0"));
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("key2", "value2");
		hashMap.put("key3", "value3");
		hashMap.put("key4", "value4");
		hashMap.put("key5", "value5");
		System.out.println("批量设置值?"+jedis.hmset("hashset", hashMap ));
	
		
		System.out.println("************遍历HashMap1********************");
		List<String> hmget = jedis.hmget("hashset", "key0","key1","key2","key3","key4","key5");
		for(String mapValue:hmget){
			System.out.println("hashset value===>"+mapValue);
		}
		
		System.out.println("测试指定的field key0是否存在?"+jedis.hexists("hashset", "key0"));
		System.out.println("获取指定的hash field(key0)===>"+jedis.hget("hashset", "key0"));
		System.out.println("返回hash的field 数量===>"+jedis.hlen("hashset"));
		System.out.println("删除指定的field(key0、key1)==>"+jedis.hdel("hashset", "key0","key1"));
		System.out.println("测试指定的field(key0) 是否存在?"+jedis.hexists("hashset", "key0"));
		System.out.println("返回hash的field数量===>"+jedis.hlen("hashset"));
		
		
		System.out.println("************遍历HashMap2********************");
		Map<String, String> hgetAll = jedis.hgetAll("hashset");
		Set<Entry<String, String>> entrySet = hgetAll.entrySet();
		for (Entry<String, String> entry : entrySet) {
			System.out.println(entry.getKey()+"===>"+entry.getValue());
		}
		
		System.out.println("hashset type is ?"+ jedis.type("hashset"));
	}
	
	/**
	 * Jedis 对Set操作（无序，确定性，唯一性）
	 * 
	 */
	@Test
	public void jedisOperateSet(){
		System.out.println("1.集合里面添加元素==>"+jedis.sadd("set","set0" ,"set1","set2","set3","set4","set5","set6","set7"));
		System.out.println("2.获取集合所有的元素");
		Set<String> smembers = jedis.smembers("set"); 
		for (String smember : smembers) {
			System.out.println("set==="+smember);
		}
		System.out.println("返回集合元素的个数?"+jedis.scard("set"));
		System.out.println("3.判断集合是否有(set0)某个值?"+jedis.sismember("set", "set0"));
		System.out.println("4.删除集合某个元素(set0,set0)?"+jedis.srem("set", "set0","set1"));
		System.out.println("5.判断集合是否有(set0)某个值?"+jedis.sismember("set", "set0"));
		System.out.println("返回集合元素的个数?"+jedis.scard("set"));
		String delOne = jedis.spop("set");
		System.out.println("删除了某任意个值===>"+delOne);
		System.out.println("6.判断集合是否有("+delOne+")某个值?"+jedis.sismember("set", delOne));
		System.out.println("返回集合元素的个数?"+jedis.scard("set"));
		System.out.println("7.随机取一个元素?"+jedis.srandmember("set"));
		System.out.println("返回集合元素的个数?"+jedis.scard("set"));
		System.out.println("***********遍历source集合**********");
		jedis.sadd("source", "source1","source2","set6","set7");
		Set<String> smembers2 = jedis.smembers("source"); 
		for (String smember : smembers2) {
			System.out.println("source==="+smember);
		}
		System.out.println("8.把source的source1移动到dest集合中?"+jedis.smove("source", "set", "source1"));
		System.out.println("判断set集合是否有(source1)某个值?"+jedis.sismember("set", "source1"));
		
		System.out.println("9.求key1 key2的交集并存在res里==>"+jedis.sinterstore("res", "set","source"));
		
		System.out.println("***********遍历res集合**********");
		Set<String> smembers3 = jedis.smembers("res"); 
		for (String smember : smembers3) {
			System.out.println("res==="+smember);
		}
		System.out.println("10.求key1 key2 key3的交集");
		Set<String> jaoji = jedis.sinter("set","source");
		for (String temp : jaoji) {
			System.out.println("set & source====>"+temp);
		}
		System.out.println("11.求key1 key2 key3的并集");
		Set<String> bingji = jedis.sunion("set","source");
		for (String temp : bingji) {
			System.out.println("set || source====>"+temp);
		}
		System.out.println("12.求key1 key2 key3的差集");
		Set<String> chaji = jedis.sdiff("set","source");
		for (String temp : chaji) {
			System.out.println("set not & source====>"+temp);
		}
	
	}
	
	/**
	 * Jedis对ZSet操作
	 */
	@Test
	public void JedisOperateZSet(){
		Map<String, Double> scoreMembers = new HashMap<String, Double>();
		scoreMembers.put("zset1", 52.3);
		scoreMembers.put("zset2", 32.5);
		scoreMembers.put("zset3", 42.6);
		scoreMembers.put("zset4", 12.7);
		scoreMembers.put("zset5", 92.2);
		System.out.println("1.添加元素==>"+jedis.zadd("zset", scoreMembers ));
		Set<String> zrange = jedis.zrange("zset", 0, -1);
		//jedis.zrevrangeWithScores("zset", 0, -1);反序排序
		System.out.println("遍历所有元素（默认升序）==>"+zrange);
		System.out.println("2.查询member(zset2)的排名（升序0名开始）==>"+jedis.zrank("zset", "zset2"));
		System.out.println("3.返回[min, max]区间内元素数量==>"+jedis.zcount("zset", 12, 40));
		Set<String> zrangeByScore = jedis.zrangeByScore("zset", 20, 100, 0, 3);
		System.out.println("4.集合（升序）排序后取score在[min, max]内的元素，并跳过offset个，取出N个===>"+zrangeByScore);
		System.out.println("按照score来删除元素，删除score在[min, max]之间==>"+jedis.zremrangeByRank("zset", 0, 2));
		System.out.println("遍历所有元素==>"+jedis.zrange("zset",0,-1));
	}
}
