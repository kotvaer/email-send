-- 删除所有表（如果存在）
DROP TABLE IF EXISTS email_log;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS email_send_record;
DROP TABLE IF EXISTS email_send_records;


-- 创建员工邮箱表
CREATE TABLE employee (
                          id SERIAL PRIMARY KEY,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          name VARCHAR(100),
                          birthday DATE
);

-- 创建邮件日志表
CREATE TABLE email_log (
                           id SERIAL PRIMARY KEY,
                           recipient VARCHAR(255),
                           subject VARCHAR(255),
                           content TEXT,
                           status VARCHAR(20),
                           retry_count INT,
                           created_at TIMESTAMP
);


CREATE TABLE email_send_records (
                                    id BIGSERIAL PRIMARY KEY,
                                    employee_id BIGINT NOT NULL,
                                    employee_name VARCHAR(255) NOT NULL,
                                    employee_email VARCHAR(255) NOT NULL,
                                    email_type VARCHAR(50) NOT NULL,
                                    send_date DATE NOT NULL,
                                    subject VARCHAR(255) NOT NULL,
                                    content TEXT,
                                    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                    last_attempt_time TIMESTAMP,
                                    retry_count INT NOT NULL DEFAULT 0,
                                    error_message TEXT,
                                    sent_time TIMESTAMP,
                                    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 添加索引以提高查询效率
CREATE INDEX idx_email_status_send_date ON email_send_records (status, send_date);
CREATE INDEX idx_email_employee_id_send_date ON email_send_records (employee_id, send_date);

-- 添加触发器，用于在记录更新时自动更新 update_time 字段
CREATE OR REPLACE FUNCTION update_timestamp_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_email_send_records_timestamp
    BEFORE UPDATE ON email_send_records
    FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

-- 插入员工数据
INSERT INTO employee (email, name, birthday) VALUES
                                       ('epiiplus@outlook.com', 'epii','2025-10-01');
