package org.example.holidaymailer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Entity
@NoArgsConstructor
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private LocalDate birthday;

    public boolean isBirthday(LocalDate birthday) {
        return  this.birthday != null &&
                this.birthday.getMonth() == birthday.getMonth() &&
                this.birthday.getDayOfMonth() == birthday.getDayOfMonth();
    }
}

