package org.example.holidaymailer.repository;

import org.example.holidaymailer.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAll();

    @Query("SELECT e FROM Employee e WHERE MONTH(e.birthday) = :month AND DAY(e.birthday) = :day")
    List<Employee> findByBirthdayMonthAndDay(@Param("month") int month, @Param("day") int day);


    @Query("SELECT e.email FROM Employee e")
    List<String> findAllEmails();
}
