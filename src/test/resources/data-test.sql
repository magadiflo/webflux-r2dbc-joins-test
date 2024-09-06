TRUNCATE TABLE departments RESTART IDENTITY CASCADE;
TRUNCATE TABLE employees RESTART IDENTITY CASCADE;

INSERT INTO departments(name)
VALUES('Tecnología'),
('Ventas'),
('Legal'),
('Soporte');

INSERT INTO employees(first_name, last_name, position, is_full_time)
VALUES('Martín', 'Díaz', 'Gerente', true),
('Katherine', 'Fernández', 'Desarrollador', true),
('Vanessa', 'Bello', 'Diseñador', false),
('Melissa', 'Peralta', 'Gerente', true),
('Alexander', 'Villanueva', 'Vendedor', true),
('Lizbeth', 'Gonzales', 'Teacher', true),
('Jorge', 'Gayoso', 'Teacher', true);

INSERT INTO department_managers(department_id, employee_id)
VALUES(1, 1),
(2, 4);

INSERT INTO department_employees(department_id, employee_id)
VALUES(1, 2),
(1,3),
(2,5);