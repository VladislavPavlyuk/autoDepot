-- Replace US city names with European cities (English)
UPDATE orders SET destination = 'Berlin'    WHERE destination = 'New York';
UPDATE orders SET destination = 'Paris'    WHERE destination = 'Los Angeles';
UPDATE orders SET destination = 'Madrid'    WHERE destination = 'Chicago';
UPDATE orders SET destination = 'Rome'     WHERE destination = 'Houston';
UPDATE orders SET destination = 'Amsterdam' WHERE destination = 'Phoenix';
UPDATE orders SET destination = 'Vienna'   WHERE destination = 'Philadelphia';
UPDATE orders SET destination = 'Warsaw'   WHERE destination = 'San Antonio';
UPDATE orders SET destination = 'Brussels' WHERE destination = 'San Diego';

-- Replace driver names with Ukrainian (Latin)
UPDATE drivers SET name = 'Ivan Petrenko'       WHERE name = 'John Smith';
UPDATE drivers SET name = 'Mykhailo Kovalenko'  WHERE name = 'Michael Johnson';
UPDATE drivers SET name = 'Oleksandr Shevchenko' WHERE name = 'David Williams';
UPDATE drivers SET name = 'Andrii Melnyk'       WHERE name = 'Robert Brown';
UPDATE drivers SET name = 'Dmytro Bondarenko'   WHERE name = 'James Davis';
