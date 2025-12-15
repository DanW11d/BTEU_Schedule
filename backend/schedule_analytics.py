"""
Модуль для аналитики расписания
Вычисляет нагрузку, баланс часов, приоритеты и другие метрики
"""
from typing import List, Dict, Optional
from datetime import datetime, timedelta
from collections import defaultdict

class ScheduleAnalytics:
    """Класс для анализа расписания"""
    
    @staticmethod
    def calculate_weekly_load(lessons: List[Dict]) -> Dict:
        """
        Вычисляет нагрузку на неделю
        
        Returns:
            {
                'total_lessons': int,
                'lessons_by_day': {1: int, 2: int, ...},
                'lessons_by_type': {'lecture': int, 'practice': int, ...},
                'total_hours': float,
                'average_per_day': float
            }
        """
        lessons_by_day = defaultdict(int)
        lessons_by_type = defaultdict(int)
        total_lessons = 0
        
        for lesson in lessons:
            day = lesson.get('day_of_week')
            lesson_type = lesson.get('lesson_type', 'lecture')
            
            if day:
                lessons_by_day[day] += 1
                lessons_by_type[lesson_type] += 1
                total_lessons += 1
        
        # Приблизительный расчет часов (1 пара = 1.5 часа)
        total_hours = total_lessons * 1.5
        average_per_day = total_hours / 6 if lessons_by_day else 0
        
        return {
            'total_lessons': total_lessons,
            'lessons_by_day': dict(lessons_by_day),
            'lessons_by_type': dict(lessons_by_type),
            'total_hours': round(total_hours, 1),
            'average_per_day': round(average_per_day, 1)
        }
    
    @staticmethod
    def calculate_hour_balance(lessons: List[Dict]) -> Dict:
        """
        Вычисляет баланс часов по типам занятий
        
        Returns:
            {
                'lecture_hours': float,
                'practice_hours': float,
                'lab_hours': float,
                'balance_score': float,  # -1 до 1, где 0 = идеальный баланс
                'recommendations': List[str]
            }
        """
        type_hours = defaultdict(float)
        
        for lesson in lessons:
            lesson_type = lesson.get('lesson_type', 'lecture')
            type_hours[lesson_type] += 1.5
        
        lecture_hours = type_hours.get('lecture', 0)
        practice_hours = type_hours.get('practice', 0)
        lab_hours = type_hours.get('lab', 0)
        
        total = lecture_hours + practice_hours + lab_hours
        if total == 0:
            return {
                'lecture_hours': 0,
                'practice_hours': 0,
                'lab_hours': 0,
                'balance_score': 0,
                'recommendations': []
            }
        
        # Идеальное соотношение: 40% лекции, 35% практики, 25% лабы
        ideal_lecture = total * 0.4
        ideal_practice = total * 0.35
        ideal_lab = total * 0.25
        
        # Вычисляем отклонение
        lecture_diff = (lecture_hours - ideal_lecture) / total
        practice_diff = (practice_hours - ideal_practice) / total
        lab_diff = (lab_hours - ideal_lab) / total
        
        # Баланс-скор (чем ближе к 0, тем лучше)
        balance_score = (abs(lecture_diff) + abs(practice_diff) + abs(lab_diff)) / 3
        
        recommendations = []
        if lecture_hours < ideal_lecture * 0.8:
            recommendations.append("Недостаточно лекционных часов")
        if practice_hours < ideal_practice * 0.8:
            recommendations.append("Недостаточно практических занятий")
        if lab_hours < ideal_lab * 0.8:
            recommendations.append("Недостаточно лабораторных работ")
        if balance_score > 0.3:
            recommendations.append("Рекомендуется пересмотреть баланс типов занятий")
        
        return {
            'lecture_hours': round(lecture_hours, 1),
            'practice_hours': round(practice_hours, 1),
            'lab_hours': round(lab_hours, 1),
            'balance_score': round(balance_score, 2),
            'recommendations': recommendations
        }
    
    @staticmethod
    def identify_priorities(lessons: List[Dict], exams: Optional[List[Dict]] = None) -> Dict:
        """
        Определяет приоритеты занятий на основе близости экзаменов и типа занятий
        
        Returns:
            {
                'high_priority': List[Dict],
                'medium_priority': List[Dict],
                'low_priority': List[Dict],
                'upcoming_exams': List[Dict]
            }
        """
        high_priority = []
        medium_priority = []
        low_priority = []
        upcoming_exams = []
        
        # Если есть экзамены, определяем приоритет на основе близости
        exam_subjects = {}
        if exams:
            today = datetime.now().date()
            for exam in exams:
                exam_date_str = exam.get('date')
                if exam_date_str:
                    try:
                        exam_date = datetime.strptime(exam_date_str, '%Y-%m-%d').date()
                        days_until = (exam_date - today).days
                        if 0 <= days_until <= 14:  # В ближайшие 2 недели
                            exam_subjects[exam.get('subject', '').lower()] = days_until
                            upcoming_exams.append({
                                'subject': exam.get('subject'),
                                'date': exam_date_str,
                                'days_until': days_until
                            })
                    except:
                        pass
        
        for lesson in lessons:
            subject = lesson.get('subject', '').lower()
            lesson_type = lesson.get('lesson_type', 'lecture')
            
            # Высокий приоритет: экзамен в ближайшие 7 дней
            if subject in exam_subjects and exam_subjects[subject] <= 7:
                high_priority.append(lesson)
            # Средний приоритет: экзамен в ближайшие 14 дней или практика/лабы
            elif subject in exam_subjects or lesson_type in ['practice', 'lab']:
                medium_priority.append(lesson)
            # Низкий приоритет: остальные
            else:
                low_priority.append(lesson)
        
        return {
            'high_priority': high_priority[:10],  # Ограничиваем количество
            'medium_priority': medium_priority[:10],
            'low_priority': low_priority[:10],
            'upcoming_exams': upcoming_exams
        }
    
    @staticmethod
    def find_next_lesson(lessons: List[Dict], subject_query: str) -> Optional[Dict]:
        """
        Находит следующее занятие по предмету
        
        Args:
            lessons: Список всех занятий
            subject_query: Запрос по предмету (может быть частичным)
        
        Returns:
            Словарь с информацией о следующем занятии или None
        """
        subject_query_lower = subject_query.lower()
        today = datetime.now()
        current_day = today.weekday() + 1  # 1 = Понедельник, 7 = Воскресенье
        
        matching_lessons = []
        for lesson in lessons:
            subject = lesson.get('subject', '').lower()
            if subject_query_lower in subject or subject in subject_query_lower:
                day = lesson.get('day_of_week')
                if day:
                    matching_lessons.append(lesson)
        
        if not matching_lessons:
            return None
        
        # Находим ближайшее занятие
        next_lesson = None
        min_days = 7
        
        for lesson in matching_lessons:
            lesson_day = lesson.get('day_of_week')
            if lesson_day:
                days_until = (lesson_day - current_day) % 7
                if days_until == 0:
                    # Проверяем, не прошло ли уже занятие сегодня
                    # (упрощенная проверка - в реальности нужно учитывать время)
                    days_until = 7
                
                if days_until < min_days:
                    min_days = days_until
                    next_lesson = lesson
        
        if next_lesson:
            day_names = {
                1: 'Понедельник',
                2: 'Вторник',
                3: 'Среда',
                4: 'Четверг',
                5: 'Пятница',
                6: 'Суббота'
            }
            next_lesson['days_until'] = min_days
            next_lesson['day_name'] = day_names.get(next_lesson.get('day_of_week'), '')
        
        return next_lesson
    
    @staticmethod
    def suggest_optimal_schedule(lessons: List[Dict], preferences: Optional[Dict] = None) -> Dict:
        """
        Предлагает оптимальное расписание с учетом предпочтений
        
        Args:
            lessons: Текущее расписание
            preferences: Предпочтения пользователя
                {
                    'preferred_days': [1, 2, 3],  # Предпочитаемые дни
                    'max_lessons_per_day': 5,
                    'preferred_time': 'morning'  # 'morning', 'afternoon', 'evening'
                }
        
        Returns:
            {
                'suggestions': List[str],
                'conflicts': List[str],
                'optimization_score': float
            }
        """
        if not preferences:
            preferences = {
                'preferred_days': [1, 2, 3, 4, 5],
                'max_lessons_per_day': 5,
                'preferred_time': 'morning'
            }
        
        suggestions = []
        conflicts = []
        
        # Анализируем текущее расписание
        lessons_by_day = defaultdict(list)
        for lesson in lessons:
            day = lesson.get('day_of_week')
            if day:
                lessons_by_day[day].append(lesson)
        
        # Проверяем перегрузку дней
        for day, day_lessons in lessons_by_day.items():
            if len(day_lessons) > preferences['max_lessons_per_day']:
                day_names = {1: 'Понедельник', 2: 'Вторник', 3: 'Среда', 
                           4: 'Четверг', 5: 'Пятница', 6: 'Суббота'}
                conflicts.append(
                    f"Перегрузка в {day_names.get(day, 'день')}: "
                    f"{len(day_lessons)} занятий"
                )
                suggestions.append(
                    f"Рекомендуется распределить занятия из {day_names.get(day, 'дня')} "
                    f"на другие дни"
                )
        
        # Проверяем предпочитаемые дни
        preferred_days = set(preferences.get('preferred_days', []))
        current_days = set(lessons_by_day.keys())
        non_preferred = current_days - preferred_days
        
        if non_preferred:
            suggestions.append(
                "Некоторые занятия запланированы на непредпочитаемые дни. "
                "Рассмотрите возможность переноса."
            )
        
        # Вычисляем score оптимизации (0-1, где 1 = идеально)
        max_lessons = max([len(lessons) for lessons in lessons_by_day.values()] + [0])
        load_balance = 1.0 - (max_lessons / (preferences['max_lessons_per_day'] * 2))
        day_preference = len(preferred_days & current_days) / max(len(current_days), 1)
        
        optimization_score = (load_balance + day_preference) / 2
        
        return {
            'suggestions': suggestions,
            'conflicts': conflicts,
            'optimization_score': round(optimization_score, 2)
        }

