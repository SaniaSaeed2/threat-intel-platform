-- ============================================================
-- Threat Intelligence Platform - Database Schema
-- COMP-370 CCP - PAF-IAST
-- ============================================================

CREATE DATABASE IF NOT EXISTS threatintel;
USE threatintel;

CREATE TABLE IF NOT EXISTS iocs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    type            VARCHAR(20)  NOT NULL COMMENT 'ip or domain',
    value           VARCHAR(512) NOT NULL UNIQUE,
    source          VARCHAR(100) NOT NULL COMMENT 'abuseipdb, alienvault',
    description     TEXT,
    severity_score  INT          DEFAULT NULL COMMENT '0-100',
    severity_level  VARCHAR(20)  DEFAULT NULL COMMENT 'CRITICAL, HIGH, MEDIUM, LOW',
    discovered_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    ranked_at       DATETIME     DEFAULT NULL,
    INDEX idx_type          (type),
    INDEX idx_severity      (severity_level),
    INDEX idx_source        (source),
    INDEX idx_discovered_at (discovered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample seed data (optional, for testing)
INSERT IGNORE INTO iocs (type, value, source, description, severity_score, severity_level, discovered_at, ranked_at) VALUES
('ip',     '192.168.1.100',             'abuseipdb', 'Abuse confidence: 95%, Country: CN', 95, 'CRITICAL', NOW(), NOW()),
('domain', 'malware-c2.evil.com',       'alienvault', 'C2 domain - Active malware campaign',   88, 'CRITICAL', NOW(), NOW()),
('ip',     '185.220.101.50',            'alienvault', 'Ransomware C2 server',                  82, 'CRITICAL', NOW(), NOW()),
('ip',     '10.0.0.50',                 'abuseipdb', 'Abuse confidence: 87%, Country: RU',    75, 'HIGH',     NOW(), NOW()),
('domain', 'phishing-site.net',         'alienvault', 'Active phishing domain',               70, 'HIGH',     NOW(), NOW()),
('ip',     '45.33.32.156',              'alienvault', 'C2 server - botnet infrastructure',    65, 'HIGH',     NOW(), NOW()),
('ip',     '172.16.0.25',               'abuseipdb', 'Abuse confidence: 92%, VPN exit node', 55, 'MEDIUM',   NOW(), NOW()),
('domain', 'ransomware-payment.onion.ws','alienvault','Ransomware payment portal',            50, 'MEDIUM',   NOW(), NOW()),
('ip',     '104.21.45.67',              'alienvault', 'Phishing host',                        45, 'MEDIUM',   NOW(), NOW()),
('domain', 'chinanet.cn',               'abuseipdb', 'Associated domain for abuse IP',       35, 'LOW',      NOW(), NOW());
