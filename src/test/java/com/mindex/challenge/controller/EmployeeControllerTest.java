package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerTest {

    @Autowired
    private EmployeeController employeeController;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String reportStructureUrl;
    private String compensationUrl;

    @Before
    public void setup() {
        reportStructureUrl = "http://localhost:" + port + "/employee/{id}/reports";
        compensationUrl = "http://localhost:" + port + "/employee/{id}/compensation";
    }

    @Test
    public void testReportingStructure() {

        Employee employee = employeeController.read("16a596ae-edd3-4847-99fe-c4518e82c86f");

        assertNotNull(employee);
        assertNotNull(employee.getEmployeeId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Lennon", employee.getLastName());
        assertEquals("Development Manager", employee.getPosition());
        assertEquals("Engineering", employee.getDepartment());

        ReportingStructure reportingStructure = restTemplate.getForEntity(reportStructureUrl, ReportingStructure.class, employee.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertNotNull(reportingStructure.getEmployee());

        assertEquals(4, reportingStructure.getNumberOfReports());

    }

    @Test
    public void testCreateReadCompensation() {

        Employee employee = employeeController.read("16a596ae-edd3-4847-99fe-c4518e82c86f");

        assertNotNull(employee);
        assertNotNull(employee.getEmployeeId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Lennon", employee.getLastName());
        assertEquals("Development Manager", employee.getPosition());
        assertEquals("Engineering", employee.getDepartment());

        Compensation compensation = new Compensation();
        compensation.setEmployee(employee);
        compensation.setSalary(new BigDecimal("190000.70"));
        compensation.setEffectiveDate(new Date());

        // Create checks
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, compensation, Compensation.class, employee.getEmployeeId()).getBody();

        assertNotNull(createdCompensation);
        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEffectiveDate());
        assertNotNull(createdCompensation.getSalary());

        assertEquals(employee.getEmployeeId(), createdCompensation.getEmployee().getEmployeeId());

        assertCompensationEquivalence(compensation, createdCompensation);

        // Read checks
        Compensation readCompensation = restTemplate.getForEntity(compensationUrl, Compensation.class, employee.getEmployeeId()).getBody();

        assertNotNull(readCompensation);
        assertNotNull(readCompensation.getEmployee());
        assertNotNull(readCompensation.getEffectiveDate());
        assertNotNull(readCompensation.getSalary());

        assertEquals(employee.getEmployeeId(), readCompensation.getEmployee().getEmployeeId());

        assertCompensationEquivalence(createdCompensation, readCompensation);

    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployee().getEmployeeId(), actual.getEmployee().getEmployeeId());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
        assertEquals(expected.getSalary(), actual.getSalary());
    }
}
