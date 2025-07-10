package org.example.holidaymailer.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "email_send_record")
public class EmailSendRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 员工ID，用于关联和追溯
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    // 员工姓名，CozeBot输入和邮件内容需要
    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    // 员工邮箱，邮件发送目标
    @Column(name = "employee_email", nullable = false)
    private String employeeEmail;


    // 计划发送日期，用于调度器筛选
    @Column(name = "send_date", nullable = false)
    private LocalDate sendDate;

    // 邮件主题，CozeBot和邮件服务输入
    @Column(name = "subject", nullable = false)
    private String subject;

    // AI生成的内容，发送前存储，发送失败重试时直接用
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 邮件发送状态，核心字段
    @Column(name = "status", nullable = false)
    private String status;

    // 上次尝试发送时间，用于重试间隔
    @Column(name = "last_attempt_time")
    private LocalDateTime lastAttemptTime;

    // 重试次数，核心字段
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    // 错误信息，方便排查
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // 实际发送成功时间，用于统计和审计
    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    // 记录创建时间
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    // 记录更新时间
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    // --- Constructors ---
    public EmailSendRecord() {
        this.status = "PENDING";
        this.retryCount = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public EmailSendRecord(Long employeeId, String employeeName, String employeeEmail, String subject) {
        this(); // 调用默认构造函数设置默认值
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.sendDate = LocalDate.now();
        this.subject = subject;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
