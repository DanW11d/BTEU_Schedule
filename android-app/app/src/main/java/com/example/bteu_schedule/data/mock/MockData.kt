package com.example.bteu_schedule.data.mock

import com.example.bteu_schedule.domain.models.FacultyUi
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi

/**
 * Мок-функции для тестирования и разработки
 */

fun mockFaculties(): List<FacultyUi> {
    return listOf(
        FacultyUi(1, "FEU", "Факультет экономики и управления", "description"),
        FacultyUi(2, "FKIF", "Факультет коммерции и финансов", "description"),
    )
}

fun mockLessonsForDay(groupCode: String, dayIndex: Int, isOddWeek: Boolean): List<LessonUi> {
    val base = mutableListOf<LessonUi>()
    
    when (groupCode) {
        "П-1" -> {
            if (dayIndex == 0) { // Понедельник
                base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Правоведение", "доц. Иванов И.И.", "3-10", "lecture", "both")
            }
        }
        "И-1" -> {
            if (dayIndex == 1) { // Вторник
                base += LessonUi(2, 1, dayIndex + 1, "09:00-10:35", "Программирование", "доц. Петров П.П.", "4-15", "practice", "both")
            }
        }
        "Е-1" -> {
            // Группа Е-1 (Менеджмент)
            when (dayIndex) {
                0 -> { // Понедельник
                    base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Основы менеджмента", "доц. Смирнов С.С.", "3-12", "lecture", "both")
                    base += LessonUi(2, 2, dayIndex + 1, "10:50-12:25", "Управление персоналом", "доц. Иванова И.И.", "3-13", "practice", "both")
                }
                1 -> { // Вторник
                    if (isOddWeek) {
                        base += LessonUi(3, 1, dayIndex + 1, "09:00-10:35", "Стратегический менеджмент", "доц. Петрова П.П.", "3-14", "lecture", "odd")
                    } else {
                        base += LessonUi(3, 1, dayIndex + 1, "09:00-10:35", "Операционный менеджмент", "доц. Петрова П.П.", "3-14", "lecture", "even")
                    }
                    base += LessonUi(4, 2, dayIndex + 1, "10:50-12:25", "Практикум по менеджменту", "доц. Петрова П.П.", "3-15", "practice", "both")
                }
                2 -> { // Среда
                    base += LessonUi(5, 1, dayIndex + 1, "09:00-10:35", "Экономика организации", "доц. Соколова С.С.", "3-16", "lecture", "both")
                }
                3 -> { // Четверг
                    base += LessonUi(6, 1, dayIndex + 1, "09:00-10:35", "Маркетинг", "доц. Волкова В.В.", "3-17", "lecture", "both")
                    base += LessonUi(7, 2, dayIndex + 1, "10:50-12:25", "Деловые коммуникации", "доц. Волкова В.В.", "3-18", "practice", "both")
                }
            }
        }
        "Л-1" -> {
            // Группа Л-1 (Логистика)
            when (dayIndex) {
                0 -> { // Понедельник
                    base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Основы логистики", "доц. Лебедева Л.Л.", "5-31", "lecture", "both")
                    base += LessonUi(2, 2, dayIndex + 1, "10:50-12:25", "Транспортная логистика", "доц. Лебедева Л.Л.", "5-32", "practice", "both")
                }
                1 -> { // Вторник
                    base += LessonUi(3, 1, dayIndex + 1, "09:00-10:35", "Складская логистика", "доц. Новиков Н.Н.", "5-33", "lecture", "both")
                }
                2 -> { // Среда
                    if (isOddWeek) {
                        base += LessonUi(4, 1, dayIndex + 1, "09:00-10:35", "Закупочная логистика", "доц. Орлов О.О.", "5-34", "lecture", "odd")
                    } else {
                        base += LessonUi(4, 1, dayIndex + 1, "09:00-10:35", "Распределительная логистика", "доц. Орлов О.О.", "5-34", "lecture", "even")
                    }
                    base += LessonUi(5, 2, dayIndex + 1, "10:50-12:25", "Логистические системы", "доц. Орлов О.О.", "5-35", "practice", "both")
                }
                3 -> { // Четверг
                    base += LessonUi(6, 1, dayIndex + 1, "09:00-10:35", "Экономика логистики", "доц. Федорова Ф.Ф.", "5-36", "lecture", "both")
                }
            }
        }
        "Б-4" -> {
            // Группа Б-4 (Бухгалтерский учет)
            when (dayIndex) {
                0 -> { // Понедельник
                    if (isOddWeek) {
                        base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Бухгалтерский учет в производственных отраслях", "доц. Тропкова Е.Г.", "3-11", "lecture", "odd")
                    } else {
                        base += LessonUi(2, 1, dayIndex + 1, "09:00-10:35", "Международные стандарты аудита", "доц. Томалева Е.Г.", "3-11", "practice", "even")
                        base += LessonUi(3, 2, dayIndex + 1, "10:50-12:25", "Внутренний аудит в потребительской кооперации", "доц. Томалева Е.Г.", "5-41", "lecture", "both")
                    }
                }
                1 -> { // Вторник
                    base += LessonUi(4, 1, dayIndex + 1, "09:00-10:35", "Финансовый учет", "доц. Романова Р.Р.", "3-19", "lecture", "both")
                    base += LessonUi(5, 2, dayIndex + 1, "10:50-12:25", "Управленческий учет", "доц. Романова Р.Р.", "3-20", "practice", "both")
                }
                2 -> { // Среда
                    base += LessonUi(6, 1, dayIndex + 1, "09:00-10:35", "Налоговый учет", "доц. Козлова К.К.", "3-21", "lecture", "both")
                }
                3 -> { // Четверг
                    base += LessonUi(7, 1, dayIndex + 1, "09:00-10:35", "Аудит", "доц. Морозова М.М.", "3-22", "lecture", "both")
                    base += LessonUi(8, 2, dayIndex + 1, "10:50-12:25", "Практикум по аудиту", "доц. Морозова М.М.", "3-23", "practice", "both")
                }
            }
        }
        "Ф-1" -> {
            // Группа Ф-1 (Финансы)
            when (dayIndex) {
                0 -> { // Понедельник
                    base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Финансы организаций", "доц. Козлова К.К.", "5-21", "lecture", "both")
                    base += LessonUi(2, 2, dayIndex + 1, "10:50-12:25", "Финансовый менеджмент", "доц. Козлова К.К.", "5-22", "practice", "both")
                }
                1 -> { // Вторник
                    base += LessonUi(3, 1, dayIndex + 1, "09:00-10:35", "Инвестиционный анализ", "доц. Соколова С.С.", "5-23", "lecture", "both")
                }
                2 -> { // Среда
                    if (isOddWeek) {
                        base += LessonUi(4, 1, dayIndex + 1, "09:00-10:35", "Банковское дело", "доц. Федорова Ф.Ф.", "5-24", "lecture", "odd")
                    } else {
                        base += LessonUi(4, 1, dayIndex + 1, "09:00-10:35", "Страхование", "доц. Федорова Ф.Ф.", "5-24", "lecture", "even")
                    }
                    base += LessonUi(5, 2, dayIndex + 1, "10:50-12:25", "Финансовые рынки", "доц. Федорова Ф.Ф.", "5-25", "practice", "both")
                }
                3 -> { // Четверг
                    base += LessonUi(6, 1, dayIndex + 1, "09:00-10:35", "Деньги, кредит, банки", "доц. Романова Р.Р.", "5-26", "lecture", "both")
                }
            }
        }
        "S-4" -> {
            // Данные для группы S-4 (Инженеры-программисты)
            when (dayIndex) {
                0 -> { // Понедельник
                    if (isOddWeek) {
                        base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Программирование", "доц. Сидоров С.С.", "4-20", "lecture", "odd")
                        base += LessonUi(2, 2, dayIndex + 1, "10:50-12:25", "Базы данных", "доц. Козлов К.К.", "4-21", "practice", "odd")
                    } else {
                        base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Веб-разработка", "доц. Морозов М.М.", "4-22", "lecture", "even")
                        base += LessonUi(2, 2, dayIndex + 1, "10:50-12:25", "Мобильная разработка", "доц. Волков В.В.", "4-23", "practice", "even")
                    }
                }
                1 -> { // Вторник
                    base += LessonUi(3, 1, dayIndex + 1, "09:00-10:35", "Алгоритмы и структуры данных", "доц. Новиков Н.Н.", "4-24", "lecture", "both")
                    base += LessonUi(4, 2, dayIndex + 1, "10:50-12:25", "Проектирование ПО", "доц. Орлов О.О.", "4-25", "practice", "both")
                }
                2 -> { // Среда
                    if (isOddWeek) {
                        base += LessonUi(5, 1, dayIndex + 1, "09:00-10:35", "Системное программирование", "доц. Петров П.П.", "4-26", "lecture", "odd")
                    } else {
                        base += LessonUi(5, 1, dayIndex + 1, "09:00-10:35", "Компьютерные сети", "доц. Соколов С.С.", "4-27", "lecture", "even")
                    }
                }
                3 -> { // Четверг
                    base += LessonUi(6, 1, dayIndex + 1, "09:00-10:35", "Машинное обучение", "доц. Лебедев Л.Л.", "4-28", "lecture", "both")
                    base += LessonUi(7, 2, dayIndex + 1, "10:50-12:25", "Лабораторная работа", "доц. Лебедев Л.Л.", "4-29", "lab", "both")
                }
                4 -> { // Пятница
                    if (isOddWeek) {
                        base += LessonUi(8, 1, dayIndex + 1, "09:00-10:35", "Кибербезопасность", "доц. Федоров Ф.Ф.", "4-30", "lecture", "odd")
                    }
                }
            }
        }
        // Заочные группы (меньше занятий, обычно в выходные или вечерние часы)
        "П-11з" -> {
            if (dayIndex == 0 || dayIndex == 5) { // Понедельник или Суббота
                base += LessonUi(1, 1, dayIndex + 1, "09:00-12:25", "Правоведение (заочное)", "доц. Иванов И.И.", "3-10", "lecture", "both")
                base += LessonUi(2, 2, dayIndex + 1, "13:00-16:35", "Экономическое право (заочное)", "доц. Иванов И.И.", "3-10", "practice", "both")
            }
        }
        "Ес-11з" -> {
            if (dayIndex == 0 || dayIndex == 5) { // Понедельник или Суббота
                base += LessonUi(1, 1, dayIndex + 1, "09:00-12:25", "Основы менеджмента (заочное)", "доц. Смирнов С.С.", "3-12", "lecture", "both")
                base += LessonUi(2, 2, dayIndex + 1, "13:00-16:35", "Управление персоналом (заочное)", "доц. Иванова И.И.", "3-13", "practice", "both")
            }
        }
        "Бс-11з" -> {
            if (dayIndex == 0 || dayIndex == 5) { // Понедельник или Суббота
                base += LessonUi(1, 1, dayIndex + 1, "09:00-12:25", "Бухгалтерский учет (заочное)", "доц. Тропкова Е.Г.", "3-11", "lecture", "both")
                base += LessonUi(2, 2, dayIndex + 1, "13:00-16:35", "Финансовый учет (заочное)", "доц. Романова Р.Р.", "3-19", "practice", "both")
            }
        }
        "Лс-11" -> {
            if (dayIndex == 0 || dayIndex == 5) { // Понедельник или Суббота
                base += LessonUi(1, 1, dayIndex + 1, "09:00-12:25", "Основы логистики (заочное)", "доц. Лебедева Л.Л.", "5-31", "lecture", "both")
                base += LessonUi(2, 2, dayIndex + 1, "13:00-16:35", "Транспортная логистика (заочное)", "доц. Лебедева Л.Л.", "5-32", "practice", "both")
            }
        }
        else -> {
            // Default mock data для групп, у которых нет специального расписания
            if (dayIndex == 0) {
                if (isOddWeek) {
                    base += LessonUi(1, 1, dayIndex + 1, "09:00-10:35", "Основной предмет", "доц. Преподаватель", "3-10", "lecture", "odd")
                } else {
                    base += LessonUi(2, 1, dayIndex + 1, "09:00-10:35", "Основной предмет", "доц. Преподаватель", "3-10", "practice", "even")
                }
            }
        }
    }
    return base
}

fun mockGroupsFor(
    facultyCode: String,
    educationFormCode: String
): List<GroupUi> {
    return when (facultyCode to educationFormCode) {
        "FEU" to "full_time" -> listOf(
            GroupUi("П-1", "Группа П-1", 1, "Экономическое право", "FEU", "full_time"),
            GroupUi("И-1", "Группа И-1", 1, "Информатика/IT", "FEU", "full_time"),
            GroupUi("Е-1", "Группа Е-1", 1, "Менеджмент", "FEU", "full_time")
        )

        "FEU" to "part_time" -> listOf(
            GroupUi("П-11з", "Группа П-11з", 1, "Экономическое право", "FEU", "part_time"),
            GroupUi("Ес-11з", "Группа Ес-11з", 1, "Менеджмент", "FEU", "part_time")
        )

        "FKIF" to "full_time" -> listOf(
            GroupUi("Л-1", "Группа Л-1", 1, "Логистика", "FKIF", "full_time"),
            GroupUi("Б-4", "Группа Б-4", 4, "Бухгалтерский учет", "FKIF", "full_time"),
            GroupUi("Ф-1", "Группа Ф-1", 1, "Финансы", "FKIF", "full_time"),
            GroupUi("S-4", "Инженеры-программисты", 4, "Инженеры-программисты", "FKIF", "full_time")
        )

        "FKIF" to "part_time" -> listOf(
            GroupUi("Бс-11з", "Группа Бс-11з", 1, "Бухгалтерский учет", "FKIF", "part_time"),
            GroupUi("Лс-11", "Группа Лс-11", 1, "Логистика", "FKIF", "part_time")
        )

        else -> emptyList()
    }
}
