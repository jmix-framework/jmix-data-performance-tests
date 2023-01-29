package com.company.demo;

import com.company.demo.entity.Department;
import com.company.demo.entity.Employee;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class JdbcTest {

    private static final int BATCH_SIZE = 100;

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
            jdbc.update("INSERT INTO public.employee(id, version, first_name, last_name, birth_date, gender, phone, address, passport, department_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    ps -> setParameters(employee, ps)
            );
        }
    }

        @Test
    void oneByOne() {
        Faker faker = new Faker();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
            Employee employee = helper.createEmployee(faker, departments);
            jdbc.update("INSERT INTO public.employee(id, version, first_name, last_name, birth_date, gender, phone, address, passport, department_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    ps -> setParameters(employee, ps)
            );
        }

        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("- JdbcTest.oneByOne: " + execTime + " ms");
    }

    @Test
    void inBatches() {
        Faker faker = new Faker();
        long startTime = System.currentTimeMillis();

        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
            Employee employee = helper.createEmployee(faker, departments);
            employees.add(employee);
            if (i > 0 && (i % BATCH_SIZE == 0 || i == TestHelper.EMPLOYEE_COUNT - 1)) {
                jdbc.batchUpdate("INSERT INTO public.employee(id, version, first_name, last_name, birth_date, gender, phone, address, passport, department_id) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",

                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                setParameters(employees.get(i), ps);
                            }

                            @Override
                            public int getBatchSize() {
                                return employees.size();
                            }
                        }
                );

                employees.clear();
            }
        }


        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("- JdbcTest.inBatches (" + BATCH_SIZE + "): " + execTime + " ms");
    }

    private static void setParameters(Employee employee, PreparedStatement ps) throws SQLException {
        int idx = 1;
        ps.setObject(idx, employee.getId());
        ps.setInt(++idx, 1);
        ps.setString(++idx, employee.getFirstName());
        ps.setString(++idx, employee.getLastName());
        ps.setDate(++idx, new java.sql.Date(employee.getBirthDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()));
        ps.setString(++idx, employee.getGender().getId());
        ps.setString(++idx, employee.getPhone());
        ps.setString(++idx, employee.getAddress());
        ps.setString(++idx, employee.getPassport());
        ps.setObject(++idx, employee.getDepartment().getId());
    }

}
