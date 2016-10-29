package cn.sunline.redis.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sunline.test.RedisConnectDemo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis获取Jedis对象和释放资源工具类
 * @author liuburu
 *
 */
public class JedisUtil {
	//使用slf4j做日志
	private static Logger logger = LoggerFactory.getLogger(JedisUtil.class);
	
	private static JedisPool jedisPool;
	//静态加载，初始化JedisPool中的连接池参数配置
	static{
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		Map<String,String> redisConfigMap = initRedisConfigMap();
		jedisPoolConfig.setMaxIdle(Integer.parseInt(redisConfigMap.get("maxIdle")));// 设置对多空闲状态
		jedisPoolConfig.setMaxWaitMillis(Integer.parseInt(redisConfigMap.get("maxWaitMillis")));// 最大等待时间
		jedisPoolConfig.setTestOnBorrow(Boolean.parseBoolean(redisConfigMap.get("testOnBorrow")));
		jedisPoolConfig.setMaxTotal(Integer.parseInt(redisConfigMap.get("maxTotal")));// 设置最大连接数
		jedisPool = new JedisPool(jedisPoolConfig, redisConfigMap.get("ip"),Integer.parseInt(redisConfigMap.get("port")));
	}

	/**
	 * 从JedisPool中获取Jedis对象
	 * @return
	 */
	public static synchronized Jedis  getJedis(){
		if(jedisPool!=null){
			Jedis jedis = jedisPool.getResource();
			if(jedis!=null){
				logger.debug("Jedis获取成功==>{}",jedis);
			}
			return jedis;
		}else{
			logger.error("JedisPool 初始化失败:{}",jedisPool); 
		}
		return null;
	}
	
	/**
	 * 释放Jedis资源
	 * @param jedis
	 */
	public static void realeseResources(Jedis jedis){
		if(jedis!=null){
			jedisPool.returnBrokenResource(jedis);
			logger.debug("Jedis连接已释放==>{}",jedis);
		}
	}
		
	/**
	 * 初始化redis的配置参数
	 * @return
	 */
	private static Map<String, String> initRedisConfigMap() {
		Map<String,String> configMap = new HashMap<String, String>();
		SAXReader saxReader = new SAXReader();
		InputStream resourceAsStream = RedisConnectDemo.class.getClassLoader().getResourceAsStream("redis/redis-config.xml");
		Document document = null;
		try {
			document = saxReader.read(resourceAsStream);
		} catch (DocumentException e) {
			logger.error("redis配置文件配置错误：{}",e.getMessage());
		}
		Element root = document.getRootElement();
		Iterator<Element> iterator = root.elementIterator();
		while (iterator.hasNext()) {
			Element character = iterator.next();
			String id = character.attribute("id").getValue();
			String value = character.attribute("value").getValue();
			configMap.put(id, value);
			//System.out.println("id=" + id + " value=" + value);
		}
		return configMap;
	}
}
