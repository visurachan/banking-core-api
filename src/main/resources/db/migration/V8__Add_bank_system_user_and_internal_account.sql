-- insert bank system user
INSERT INTO users ( email, password, first_name, last_name, role, is_verified, created_at, updated_at)
VALUES (

           'bank@system.internal',
           'N/A',
           'Bank',
           'System',
           'ADMIN',
           true,
           NOW(),
           NOW()
       );

-- insert bank internal account linked to system user
INSERT INTO accounts (id, account_number, account_type, account_status, currency, user_id, created_at, updated_at)
VALUES (
           gen_random_uuid(),
           'BANK-INTERNAL',
           'INTERNAL',
           'ACTIVE',
           'GBP',
           (SELECT id FROM users WHERE email = 'bank@system.internal'),
           NOW(),
           NOW()
       );