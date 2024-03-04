package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerTest {

    @Autowired
    private EmployeeController employeeController;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String reportStructureUrl;
    private String compensationReadUrl;
    private String compensationCreateUrl;

    @BeforeEach
    public void setup() {
        reportStructureUrl = "http://localhost:" + port + "/employee/{id}/reports";
        compensationReadUrl = "http://localhost:" + port + "/employee/{id}/compensation";
        compensationCreateUrl = "http://localhost:" + port + "/employee/compensation";
    }

    @Test
    public void testReportingStructure1() {

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
    public void testReportingStructure2() {

        Employee employee = employeeController.read("b7839309-3348-463b-a7e3-5de1c168beb3");

        assertNotNull(employee);
        assertNotNull(employee.getEmployeeId());
        assertEquals("Paul", employee.getFirstName());
        assertEquals("McCartney", employee.getLastName());
        assertEquals("Developer I", employee.getPosition());
        assertEquals("Engineering", employee.getDepartment());

        ReportingStructure reportingStructure = restTemplate.getForEntity(reportStructureUrl, ReportingStructure.class, employee.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertNotNull(reportingStructure.getEmployee());

        assertEquals(0, reportingStructure.getNumberOfReports());

    }

    @Test
    public void testReportingStructure3() {

        Employee employee = employeeController.read("03aa1462-ffa9-4978-901b-7c001562cf6f");

        assertNotNull(employee);
        assertNotNull(employee.getEmployeeId());
        assertEquals("Ringo", employee.getFirstName());
        assertEquals("Starr", employee.getLastName());
        assertEquals("Developer V", employee.getPosition());
        assertEquals("Engineering", employee.getDepartment());

        ReportingStructure reportingStructure = restTemplate.getForEntity(reportStructureUrl, ReportingStructure.class, employee.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertNotNull(reportingStructure.getEmployee());

        assertEquals(2, reportingStructure.getNumberOfReports());

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
        Compensation createdCompensation = restTemplate.postForEntity(compensationCreateUrl, compensation, Compensation.class).getBody();

        assertNotNull(createdCompensation);
        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEffectiveDate());
        assertNotNull(createdCompensation.getSalary());

        assertEquals(employee.getEmployeeId(), createdCompensation.getEmployee().getEmployeeId());

        assertCompensationEquivalence(compensation, createdCompensation);

        // Read checks
        Compensation readCompensation = restTemplate.getForEntity(compensationReadUrl, Compensation.class, employee.getEmployeeId()).getBody();

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
