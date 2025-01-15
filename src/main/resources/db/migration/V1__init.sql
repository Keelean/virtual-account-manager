create table if not exists virtual_account_customer
(
    id bigserial primary key,
    created_by varchar,
    last_modified_by varchar,
    created_date timestamp,
    last_modified_date timestamp,
    opt_lock int8,
    account_id varchar,
    status varchar,
    partner_id varchar,
    reference_id varchar,
    expiry_date timestamp,
    payment_received_date timestamp,
    invoice_payment_ref varchar,
    mode varchar,
    meta jsonb
);

ALTER TABLE virtual_account_customer ADD CONSTRAINT unique_constraint_reference_and_account_id UNIQUE(reference_id, account_id);
--ALTER TABLE virtual_account_customer ADD CONSTRAINT unique_constraint_account_id UNIQUE(account_id);

create table if not exists virtual_account_partner_config
(
    id bigserial primary key,
    created_by varchar,
    last_modified_by varchar,
    created_date timestamp,
    last_modified_date timestamp,
    opt_lock int8,
    capacity varchar,
    partner_id varchar,
    code varchar,
    prefix varchar,
    status varchar,
    meta json
);

create table if not exists virtual_account_transaction
(
    id bigserial primary key,
    created_by varchar,
    last_modified_by varchar,
    created_date timestamp,
    last_modified_date timestamp,
    opt_lock int8,
    payment_received_date timestamp,
    customer_id int8,
    transaction_id varchar,
    actual_amount decimal
);

create table if not exists virtual_account_activity
(
    id bigserial primary key,
    created_by varchar,
    last_modified_by varchar,
    created_date timestamp,
    last_modified_date timestamp,
    opt_lock int8,
    account_id varchar
);

create table if not exists virtual_account_pool
(
    id bigserial primary key,
    created_by varchar,
    last_modified_by varchar,
    created_date timestamp,
    last_modified_date timestamp,
    opt_lock int8,
    capacity varchar,
    total_usable_digits int8,
    prefix_series int8,
    prefix_end_series int8,
    current_prefix int8,
    start_prefix int8,
    current_sequence int8,
    maximum_range int8,
    state varchar
);


insert into virtual_account_pool values (1, 'System', 'System', now(), now(),0,'THOUSAND_10' ,4,520001,529999,520001,520001,0,9998,'OPEN');
insert into virtual_account_pool values (2, 'System', 'System', now(), now(),0,'THOUSAND_100',5,51001 ,51999 ,51001 ,51001 ,0,998 ,'OPEN');
insert into virtual_account_pool values (3, 'System', 'System', now(), now(),0,'MILLION_1'   ,6,5001  ,5099  ,5001  ,5001  ,0,98  ,'OPEN');
insert into virtual_account_pool values (4, 'System', 'System', now(), now(),0,'MILLION_10'  ,7,541   ,549   ,541   ,541   ,0,8   ,'OPEN');
insert into virtual_account_pool values (5, 'System', 'System', now(), now(),0,'MILLION_100' ,8,55    ,55    ,55    ,55    ,0,0   ,'OPEN');

