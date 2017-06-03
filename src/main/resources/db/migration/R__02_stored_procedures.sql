DROP PROCEDURE IF EXISTS `sp_get_user_attributes`;
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_get_user_attributes`(p_username varchar(255))
  BEGIN
    SELECT 'email' AS 'key', email AS 'value' FROM users WHERE username=p_username
    UNION SELECT 'firstname' AS 'key', firstname AS 'value', 'givenName' AS 'key2', firstname AS 'value2' FROM users WHERE username=p_username
    UNION SELECT 'lastname' AS 'key', lastname AS 'value', 'sn' AS 'key2', lastname AS 'value2' FROM users WHERE username=p_username
    UNION SELECT 'userid' AS 'key', cast(userid AS char) AS 'value', 'id' AS 'key2', cast(userid AS char) AS 'value2' FROM users WHERE username=p_username
    UNION SELECT 'authority' AS 'key', group_concat(a.role_id) AS 'value' FROM user_role a, users u WHERE a.user_id=u.userid and u.username=p_username
    UNION SELECT 'role' AS 'key', a.role_id AS 'value' FROM user_role a JOIN users u ON a.user_id=u.userid WHERE u.username=p_username
    UNION SELECT p.property AS 'key', p.value FROM users u LEFT OUTER JOIN `profiles` p on u.userid=p.userid WHERE u.username=p_username;
  end
//
DELIMITER ;


DROP PROCEDURE IF EXISTS `sp_get_user_authorities`;
DELIMITER //
create procedure sp_get_user_authorities()
  BEGIN
    SELECT username, group_concat(ur.role_id) AS 'authorities' FROM users u, user_role ur WHERE u.userid=ur.user_id group by username;
  end
//
DELIMITER ;


DROP PROCEDURE IF EXISTS `sp_create_user`;
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_create_user`(
  `email`              varchar(255),
  `firstname`          varchar(255),
  `lastname`           varchar(255),
  `password`           varchar(255),
  `city`               varchar(255),
  `organisation`       varchar(255),
  `primaryUserType`    varchar(255),
  `secondaryUserType`  varchar(255),
  `ausstate`           varchar(255),
  `telephone`          varchar(255))
  BEGIN
    DECLARE userid int(11);
    INSERT INTO `emmet`.`users` (`username`, `firstname`, `lastname`, `email`, `activated`, `locked`) VALUES (email, firstname, lastname, email, '1', '0');
    SET userid = LAST_INSERT_ID();
    INSERT INTO `emmet`.`passwords` (`userid`, `password`, `status`, `type`) VALUES (userid, password, 'CURRENT', 'bcrypt');
    INSERT INTO `emmet`.`user_role` (`user_id`, `role_id`) VALUES (userid, 'ROLE_USER');
    INSERT INTO `emmet`.`profiles` (`userid`, `property`, `value`) VALUES (userid, 'city',              city);
    INSERT INTO `emmet`.`profiles` (`userid`, `property`, `value`) VALUES (userid, 'organisation',      organisation);
    INSERT INTO `emmet`.`profiles` (`userid`, `property`, `value`) VALUES (userid, 'primaryUserType',   primaryUserType);
    INSERT INTO `emmet`.`profiles` (`userid`, `property`, `value`) VALUES (userid, 'secondaryUserType', secondaryUserType);
    INSERT INTO `emmet`.`profiles` (`userid`, `property`, `value`) VALUES (userid, 'state',             ausstate);
    INSERT INTO `emmet`.`profiles` (`userid`, `property`, `value`) VALUES (userid, 'telephone',         telephone);
  END
//
DELIMITER ;
