INSERT INTO role (id, name)
SELECT 1, 'ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 1);

INSERT INTO role (id, name)
SELECT 2, 'USER'
    WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 2);