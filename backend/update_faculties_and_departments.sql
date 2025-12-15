-- Скрипт для обновления факультетов и кафедр в БД
-- Структура согласно сайту университета

-- Сначала обновим существующие факультеты или добавим новые
-- Факультет экономики и управления
INSERT INTO faculties (code, name_ru, name_en, description, is_active)
VALUES ('FEU', 'Факультет экономики и управления', 'Faculty of Economics and Management', 'Факультет экономики и управления', TRUE)
ON CONFLICT (code) DO UPDATE 
SET name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en,
    description = EXCLUDED.description,
    is_active = TRUE;

-- Факультет коммерции и финансов
INSERT INTO faculties (code, name_ru, name_en, description, is_active)
VALUES ('FKF', 'Факультет коммерции и финансов', 'Faculty of Commerce and Finance', 'Факультет коммерции и финансов', TRUE)
ON CONFLICT (code) DO UPDATE 
SET name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en,
    description = EXCLUDED.description,
    is_active = TRUE;

-- Факультет повышения квалификации и переподготовки
INSERT INTO faculties (code, name_ru, name_en, description, is_active)
VALUES ('FPKP', 'Факультет повышения квалификации и переподготовки', 'Faculty of Advanced Training and Retraining', 'Факультет повышения квалификации и переподготовки', TRUE)
ON CONFLICT (code) DO UPDATE 
SET name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en,
    description = EXCLUDED.description,
    is_active = TRUE;

-- Теперь добавим кафедры для каждого факультета
-- Получаем ID факультетов для связи
DO $$
DECLARE
    v_feu_id INTEGER;
    v_fkf_id INTEGER;
    v_fpkp_id INTEGER;
BEGIN
    -- Получаем ID факультетов
    SELECT id INTO v_feu_id FROM faculties WHERE code = 'FEU';
    SELECT id INTO v_fkf_id FROM faculties WHERE code = 'FKF';
    SELECT id INTO v_fpkp_id FROM faculties WHERE code = 'FPKP';

    -- Кафедры Факультета экономики и управления
    INSERT INTO departments (code, name_ru, name_en, faculty_id, description, is_active)
    VALUES 
        ('IVS', 'Кафедра информационно-вычислительных систем', 'Department of Information and Computing Systems', v_feu_id, 'Кафедра информационно-вычислительных систем', TRUE),
        ('PET', 'Кафедра права и экономических теорий', 'Department of Law and Economic Theories', v_feu_id, 'Кафедра права и экономических теорий', TRUE),
        ('MNE', 'Кафедра мировой и национальной экономики', 'Department of World and National Economics', v_feu_id, 'Кафедра мировой и национальной экономики', TRUE),
        ('ET', 'Кафедра экономики торговли', 'Department of Trade Economics', v_feu_id, 'Кафедра экономики торговли', TRUE)
    ON CONFLICT (code) DO UPDATE 
    SET name_ru = EXCLUDED.name_ru,
        name_en = EXCLUDED.name_en,
        faculty_id = EXCLUDED.faculty_id,
        description = EXCLUDED.description,
        is_active = TRUE;

    -- Кафедры Факультета коммерции и финансов
    INSERT INTO departments (code, name_ru, name_en, faculty_id, description, is_active)
    VALUES 
        ('BAF', 'Кафедра бухгалтерского учета и финансов', 'Department of Accounting and Finance', v_fkf_id, 'Кафедра бухгалтерского учета и финансов', TRUE),
        ('GPE', 'Кафедра гуманитарного и физического воспитания', 'Department of Humanities and Physical Education', v_fkf_id, 'Кафедра гуманитарного и физического воспитания', TRUE),
        ('CL', 'Кафедра коммерции и логистики', 'Department of Commerce and Logistics', v_fkf_id, 'Кафедра коммерции и логистики', TRUE),
        ('MKT', 'Кафедра маркетинга', 'Department of Marketing', v_fkf_id, 'Кафедра маркетинга', TRUE),
        ('TD', 'Кафедра товароведения', 'Department of Commodity Science', v_fkf_id, 'Кафедра товароведения', TRUE)
    ON CONFLICT (code) DO UPDATE 
    SET name_ru = EXCLUDED.name_ru,
        name_en = EXCLUDED.name_en,
        faculty_id = EXCLUDED.faculty_id,
        description = EXCLUDED.description,
        is_active = TRUE;

    -- Кафедры Факультета повышения квалификации и переподготовки
    INSERT INTO departments (code, name_ru, name_en, faculty_id, description, is_active)
    VALUES 
        ('EPD', 'Кафедра экономических и правовых дисциплин', 'Department of Economic and Legal Disciplines', v_fpkp_id, 'Кафедра экономических и правовых дисциплин', TRUE)
    ON CONFLICT (code) DO UPDATE 
    SET name_ru = EXCLUDED.name_ru,
        name_en = EXCLUDED.name_en,
        faculty_id = EXCLUDED.faculty_id,
        description = EXCLUDED.description,
        is_active = TRUE;

    RAISE NOTICE 'Факультеты и кафедры успешно обновлены';
END $$;

-- Проверка результатов
SELECT 
    f.code as faculty_code,
    f.name_ru as faculty_name,
    d.code as department_code,
    d.name_ru as department_name
FROM faculties f
LEFT JOIN departments d ON d.faculty_id = f.id
WHERE f.is_active = TRUE
ORDER BY f.code, d.code;

