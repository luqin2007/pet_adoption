-- AI 匹配系统：配置、特征缓存、Token 用量、用户设置

CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO system_config (config_key, config_value, description) VALUES
('ai.enabled', 'false', '全局 AI 匹配开关'),
('ai.endpoint', '', 'OpenAI 兼容 API 接入点'),
('ai.model', 'gpt-4o-mini', 'AI 模型名'),
('ai.key', '', 'API Key'),
('ai.multimodal', 'false', '是否启用多模态图片分析'),
('ai.daily_token_limit', '1000000', '每日 Token 上限'),
('ai.version', '1', 'AI 版本号，切换模型时递增以触发缓存重建');

CREATE TABLE IF NOT EXISTS pet_feature_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL,
    parent_type VARCHAR(20) NOT NULL COMMENT 'PET 或 LOST_PET',
    model_version VARCHAR(100) NOT NULL COMMENT '生成此特征的 AI 模型版本',
    features TEXT COMMENT 'JSON 结构化特征',
    scraped_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_parent (parent_type, parent_id, model_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_token_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usage_date DATETIME NOT NULL,
    operation VARCHAR(50) NOT NULL COMMENT 'extract_features / match / ...',
    model VARCHAR(100),
    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_token_usage_date ON ai_token_usage(usage_date);

CREATE TABLE IF NOT EXISTS user_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    enable_ai BOOLEAN NOT NULL DEFAULT TRUE,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
