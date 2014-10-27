drop database droolsbook;
create database droolsbook character set utf8;
grant all privileges on droolsbook.* to 'droolsbook'@'localhost' identified by 'droolsbook' with grant option;
flush privileges;

CREATE TABLE `droolsbook`.`customer` (
`customer_id` bigint(20) NOT NULL,
`first_name` varchar(255) NOT NULL,
`last_name` varchar(255) NOT NULL,
`email` varchar(255) NOT NULL,
PRIMARY KEY (`customer_id`)
);

CREATE TABLE `droolsbook`.`address` (
`address_id` bigint(20) NOT NULL default '0',
`parent_id` bigint(20) NOT NULL,
`street` varchar(255) NOT NULL,
`area` varchar(255) NOT NULL,
`town` varchar(255) NOT NULL,
`country` varchar(255) NOT NULL,
PRIMARY KEY (`address_id`)
);

CREATE TABLE `droolsbook`.`account` (
`account_id` bigint(20) NOT NULL,
`name` varchar(255) NOT NULL,
`currency` varchar(100) NOT NULL,
`balance` varchar(255) NOT NULL,
`customer_id` bigint(20) NOT NULL,
PRIMARY KEY (`account_id`)
);