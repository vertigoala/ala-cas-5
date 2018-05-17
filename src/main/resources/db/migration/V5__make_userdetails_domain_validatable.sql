/* update passwords to nullable expiries and remove all invalid expiries */
ALTER TABLE `passwords`
MODIFY COLUMN expiry TIMESTAMP NULL DEFAULT NULL;

UPDATE `passwords` SET expiry = null WHERE expiry = 0 or expiry = '2038-01-01 00:00:00';

ALTER TABLE authorities MODIFY userid BIGINT(20);
ALTER TABLE identities MODIFY userid BIGINT(20);
ALTER TABLE passwords MODIFY userid BIGINT(20);
ALTER TABLE profiles MODIFY userid BIGINT(20);
ALTER TABLE user_details MODIFY userid BIGINT(20);
ALTER TABLE users MODIFY userid BIGINT(20);
