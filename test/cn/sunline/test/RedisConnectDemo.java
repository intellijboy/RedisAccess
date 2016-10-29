package cn.sunline.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectDemo {

	@Test
	public void testRedisConnect() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(200);// 设置对多空闲状态
		jedisPoolConfig.setMaxWaitMillis(10000);// 最大等待时间
		jedisPoolConfig.setTestOnBorrow(false);
		jedisPoolConfig.setMaxTotal(1024);// 设置最大连接数
		JedisPool jedisPool = new JedisPool(jedisPoolConfig, "192.168.56.12", 6379);
		Jedis jedis = jedisPool.getResource();
		jedis.set("name", "刘卜铷");
		System.out.println("name=" + jedis.get("name"));
		System.out.println("PING-->" + jedis.ping());
	}

	/**
	 * 创建xml文本
	 */
	@Test
	public void createXml() {
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("dragonBall");
		// 第一个节点
		Element character1 = root.addElement("character");
		character1.addAttribute("name", "卡卡罗特");
		character1.addAttribute("age", "23");
		// 第二个节点
		Element character2 = root.addElement("character");
		character2.addAttribute("name", "贝吉塔");
		character2.addAttribute("age", "26");
		// 第三个节点
		Element character3 = root.addElement("character");
		character3.addAttribute("name", "布尔玛");
		character3.addAttribute("age", "18");
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			XMLWriter xw = new XMLWriter(new FileOutputStream("mine.xml"), format);
			xw.write(doc);
			xw.flush();
			xw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Over");
		}
	}

	/**
	 * 读取xml文件
	 * 
	 * @throws DocumentException
	 */
	@Test
	public void readXml() throws DocumentException {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(new File("test/mine.xml"));
		Element root = document.getRootElement();
		Iterator<Element> iterator = root.elementIterator();
		while (iterator.hasNext()) {
			Element character = iterator.next();
			String name = character.attribute("name").getValue();
			String age = character.attribute("age").getValue();
			System.out.println("name=" + name + " age=" + age);
		}
	}

	/**
	 * properties文件从类路径中读取方法
	 * @throws IOException
	 */
	@Test
	public void readProperties() throws IOException {
		 Properties prop = new Properties(); 
		 prop.load(RedisConnectDemo.class.getClassLoader().getResourceAsStream("test/redis_url.properties"));
		Iterator<String> it = prop.stringPropertyNames().iterator();
		while (it.hasNext()) {
			String key = it.next();
			System.out.println(key + ":" + prop.getProperty(key));
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void readXmlFromClassPath() throws DocumentException {
		SAXReader saxReader = new SAXReader();
		InputStream resourceAsStream = RedisConnectDemo.class.getClassLoader().getResourceAsStream("test/mine.xml");
		Document document = saxReader.read(resourceAsStream);
		Element root = document.getRootElement();
		Iterator<Element> iterator = root.elementIterator();
		while (iterator.hasNext()) {
			Element character = iterator.next();
			String name = character.attribute("name").getValue();
			String age = character.attribute("age").getValue();
			System.out.println("name=" + name + " age=" + age);
		}
	}
	
	private Logger logger = LoggerFactory.getLogger(RedisConnectDemo.class);
	/**
	 * 日志测试
	 */
	@Test
	public void testLog(){
		System.out.println(logger);
		logger.error("错误提示：{}","fuck!!1");
		
		try {
			int i=10/0;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("异常*"+e.getMessage(),e);
		}
	}
}
