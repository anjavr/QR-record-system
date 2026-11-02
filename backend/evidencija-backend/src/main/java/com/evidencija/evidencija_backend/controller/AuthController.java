package com.evidencija.evidencija_backend.controller;

import com.evidencija.evidencija_backend.model.Student;
import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.service.DomainValidator;
import com.evidencija.evidencija_backend.service.StudentService;
import com.evidencija.evidencija_backend.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmployeeService employeeService;
    private final StudentService studentService;
    private final DomainValidator domainValidator;

    public AuthController(EmployeeService employeeService, StudentService studentService, DomainValidator domainValidator) {
        this.employeeService = employeeService;
        this.studentService = studentService;
        this.domainValidator = domainValidator;
    }


    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        domainValidator.validate(email);

        List<String> roles = extractRoles(jwt);

        if (roles.contains("employee")) {
            Employee employee = employeeService.findOrCreate(jwt);
            return Map.of("role", "EMPLOYEE", "data", employee);
        }

        if (roles.contains("student")) {
            Student student = studentService.findByEmail(email);
            return Map.of("role", "STUDENT", "data", student);
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User does not have an assigned role (employee/student) in the system"
        );
    }

    //get Keycloack realm roles from token
    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }

        return (List<String>) realmAccess.get("roles");
    }
}
