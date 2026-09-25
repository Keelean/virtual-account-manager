-- Align the schema with the renamed entities (virtual_account_* -> entity-named tables).

-- AccountCustomer
alter table virtual_account_customer rename to account_customer;
alter table account_customer add column if not exists partner_config_id int8;
-- invoice payment ref now lives in AccountMeta
update account_customer
set meta = coalesce(meta, '{}'::jsonb) || jsonb_build_object('invoicePaymentRef', invoice_payment_ref)
where invoice_payment_ref is not null;
alter table account_customer drop column if exists invoice_payment_ref;
alter table account_customer drop column if exists payment_received_date;

-- PartnerAccountConfig
alter table virtual_account_partner_config rename to partner_account_config;
alter table partner_account_config alter column meta type jsonb using meta::jsonb;
alter table partner_account_config add column if not exists current_sequence int4 default 0;

-- Transaction
alter table virtual_account_transaction rename to transaction;
alter table transaction add column if not exists accountid varchar;
alter table transaction add column if not exists expected_amount numeric(19, 2);
alter table transaction add column if not exists validation_status varchar;
alter table transaction add column if not exists txn_status varchar;

-- AccountActivity
alter table virtual_account_activity rename to account_activity;

-- AccountPool: sequence generation moved from the removed generator service into AccountPool
alter table virtual_account_pool rename to account_pool;
alter table account_pool drop column if exists total_usable_digits;
alter table account_pool drop column if exists current_prefix;
alter table account_pool drop column if exists maximum_range;
alter table account_pool add column if not exists start_prefix_count int4 default 0;
alter table account_pool add column if not exists allocation_count int4 default 0;
alter table account_pool add column if not exists excluded_prefix_start jsonb;
-- V1 seeded pool rows with explicit ids without advancing the sequence
select setval(pg_get_serial_sequence('account_pool', 'id'), coalesce(max(id), 1)) from account_pool;

-- Account hierarchy (StaticAccount / DynamicAccount, single table)
create table if not exists customer_account
(
    id bigserial primary key,
    created_by varchar,
    last_modified_by varchar,
    created_date timestamp,
    last_modified_date timestamp,
    opt_lock int8,
    account_type varchar(31) not null,
    account_id varchar,
    account_mode varchar,
    account_name varchar(50),
    amount int8,
    invoice_ref varchar,
    max_deposit int8,
    min_deposit int8,
    partner_id varchar,
    reference_id varchar,
    timeout_in_mins int4,
    txn_status varchar,
    wait_start_time int4
);
