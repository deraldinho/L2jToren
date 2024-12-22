CREATE TABLE vip_system (
    char_id INT UNSIGNED NOT NULL PRIMARY KEY, -- ID do personagem, chave primária
    vip_level INT UNSIGNED NOT NULL DEFAULT 0, -- Nível VIP com valor padrão 0
    vip_exp BIGINT UNSIGNED NOT NULL DEFAULT 0, -- Experiência VIP com valor padrão 0
    vip_time BIGINT NOT NULL DEFAULT 0, -- Tempo VIP (em segundos) com valor padrão 0
    CONSTRAINT fk_char FOREIGN KEY (char_id) REFERENCES characters(obj_id) -- Chave estrangeira referenciando a tabela characters
);
