-- Скрипт для удаления кафедр с id от 1 до 10 включительно
-- ВНИМАНИЕ: Это удалит записи из таблицы departments

DELETE FROM departments
WHERE id >= 1 AND id <= 10;

-- Проверка результатов
SELECT 
    COUNT(*) as total_departments,
    MIN(id) as min_id,
    MAX(id) as max_id
FROM departments
WHERE is_active = TRUE;

