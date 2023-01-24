package com.company.demo.screen.department;

import io.jmix.ui.screen.*;
import com.company.demo.entity.Department;

@UiController("Department.edit")
@UiDescriptor("department-edit.xml")
@EditedEntityContainer("departmentDc")
public class DepartmentEdit extends StandardEditor<Department> {
}