package com.ljw.hibernate.test;

import org.hibernate.Session;
import org.junit.Test;

import com.ljw.hibernate.domain2.Customer2;
import com.ljw.hibernate.domain2.Order;
import com.ljw.hibernate.util.HibernateUtil;

public class HibernateTest2 {

	/*
	 * 双向关联保存
	 */
	@Test
	public void testSave(){
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		
		Customer2 customer = new Customer2();
		customer.setName("kebi");
		
		Order order =new Order();
		order.setName("dengken");
		
		// 双向建立关系
		customer.getOrders().add(order);
		order.setCustomer(customer);
		
		// 必须同时保存两个对象，否则数据库不会进行保存处理
		session.save(order);
		session.save(customer);
		
		session.getTransaction().commit();
		session.close();
		
	}
	
	@Test
	public void testSave2(){
		
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		
		Customer2 customer = new Customer2();
		customer.setName("剑圣");
		
		Order o1=new Order();
		o1.setName(customer.getName()+"的订单1");
		
		Order o2=new Order();
		o2.setName(customer.getName()+"的订单2");
		
		Order o3=new Order();
		o3.setName(customer.getName()+"的订单3");
		
		Order o4=new Order();
		o4.setName(customer.getName()+"的订单3");
		
		
		// 当级联保存的时候，当我们在Customer.hbm.xml中设置了级联关系的时候，
		// 那么在设置关系的时候，只需要向Customer的orders集合中添加Order，就可以进行级联保存
		customer.getOrders().add(o1);
		customer.getOrders().add(o2);
		customer.getOrders().add(o3);
		customer.getOrders().add(o4);
		
		
		
		session.save(customer);
		
		session.getTransaction().commit();
		session.close();
		
	}
	
	@Test
	public void testDelete() {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();

		// 删除多方
		// Order o1 = new Order();
		// o1.setId(17);
		// session.delete(o1);

		// 删除一方:如果一方是一个脱管态对象，那么先将多方的外键置空（解除关系），然后在删除
		// Customer c1 = new Customer();
		// c1.setId(8);

		// 删除一方：一方是一个持久态对象,也是先解除关系，然后在删除
		// 问题：如果客户都不存在了，那么这个订单还有存在的意义吗？
		// 答：订单的存在已经没有意义了，所以说我们可以通过级联删除，
		// 在删除Customer的时候，自动删除与Customer相关的order
		Customer2 c1 = (Customer2) session.get(Customer2.class, 3);

		session.delete(c1);

		session.getTransaction().commit();
		session.close();
	}
	
	
	
	
}
