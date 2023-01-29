package com.company.demo;

import com.company.demo.entity.Department;
import com.company.demo.entity.Employee;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class EntityManagerTest {

    private static final int BATCH_SIZE = 100;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    TestHelper helper;

    List<Department> departments;

    @BeforeEach
    void setUp() {
        jdbc.execute("delete from EMPLOYEE");
        jdbc.execute("delete from DEPARTMENT");

        departments = helper.createDepartments();

        // warm up
        Faker faker = new Faker();
        for (int i = 0; i < 10; i++) {
            Employee employee = helper.createEmployee(faker, departments);
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                entityManager.persist(employee);
            });
        }
    }

    @Test
    void oneByOne() {
        Faker faker = new Faker();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                Employee employee = helper.createEmployee(faker, departments);
                entityManager.persist(employee);
            });
        }

        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("- EntityManagerTest.oneByOne: " + execTime + " ms");
    }

    @Test
    void allAtOnce() {
        Faker faker = new Faker();
        long startTime = System.currentTimeMillis();

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
                Employee employee = helper.createEmployee(faker, departments);
                entityManager.persist(employee);
            }
        });

        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("- EntityManagerTest.allAtOnce: " + execTime + " ms");
    }

    @Test
    void inBatches() {
        Faker faker = new Faker();
        long startTime = System.currentTimeMillis();

        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
            Employee employee = helper.createEmployee(faker, departments);
            employees.add(employee);
            if (i > 0 && i % BATCH_SIZE == 0) {
                transactionTemplate.executeWithoutResult(transactionStatus -> {
                    for (Employee e : employees) {
                        entityManager.persist(employee);
                    }
                });
                employees.clear();
            }
        }

        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("- EntityManagerTest.inBatches (" + BATCH_SIZE + "): " + execTime + " ms");
    }

}
