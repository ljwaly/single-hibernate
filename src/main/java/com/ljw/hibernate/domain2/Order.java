package com.ljw.hibernate.domain2;

public class Order {

	private Integer id;
	private String name;
	private Customer2 customer;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Customer2 getCustomer() {
		return customer;
	}
	public void setCustomer(Customer2 customer) {
		this.customer = customer;
	}
	@Override
	public String toString() {
		return "Order [id=" + id + ", name=" + name + ", customer=" + customer + "]";
	}

}
