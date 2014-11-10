//检查是否退款
SELECT * FROM
(SELECT member_id,wealth,serial_number,sub_serial_number,create_time FROM `uc_thirdplat_account_log` WHERE `status` = 'REJECT') a
LEFT JOIN
(SELECT member_id,wealth,serial_number,sub_serial_number,create_time FROM `uc_account_log` WHERE wealth_type = 'REFUND' AND account = 'S2') b
ON a.serial_number = b.serial_number;