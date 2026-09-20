CREATE TABLE IF NOT EXISTS recharge_settings (
    id TINYINT PRIMARY KEY,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder VARCHAR(100) NOT NULL,
    transfer_pattern VARCHAR(150) NOT NULL,
    warning_text TEXT NOT NULL,
    rate_vnd BIGINT NOT NULL,
    rate_gem INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO recharge_settings
    (id, bank_name, account_number, account_holder, transfer_pattern, warning_text, rate_vnd, rate_gem)
VALUES
    (1, 'MBBank', '8386888999888', 'VY NGOC DINH', '{player_name}',
     'Chuyển khoản đúng nội dung, chuyển khoản không đúng nội dung -> admin không giải quyết.', 1000, 1);

CREATE TABLE IF NOT EXISTS recharge_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recharge_package_reward (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id BIGINT NOT NULL,
    reward_type VARCHAR(10) NOT NULL,
    item_template_id INT NOT NULL DEFAULT 0,
    amount INT NOT NULL,
    options_data TEXT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    KEY idx_rpr_package(package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recharge_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    player_name VARCHAR(100) NOT NULL,
    recharge_type VARCHAR(10) NOT NULL,
    cash_amount BIGINT NOT NULL DEFAULT 0,
    diamond_base INT NOT NULL DEFAULT 0,
    diamond_total INT NOT NULL DEFAULT 0,
    package_id BIGINT NULL,
    package_name VARCHAR(150) NULL,
    event_points INT NOT NULL DEFAULT 0,
    mail_id BIGINT NOT NULL,
    reward_snapshot LONGTEXT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'SUCCESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_rh_player(player_id, created_at),
    KEY idx_rh_event(recharge_type, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
