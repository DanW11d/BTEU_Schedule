-- Проверка зависимостей перед удалением факультета FKIF (id=1)
-- Факультет FKF (id=4) является дубликатом с тем же названием

-- 1. Проверить, используется ли факультет id=1 в таблице groups
SELECT 
    'Groups using faculty_id=1:' as check_type,
    COUNT(*) as count
FROM groups 
WHERE faculty_id = 1;

-- 2. Проверить, используется ли факультет id=1 в таблице departments
SELECT 
    'Departments using faculty_id=1:' as check_type,
    COUNT(*) as count
FROM departments 
WHERE faculty_id = 1;

-- 3. Показать все группы, связанные с факультетом id=1
SELECT 
    id, code, name, faculty_id, course, education_form
FROM groups 
WHERE faculty_id = 1
LIMIT 10;

-- 4. Показать все кафедры, связанные с факультетом id=1
SELECT 
    id, code, name_ru, faculty_id
FROM departments 
WHERE faculty_id = 1;

-- 5. Если есть зависимости, переназначить их на факультет FKF (id=4)
-- Обновить группы
UPDATE groups 
SET faculty_id = 4 
WHERE faculty_id = 1;

-- Обновить кафедры
UPDATE departments 
SET faculty_id = 4 
WHERE faculty_id = 1;

-- 6. Удалить дубликат факультета
DELETE FROM faculties 
WHERE id = 1;

-- 7. Проверить результат
SELECT 
    id, code, name_ru, description, is_active
FROM faculties 
ORDER BY id;

