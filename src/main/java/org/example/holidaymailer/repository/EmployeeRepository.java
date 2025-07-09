package org.example.holidaymailer.repository;

import org.example.holidaymailer.entity.Employee;
import org.example.holidaymailer.entity.NameEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAll();

    @Query("SELECT e FROM Employee e WHERE MONTH(e.birthday) = :month AND DAY(e.birthday) = :day")
    List<Employee> findByBirthday(@Param("month") int month, @Param("day") int day);

    @Query("SELECT e.name AS name,e.email AS email FROM Employee e WHERE MONTH(e.birthday) = :month AND DAY(e.birthday) = :day")
    List<NameEmail> findNameEmailByBirthday(@Param("month") int month, @Param("day") int day);

    @Query("SELECT e.email FROM Employee e")
    List<String> findAllEmails();

    @Query("SELECT e.name AS name,e.email AS email FROM Employee e")
    List<NameEmail> findAllNameEmails();


    List<Employee> findDistinctByBirthday(LocalDate birthday);
}
