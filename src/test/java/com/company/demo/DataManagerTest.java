package com.company.demo;

import com.company.demo.entity.Department;
import com.company.demo.entity.Employee;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@SpringBootTest
class DataManagerTest {

	private static final int BATCH_SIZE = 100;

	@Autowired
	DataManager dataManager;

	@Autowired
	SystemAuthenticator authenticator;

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
			authenticator.runWithSystem(() ->
					dataManager.save(employee)
			);
		}
	}

	@Test
	void oneByOne() {
		Faker faker = new Faker();
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
			Employee employee = helper.createEmployee(faker, departments);

			authenticator.runWithSystem(() ->
					dataManager.save(employee)
			);
		}

		long execTime = System.currentTimeMillis() - startTime;
		System.out.println("- DataManagerTest.oneByOne: " + execTime + " ms");
	}

	@Test
	void oneByOne_discardSaved() {
		Faker faker = new Faker();
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
			Employee employee = helper.createEmployee(faker, departments);

			authenticator.runWithSystem(() -> {
				SaveContext saveContext = new SaveContext().saving(employee).setDiscardSaved(true);
				dataManager.save(saveContext);
			});
		}

		long execTime = System.currentTimeMillis() - startTime;
		System.out.println("- DataManagerTest.oneByOne_discardSaved: " + execTime + " ms");
	}

	@Test
	void oneByOne_discardSaved_unconstrained() {
		Faker faker = new Faker();
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
			Employee employee = helper.createEmployee(faker, departments);

			SaveContext saveContext = new SaveContext().saving(employee).setDiscardSaved(true);
			dataManager.unconstrained().save(saveContext);
		}

		long execTime = System.currentTimeMillis() - startTime;
		System.out.println("- DataManagerTest.oneByOne_discardSaved_unconstrained: " + execTime + " ms");
	}

	@Test
	void allAtOnce_discardSaved_unconstrained() {
		Faker faker = new Faker();
		long startTime = System.currentTimeMillis();

		SaveContext saveContext = new SaveContext().setDiscardSaved(true);
		for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
			Employee employee = helper.createEmployee(faker, departments);
			saveContext.saving(employee);
		}
		dataManager.unconstrained().save(saveContext);

		long execTime = System.currentTimeMillis() - startTime;
		System.out.println("- DataManagerTest.allAtOnce_discardSaved_unconstrained: " + execTime + " ms");
	}

	@Test
	void inBatches() {
		Faker faker = new Faker();
		long startTime = System.currentTimeMillis();

		authenticator.runWithSystem(() -> {
			SaveContext saveContext = new SaveContext().setDiscardSaved(true);
			for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
				Employee employee = helper.createEmployee(faker, departments);
				saveContext.saving(employee);
				if (i > 0 && (i % BATCH_SIZE == 0 || i == TestHelper.EMPLOYEE_COUNT - 1)) {
					dataManager.save(saveContext);
					saveContext = new SaveContext().setDiscardSaved(true);
				}
			}
		});

		long execTime = System.currentTimeMillis() - startTime;
		System.out.println("- DataManagerTest.inBatches ("+ BATCH_SIZE +"): " + execTime + " ms");
	}

	@Test
	void inBatches_unconstrained() {
		Faker faker = new Faker();
		long startTime = System.currentTimeMillis();

		SaveContext saveContext = new SaveContext().setDiscardSaved(true);
		for (int i = 0; i < TestHelper.EMPLOYEE_COUNT; i++) {
			Employee employee = helper.createEmployee(faker, departments);
			saveContext.saving(employee);
			if (i > 0 && (i % BATCH_SIZE == 0 || i == TestHelper.EMPLOYEE_COUNT - 1)) {
				dataManager.unconstrained().save(saveContext);
				saveContext = new SaveContext().setDiscardSaved(true);
			}
		}

		long execTime = System.currentTimeMillis() - startTime;
		System.out.println("- DataManagerTest.inBatches_unconstrained ("+ BATCH_SIZE +"): " + execTime + " ms");
	}

}
