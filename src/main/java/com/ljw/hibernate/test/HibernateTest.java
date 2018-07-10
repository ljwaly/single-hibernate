package com.ljw.hibernate.test;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.ljw.hibernate.util.HibernateUtil;
import com.ljw.hibernate.domain.Customer;

public class HibernateTest {

	@Test
	public void test1(){
		
		Configuration configuration = new Configuration().configure();
		SessionFactory sessionFactory = configuration.buildSessionFactory();
		Session session = sessionFactory.openSession();
		
		Transaction transaction =session.beginTransaction();
		Customer customer = new Customer();

		customer.setName("lamp");
		customer.setAge(12);
		customer.setCity("shanghai");
		
		session.save(customer);
		
		
		transaction.commit();
		
		session.close();
		sessionFactory.close();
	}
	
	@Test
	public void testFirstCache() {

		Session session = HibernateUtil.openSession();
		session.beginTransaction();

		// 第一次获取，先走session缓存，没有才会发出sql语句
		// 我们知道，第一次肯定会发出sql语句查询数据库,查询数据库
		// 查询出来之后，这个oid=1的对象就保存在session缓存中了
		Customer customer = (Customer) session.get(Customer.class, 7);

		System.out.println(customer);
		// 第二次获取id=1的对象，此时，系统会先去session缓存中查找，存不存在id=1的对象
		// 存在，就直接返回这个对象，如果不存在，则去数据库查询
		Customer customer2 = (Customer) session.get(Customer.class, 7);

		System.out.println(customer2);

		// 第三次，步骤同第二次
		Customer customer3 = (Customer) session.get(Customer.class, 7);

		System.out.println(customer3);

		session.getTransaction().commit();
		session.close();

	}

	// 一级缓存与session同生共死
	@Test
	public void testFirstCachelifecycle() {
		/**
		 * 证明的思路： 先获取一个session1，然后查询数据，紧接着关闭这个session
		 * 在获取一个session2，然后查询数据，观察，控制台是否发出sql语句
		 */
		Session session1 = HibernateUtil.openSession();
		session1.beginTransaction();

		Customer customer1 = (Customer) session1.get(Customer.class, 7);
		System.out.println(customer1);

		session1.getTransaction().commit();
		session1.close();

		/***********************/
		Session session2 = HibernateUtil.openSession();
		session2.beginTransaction();

		Customer customer2 = (Customer) session2.get(Customer.class, 7);
		System.out.println(customer2);

		session2.getTransaction().commit();
		session2.close();
	}

	/**
	 * 证明一级缓存快照的能力（作用 ）
	 * 不需要update，在session提交的时候，直接就进行对数据库的同步处理了
	 */
	@Test
	public void testSnapShot() {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		// 这是获取，直接发出sql语句查询数据库
		// 在获取到数据之后，会将数据先放入一级缓存， 然后在放入一级缓存的快照
		Customer customer = (Customer) session.get(Customer.class, 7);

		System.out.println(customer);

		// 直接改变customer的值
		customer.setName("tom2");

		// flush:会直接发出update更新语句，更新数据库
		// session.flush();
		// 其实，咋们没必要手动的flush，因为当每次commit之前，都会自动的隐式进行flush操作
		session.getTransaction().commit();
		session.close();
	}

	/**
	 * 一级缓存的测试
	 * 
	 * 在事物没有提交的时候，对obj的修改先进性修改的是一级缓存session，如果没有提交的话，不更新数据库；
	 * 再次提取此obj，会优先查询session一级缓存中的数据，如果有，直接进行提取，已经发生改变，所以提取出来的是改变的
	 * 然后提交的时候，会进行数据库的更新
	 */
	@Test
	public void testflushcache3() {
		// 先学习get和load方法的处理方式
		Session session = HibernateUtil.openSession();
		session.beginTransaction();

		Customer customer = (Customer) session.get(Customer.class,7);
		System.out.println(customer);

		// 修改Customer的值
		customer.setName("1234");
		// 此时发出update语句吗？不会，因为缓存没有flush

		// get和load的处理方式
		Customer customer2 = (Customer) session.load(Customer.class, 7);
		// 问题：值是多少？name=?
		// 由于get、load方式直接走一级缓存，它只是去判断oid是否一致，如果一致，就认为是同一个对象，
		// /然后返回这个对象
		System.out.println(customer2);

		// query的处理方式

		session.getTransaction().commit();
		session.close();
	}

	
	/**
	 * 使用sql创建语句，没有读取session一级缓存中的数据，直接去读取数据库数据了
	 */
	@Test
	public void testflushcache3_2() {

		Session session = HibernateUtil.openSession();
		session.beginTransaction();

		// 当get的时候，一级缓存中肯定有数据库
		Customer customer = (Customer) session.get(Customer.class, 7);
		// 此时打印的信息肯定是数据库中的信息
		System.out.println(customer);

		// query的处理方式
		// 问题：执行下面这行代码的时候，会不会发出sql语句查询数据库？
		Customer customer2 = (Customer) session.createQuery("from Customer where id = 7").uniqueResult();
		System.out.println(customer2);

		session.getTransaction().commit();
		session.close();
	}

	
	
	/**
	 * 设置数据库刷新操作为手动刷新操作
	 * 即FlushMode=FlushMode.MANUAL
	 * 只能使用session.flush()来进行刷新操作
	 */
	@Test
	public void testFlushMode() {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();

		// 改变缓存的刷出时机,MANUAL模式只有在flush的时候才会发出update语句
		session.setFlushMode(FlushMode.MANUAL);

		Customer customer = (Customer) session.get(Customer.class, 7);
		System.out.println(customer);

		customer.setName("tom");
		// MANUAL模式下，只能手动的flush
		session.flush();
		
		customer.setName("tom323");//此次修改则不会被执行数据库更新操作，
		
		// 因为设置flush为MANUAL模式，commit不会发出update语句
		session.getTransaction().commit();
		session.close();

	}

	@Test
	public void testClearAndEvict1() {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		// 要清除缓存，第一步，得先将数据加载入一级缓存
		Customer customer = (Customer) session.get(Customer.class, 7);
		System.out.println(customer);
		// 清除一级缓存中所有的数据
		// session.clear();
		// 清除一级缓存中的指定对象
		session.evict(customer);

		// 此时会不会发出sql语句？答：会！肯定会
		Customer customer2 = (Customer) session.get(Customer.class, 7);

		System.out.println(customer2);

		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testRefresh() {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		// 此时会发出sql查询语句，查询数据库
		Customer customer = (Customer) session.get(Customer.class, 7);
		System.out.println(customer);

		// 改变customer的属性的值
		// customer.setName("rose");

		// System.out.println(customer);

		// refresh方法的作用：不管一级缓存的数据和快照是否一致，
		// 只要调用refresh方法，就会去数据库查询数据，然后覆盖一级缓存和快照中的数据
		session.refresh(customer);

		System.out.println(customer);
		session.getTransaction().commit();
		session.close();
	}

	
	
}
