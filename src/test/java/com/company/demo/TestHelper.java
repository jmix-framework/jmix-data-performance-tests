package com.company.demo;

import com.company.demo.entity.Department;
import com.company.demo.entity.Employee;
import com.company.demo.entity.Gender;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestHelper {

    static final int EMPLOYEE_COUNT = 10000;
    @Autowired
    private DataManager dataManager;

    @Authenticated
    public List<Department> createDepartments() {
        List<Department> result = new ArrayList<>();

        Department department = dataManager.create(Department.class);
        department.setName("Finance");
        result.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("Accounting");
        result.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("Operations");
        result.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("IT");
        result.add(dataManager.save(department));

        return result;
    }

    public Employee createEmployee(Faker faker, List<Department> departments) {
        Employee employee = dataManager.create(Employee.class);
        employee.setFirstName(faker.name().firstName());
        employee.setLastName(faker.name().lastName());
        employee.setBirthDate(faker.date().birthday().toLocalDateTime().toLocalDate());
        employee.setGender(Gender.fromId(faker.gender().shortBinaryTypes().toUpperCase()));
        employee.setAddress(faker.address().fullAddress());
        employee.setPhone(faker.phoneNumber().cellPhone());
        employee.setPassport(faker.idNumber().valid());
        employee.setDepartment(departments.get(faker.number().numberBetween(0, departments.size() - 1)));
        return employee;
    }
}
