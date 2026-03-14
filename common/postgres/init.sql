create database shop_db;
create database client_db;

create user shop_user with password 'shop_pass';
create user client_user with password 'client_pass';

grant all privileges on database shop_db to shop_user;
grant all privileges on database client_db to client_user;

\connect shop_db

grant usage on schema public to shop_user;
grant create on schema public to shop_user;

create table if not exists public.product (
    product_id text primary key,
    name text,
    description text,
    price_amount numeric,
    price_currency text,
    category text,
    brand text,
    stock_available integer,
    stock_reserved integer,
    sku text,
    tags varchar[],
    specifications_weight text,
    specifications_dimensions text,
    specifications_battery_life text,
    specifications_water_resistance text,
    store_id text,
    "index" text,
    created_at timestamp,
    updated_at timestamp
);

alter table public.product owner to shop_user;
