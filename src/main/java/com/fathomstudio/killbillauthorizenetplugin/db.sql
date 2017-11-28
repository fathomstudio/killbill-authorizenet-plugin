DROP TABLE IF EXISTS `baseCommerce_paymentMethods`;
CREATE TABLE `baseCommerce_paymentMethods` (
  `id`              INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `paymentMethodId` VARCHAR(255) NOT NULL UNIQUE,
  `token`   VARCHAR(255) NOT NULL,
  `type`   VARCHAR(255) NOT NULL,
  INDEX `INDEX_baseCommerce_paymentMethods_ON_paymentMethodId`(`paymentMethodId`)
)
  ENGINE = InnoDB
  CHARACTER SET utf8
  COLLATE utf8_bin;

DROP TABLE IF EXISTS `baseCommerce_credentials`;
CREATE TABLE `baseCommerce_credentials` (
  `id`        INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenantId`  VARCHAR(255) NOT NULL UNIQUE,
  `username` VARCHAR(255),
  `password` VARCHAR(255),
  `key`      VARCHAR(255),
  `test`      BOOLEAN,
  INDEX `INDEX_baseCommerce_credentials_ON_tenantId`(`tenantId`),
  INDEX `INDEX_baseCommerce_credentials_ON_accountId`(`username`)
)
  ENGINE = InnoDB
  CHARACTER SET utf8
  COLLATE utf8_bin;