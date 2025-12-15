"""
Модуль синхронизации расписания из БД АРМА PostgreSQL
"""

import logging
from datetime import date, datetime, timedelta
from typing import List, Dict, Any, Optional
from db_connection import get_connector, ARMAPostgreSQLConnector

logger = logging.getLogger(__name__)


class ScheduleSync:
    """
    Класс для синхронизации расписания из БД АРМА
    """
    
    def __init__(self, arma_connector: ARMAPostgreSQLConnector):
        """
        Инициализация синхронизатора
        
        Args:
            arma_connector: Коннектор к БД АРМА
        """
        self.arma = arma_connector
    
    def sync_references(self):
        """
        Синхронизация справочников
        """
        logger.info("Начало синхронизации справочников...")
        
        # Синхронизация групп
        self._sync_groups()
        
        # Синхронизация предметов
        self._sync_subjects()
        
        # Синхронизация преподавателей
        self._sync_teachers()
        
        # Синхронизация аудиторий
        self._sync_audiences()
        
        # Синхронизация видов занятий
        self._sync_lesson_types()
        
        # Синхронизация факультетов
        self._sync_faculties()
        
        logger.info("Синхронизация справочников завершена")
    
    def _sync_groups(self):
        """Синхронизация групп из таблицы SGRUP"""
        try:
            query = """
                SELECT 
                    okgrup as code,
                    qkfak as faculty_id,
                    qkolstud as student_count,
                    qnsemest as semester,
                    qgod as academic_year,
                    qsp as specialty_code,
                    qspcl as specialization_code
                FROM sgrupp
                WHERE qgod = (SELECT MAX(qgod) FROM sgrupp)
            """
            
            groups = self.arma.execute_query(query)
            logger.info(f"Найдено групп: {len(groups)}")
            
            # Здесь должна быть логика сохранения в вашу БД
            # Например:
            # for group in groups:
            #     save_group_to_api_db(group)
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации групп: {e}")
    
    def _sync_subjects(self):
        """Синхронизация предметов из таблицы SPRED"""
        try:
            query = """
                SELECT 
                    qkod as id,
                    qkrnam as short_name,
                    qpnam as full_name,
                    qkaf as department_id
                FROM spred
            """
            
            subjects = self.arma.execute_query(query)
            logger.info(f"Найдено предметов: {len(subjects)}")
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации предметов: {e}")
    
    def _sync_teachers(self):
        """Синхронизация преподавателей из таблицы SPREP"""
        try:
            query = """
                SELECT 
                    qtnprep as id,
                    qfiopr as full_name,
                    qprfpr as position,
                    qkaf as department_id,
                    qus as academic_degree,
                    quz as academic_title
                FROM sprep
            """
            
            teachers = self.arma.execute_query(query)
            logger.info(f"Найдено преподавателей: {len(teachers)}")
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации преподавателей: {e}")
    
    def _sync_audiences(self):
        """Синхронизация аудиторий из таблицы SAUDIT"""
        try:
            query = """
                SELECT 
                    qkaud as id,
                    qiaud as name,
                    onkorp as building_number,
                    qkolmest as capacity,
                    qpk as is_computer_lab
                FROM saudit
            """
            
            audiences = self.arma.execute_query(query)
            logger.info(f"Найдено аудиторий: {len(audiences)}")
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации аудиторий: {e}")
    
    def _sync_lesson_types(self):
        """Синхронизация видов занятий из таблицы FVZAN"""
        try:
            query = """
                SELECT 
                    okvzan as id,
                    qivzan as name
                FROM fvzan
            """
            
            lesson_types = self.arma.execute_query(query)
            logger.info(f"Найдено видов занятий: {len(lesson_types)}")
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации видов занятий: {e}")
    
    def _sync_faculties(self):
        """Синхронизация факультетов из таблицы SFAK"""
        try:
            query = """
                SELECT 
                    qkod as id,
                    oname as name,
                    qkotd as department_code
                FROM sfak
            """
            
            faculties = self.arma.execute_query(query)
            logger.info(f"Найдено факультетов: {len(faculties)}")
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации факультетов: {e}")
    
    def sync_schedule_full_time(
        self,
        academic_year: str,
        semester: int,
        start_date: Optional[date] = None,
        end_date: Optional[date] = None
    ) -> List[Dict[str, Any]]:
        """
        Синхронизация расписания для дневного обучения из TMP_GRAF
        
        Args:
            academic_year: Учебный год (например, "2023-2024")
            semester: Номер семестра (1 или 2)
            start_date: Начальная дата (опционально)
            end_date: Конечная дата (опционально)
        
        Returns:
            Список занятий
        """
        logger.info(f"Синхронизация расписания дневного обучения: {academic_year}, семестр {semester}")
        
        try:
            # Базовый запрос
            query = """
                SELECT 
                    qdate as lesson_date,
                    qnurok as lesson_number,
                    qkgrup as group_code,
                    qkpred as subject_id,
                    qkvzan as lesson_type_id,
                    qkprep1 as teacher1_id,
                    qkprep2 as teacher2_id,
                    qkprep3 as teacher3_id,
                    qkaud1 as audience1_id,
                    qkaud2 as audience2_id,
                    qkaud3 as audience3_id,
                    qnpot as stream_number
                FROM tmp_graf
                WHERE 1=1
            """
            
            params = []
            
            # Фильтр по датам
            if start_date:
                query += " AND qdate >= %s"
                params.append(start_date)
            
            if end_date:
                query += " AND qdate <= %s"
                params.append(end_date)
            
            query += " ORDER BY qdate, qnurok"
            
            lessons = self.arma.execute_query(query, tuple(params) if params else None)
            
            # Добавить время начала/окончания по номеру урока
            for lesson in lessons:
                lesson['time_start'], lesson['time_end'] = self._get_lesson_time(lesson['lesson_number'])
                lesson['study_form'] = 'full_time'
                lesson['academic_year'] = academic_year
                lesson['semester'] = semester
            
            logger.info(f"Найдено занятий: {len(lessons)}")
            return lessons
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации расписания дневного обучения: {e}")
            return []
    
    def sync_schedule_extramural(
        self,
        academic_year: str,
        semester: int,
        start_date: Optional[date] = None,
        end_date: Optional[date] = None
    ) -> List[Dict[str, Any]]:
        """
        Синхронизация расписания для заочного обучения из FRASPZ/FGRAFIKZ
        
        Args:
            academic_year: Учебный год
            semester: Номер семестра
            start_date: Начальная дата
            end_date: Конечная дата
        
        Returns:
            Список занятий
        """
        logger.info(f"Синхронизация расписания заочного обучения: {academic_year}, семестр {semester}")
        
        try:
            # Попробовать найти таблицу для заочников
            # Возможно, это fraspz или fgrafikz
            query = """
                SELECT 
                    qdate as lesson_date,
                    qnurok as lesson_number,
                    qkgrup as group_code,
                    qkpred as subject_id,
                    qkvzan as lesson_type_id,
                    qkprep1 as teacher1_id,
                    qkprep2 as teacher2_id,
                    qkprep3 as teacher3_id,
                    qkaud1 as audience1_id,
                    qkaud2 as audience2_id,
                    qkaud3 as audience3_id
                FROM fraspz
                WHERE 1=1
            """
            
            params = []
            
            if start_date:
                query += " AND qdate >= %s"
                params.append(start_date)
            
            if end_date:
                query += " AND qdate <= %s"
                params.append(end_date)
            
            query += " ORDER BY qdate, qnurok"
            
            lessons = self.arma.execute_query(query, tuple(params) if params else None)
            
            for lesson in lessons:
                lesson['time_start'], lesson['time_end'] = self._get_lesson_time(lesson['lesson_number'])
                lesson['study_form'] = 'extramural'
                lesson['academic_year'] = academic_year
                lesson['semester'] = semester
            
            logger.info(f"Найдено занятий для заочников: {len(lessons)}")
            return lessons
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации расписания заочного обучения: {e}")
            # Возможно, таблица называется по-другому
            return []
    
    def sync_exams(
        self,
        academic_year: str,
        semester: int,
        study_form: str = 'extramural'
    ) -> List[Dict[str, Any]]:
        """
        Синхронизация экзаменов и зачетов
        
        Args:
            academic_year: Учебный год
            semester: Номер семестра
            study_form: Форма обучения
        
        Returns:
            Список экзаменов/зачетов
        """
        logger.info(f"Синхронизация экзаменов: {academic_year}, семестр {semester}, форма {study_form}")
        
        try:
            # Для заочников экзамены могут быть в fgrafikz или fuplanz
            # Нужно проверить структуру таблиц
            
            # Вариант 1: Из fgrafikz (если там есть экзамены)
            query = """
                SELECT 
                    qdate as exam_date,
                    qkgrup as group_code,
                    qkpred as subject_id,
                    qkprep1 as teacher1_id,
                    qkaud1 as audience_id,
                    qekzamen as is_exam,
                    qzachet as is_test
                FROM fgrafikz
                WHERE (qekzamen = 1 OR qzachet = 1)
            """
            
            exams = self.arma.execute_query(query)
            
            # Добавить тип экзамена
            for exam in exams:
                if exam.get('is_exam') == 1:
                    exam['exam_type'] = 'exam'
                elif exam.get('is_test') == 1:
                    exam['exam_type'] = 'test'
                exam['study_form'] = study_form
                exam['academic_year'] = academic_year
                exam['semester'] = semester
            
            logger.info(f"Найдено экзаменов/зачетов: {len(exams)}")
            return exams
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации экзаменов: {e}")
            return []
    
    def sync_session_periods(
        self,
        academic_year: str,
        semester: int
    ) -> List[Dict[str, Any]]:
        """
        Синхронизация периодов сессий из SSESG1
        
        Args:
            academic_year: Учебный год
            semester: Номер семестра
        
        Returns:
            Список периодов сессий
        """
        logger.info(f"Синхронизация периодов сессий: {academic_year}, семестр {semester}")
        
        try:
            query = """
                SELECT 
                    qkfk as faculty_id,
                    quchg1 as year_start,
                    quchg2 as year_end,
                    qsemes as semester,
                    qkgr as group_code,
                    qkurs as course,
                    qdatsn as session_start,
                    qdatsk as session_end
                FROM ssesg1
                WHERE quchg1 = %s AND quchg2 = %s AND qsemes = %s
            """
            
            year_start, year_end = academic_year.split('-')
            sessions = self.arma.execute_query(query, (year_start, year_end, semester))
            
            logger.info(f"Найдено периодов сессий: {len(sessions)}")
            return sessions
            
        except Exception as e:
            logger.error(f"Ошибка синхронизации периодов сессий: {e}")
            return []
    
    def _get_lesson_time(self, lesson_number: int) -> tuple:
        """
        Получение времени начала и окончания занятия по номеру урока
        
        Args:
            lesson_number: Номер урока (1-6)
        
        Returns:
            Кортеж (time_start, time_end)
        """
        # Стандартное расписание (можно настроить)
        schedule = {
            1: ("08:00:00", "09:30:00"),
            2: ("09:45:00", "11:15:00"),
            3: ("11:30:00", "13:00:00"),
            4: ("13:30:00", "15:00:00"),
            5: ("15:15:00", "16:45:00"),
            6: ("17:00:00", "18:30:00"),
        }
        
        return schedule.get(lesson_number, ("08:00:00", "09:30:00"))


if __name__ == "__main__":
    # Тестирование синхронизации
    import logging
    
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    from db_connection import get_connector
    
    connector = get_connector()
    
    if connector.test_connection():
        sync = ScheduleSync(connector)
        
        # Синхронизация справочников
        sync.sync_references()
        
        # Синхронизация расписания на текущую неделю
        today = date.today()
        week_start = today - timedelta(days=today.weekday())
        week_end = week_start + timedelta(days=6)
        
        lessons = sync.sync_schedule_full_time(
            academic_year="2023-2024",
            semester=1,
            start_date=week_start,
            end_date=week_end
        )
        
        print(f"\n✅ Найдено занятий на неделю: {len(lessons)}")
        
        if lessons:
            print("\nПример занятия:")
            print(lessons[0])
    
    connector.close()

