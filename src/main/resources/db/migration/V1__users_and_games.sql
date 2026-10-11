-- USERS
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,

    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE games
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_x_id BIGINT      NOT NULL,
    player_o_id BIGINT      NOT NULL,
    status      VARCHAR(50) NOT NULL,
    outcome     VARCHAR(50),
    winner_id   BIGINT,
    end_reason  VARCHAR(50),

    started_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at    TIMESTAMP   NULL,

    CONSTRAINT fk_games_player_x FOREIGN KEY (player_x_id) REFERENCES users (id),
    CONSTRAINT fk_games_player_o FOREIGN KEY (player_o_id) REFERENCES users (id),
    CONSTRAINT fk_games_winner   FOREIGN KEY (winner_id)   REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
