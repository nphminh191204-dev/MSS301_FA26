CREATE TABLE t_orders
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(255) DEFAULT NULL,
    sku_code     VARCHAR(255) NOT NULL,
    price        DECIMAL(19, 2) NOT NULL,
    quantity     INT          NOT NULL,
    PRIMARY KEY (id)
);
