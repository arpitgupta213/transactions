DELETE FROM TRANSACTION where TRANSACTION_ID=1;
DELETE FROM CUSTOMER;
DELETE FROM PRODUCT;

INSERT INTO CUSTOMER (CUSTOMER_ID, FIRST_NAME, LAST_NAME, EMAIL, LOCATION)
VALUES
    (10001, 'Tony', 'Stark', 'tony.stark@gmail.com', 'Australia'),
    (10002, 'Bruce', 'Banner', 'bruce.banner@gmail.com', 'US'),
    (10003, 'Steve', 'Rogers', 'steve.rogers@hotmail.com', 'Australia'),
    (10004, 'Wanda', 'Maximoff', 'wanda.maximoff@gmail.com', 'US'),
    (10005, 'Natasha', 'Romanoff', 'natasha.romanoff@gmail.com', 'Canada');

INSERT INTO PRODUCT (PRODUCT_CODE, COST, STATUS)
VALUES
    ('PRODUCT_001', 50, 'Active'),
    ('PRODUCT_002', 100, 'Inactive'),
    ('PRODUCT_003', 200, 'Active'),
    ('PRODUCT_004', 10, 'Inactive'),
    ('PRODUCT_005', 500, 'Active');

INSERT INTO TRANSACTION (TRANSACTION_ID, CUSTOMER_ID, TRANSACTION_TIME, QUANTITY, PRODUCT_CODE)
VALUES (100001, 10001, now(), 1, 'PRODUCT_001');