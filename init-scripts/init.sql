-- 删除所有表（如果存在）
DROP TABLE IF EXISTS email_log;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS holiday;

-- 创建节假日表
CREATE TABLE holiday (
                         id SERIAL PRIMARY KEY,
                         date DATE NOT NULL UNIQUE,
                         name VARCHAR(100)
);

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
                           send_time TIMESTAMP,
                           success BOOLEAN,
                           error_message TEXT
);

-- 插入节假日
INSERT INTO holiday (date, name) VALUES
                                     ('2025-01-01', '元旦节'),
                                     ('2025-05-01', '劳动节'),
                                     ('2025-10-01', '国庆节');

-- 插入员工数据
INSERT INTO employee (email, name, birthday) VALUES
                                       ('epiiplus@outlook.com', 'epii','2025-10-01'),
                                       ('lanran408@gmial.com', 'lanran','2025-10-01');
