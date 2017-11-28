DROP TABLE IF EXISTS `authorizeNet_paymentMethods`;
CREATE TABLE `authorizeNet_paymentMethods` (
  `id`              INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `paymentMethodId` VARCHAR(255) NOT NULL UNIQUE,
  `customerProfileId`   VARCHAR(255) NOT NULL,
  `type`   VARCHAR(255) NOT NULL,
  INDEX `INDEX_authorizeNet_paymentMethods_ON_paymentMethodId`(`paymentMethodId`)
)
  ENGINE = InnoDB
  CHARACTER SET utf8
  COLLATE utf8_bin;

DROP TABLE IF EXISTS `authorizeNet_credentials`;
CREATE TABLE `authorizeNet_credentials` (
  `id`        INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenantId`  VARCHAR(255) NOT NULL UNIQUE,
  `loginId` VARCHAR(255),
  `transactionKey` VARCHAR(255),
  `test`      BOOLEAN,
  INDEX `INDEX_authorizeNet_credentials_ON_tenantId`(`tenantId`),
  INDEX `INDEX_authorizeNet_credentials_ON_accountId`(`loginId`)
)
  ENGINE = InnoDB
  CHARACTER SET utf8
  COLLATE utf8_bin;