package com.evidencija.evidencija_backend.service;

import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.repository.EmployeeRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    //employee is able to login or "register" through login on the first visit
    public Employee findOrCreate(Jwt jwt) {
        String email = jwt.getClaimAsString("email");

        return employeeRepository.findByEmail(email)
                .orElseGet(() -> {
                    Employee newEmployee = new Employee();
                    newEmployee.setEmail(email);
                    newEmployee.setFirstName(jwt.getClaimAsString("given_name"));
                    newEmployee.setLastName(jwt.getClaimAsString("family_name"));
                    return employeeRepository.save(newEmployee);
                });
    }
}