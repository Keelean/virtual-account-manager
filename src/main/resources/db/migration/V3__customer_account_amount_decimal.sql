-- Account.amount is a BigDecimal so amounts keep their decimal places
alter table customer_account alter column amount type numeric(19, 2);
