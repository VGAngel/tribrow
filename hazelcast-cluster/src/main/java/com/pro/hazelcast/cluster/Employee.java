package com.pro.hazelcast.cluster;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/21/13
 * Time: 4:14 PM
 */
public class Employee implements Serializable {
    private String name;
    private int age;
    private boolean active;
    private double salary;

    public Employee(String name, int age, boolean live, double price) {
        this.name = name;
        this.age = age;
        this.active = live;
        this.salary = price;
    }

    public Employee() {
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getSalary() {
        return salary;
    }

    public boolean isActive() {
        return active;
    }
}