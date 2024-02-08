package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EmployeeController {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/employee/{employeeId}/compensation")
    public Compensation create(@PathVariable String employeeId, @RequestBody Compensation compensation) {
        LOG.debug("Received compensation create request for Employee ID [{}] and compensation [{}]", employeeId, compensation);

        return employeeService.create(compensation);
    }

    @GetMapping("/employee/{employeeId}/compensation")
    public Compensation getCompensationByEmployeeId(@PathVariable String employeeId) {
        LOG.debug("Received compensation get request for Employee ID [{}]", employeeId);

        return employeeService.getCompensationByEmployeeId(employeeId);
    }

    @GetMapping("/employee/{id}")
    public Employee read(@PathVariable String id) {
        LOG.debug("Received employee read request for id [{}]", id);

        return employeeService.read(id);
    }

    @PutMapping("/employee/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        LOG.debug("Received employee create request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        return employeeService.update(employee);
    }

    @GetMapping("/employee/{id}/reports")
    public ReportingStructure getReportingStructure(@PathVariable String id) {
        LOG.debug("Received reporting structure request for id [{}]", id);

        Employee employee = employeeService.read(id);

        // This is being checked in the current EmployeeServiceImpl, but with a different impl class it may not be there
        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(countReports(employee));

        return reportingStructure;
    }

    @PostMapping("/employee")
    public Employee create(@RequestBody Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        return employeeService.create(employee);
    }

    private int countReports(Employee employee) {
        List<Employee> directReports = employee.getDirectReports();
        if (!CollectionUtils.isEmpty(directReports)) {
            int counter = directReports.size();
            for (Employee report : directReports) {
                counter += countReports(employeeService.read(report.getEmployeeId()));
            }
            return counter;
        }
        return 0;
    }
}
