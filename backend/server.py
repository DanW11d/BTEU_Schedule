"""
Простой REST API сервер для BTEU Schedule
Работает с PostgreSQL базой данных
"""
from flask import Flask, jsonify, request
from flask_cors import CORS
import psycopg2
from psycopg2.extras import RealDictCursor
import os
from datetime import datetime
from dotenv import load_dotenv
from pathlib import Path
from werkzeug.utils import secure_filename
import tempfile
from excel_parser import ExcelScheduleParser
from file_processor import extract_group_code, parse_filename
from exam_parser import ExamScheduleParser
from ai_service import AIService
from schedule_analytics import ScheduleAnalytics

# Загружаем переменные окружения из .env файла
load_dotenv()

app = Flask(__name__)
# Настройка CORS для Android приложения
CORS(app, resources={
    r"/v1/*": {
        "origins": "*",
        "methods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        "allow_headers": ["Content-Type", "Authorization"]
    }
})

# Логирование всех запросов
@app.before_request
def log_request_info():
    print(f"\n{'='*60}")
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {request.method} {request.path}")
    print(f"Query params: {dict(request.args)}")
    if request.is_json:
        print(f"JSON body: {request.get_json()}")
    print(f"{'='*60}")

@app.after_request
def log_response_info(response):
    print(f"Response: {response.status_code} {response.status}")
    if response.is_json:
        try:
            data = response.get_json()
            if isinstance(data, list):
                print(f"Response items count: {len(data)}")
            elif isinstance(data, dict):
                print(f"Response keys: {list(data.keys())}")
        except:
            pass
    return response

# Настройки для загрузки файлов
UPLOAD_FOLDER = tempfile.gettempdir()
ALLOWED_EXTENSIONS = {'xlsx', 'xls'}
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16 MB max file size

def allowed_file(filename):
    """Проверяет, разрешен ли тип файла"""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

# Настройки подключения к БД
# Используем прямые значения для надежности
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'postgres'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', '7631')
}

# Инициализация AI сервиса и аналитики
ai_service = AIService()
schedule_analytics = ScheduleAnalytics()

def get_db_connection():
    """Создает подключение к БД"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"Ошибка подключения к БД: {e}")
        raise

@app.route('/v1/faculties', methods=['GET'])
def get_faculties():
    """Получить список факультетов"""
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("""
            SELECT id, code, name_ru as name, name_en, description, is_active
            FROM faculties
            WHERE is_active = TRUE
            ORDER BY code
        """)
        
        faculties = cur.fetchall()
        cur.close()
        conn.close()
        
        return jsonify([dict(f) for f in faculties]), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/faculties/<int:faculty_id>/departments', methods=['GET'])
def get_departments(faculty_id):
    """Получить кафедры факультета"""
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("""
            SELECT id, code, name_ru as name, name_en, faculty_id, description, is_active
            FROM departments
            WHERE faculty_id = %s AND is_active = TRUE
            ORDER BY name_ru
        """, (faculty_id,))
        
        departments = cur.fetchall()
        cur.close()
        conn.close()
        
        return jsonify([dict(d) for d in departments]), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/departments', methods=['GET'])
def get_all_departments():
    """Получить все кафедры"""
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("""
            SELECT 
                d.id, 
                d.code, 
                d.name_ru as name, 
                d.name_en, 
                d.faculty_id,
                f.code as faculty_code,
                f.name_ru as faculty_name,
                d.description, 
                d.is_active
            FROM departments d
            JOIN faculties f ON d.faculty_id = f.id
            WHERE d.is_active = TRUE AND f.is_active = TRUE
            ORDER BY f.code, d.name_ru
        """)
        
        departments = cur.fetchall()
        cur.close()
        conn.close()
        
        return jsonify([dict(d) for d in departments]), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/groups', methods=['GET'])
def get_groups():
    """Получить группы по факультету и форме обучения"""
    try:
        faculty_code = request.args.get('faculty')
        education_form = request.args.get('form')
        
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        query = """
            SELECT 
                g.id,
                g.code,
                g.name,
                g.faculty_id,
                f.code as faculty_code,
                f.name_ru as faculty_name,
                g.department_id,
                d.name_ru as department_name,
                g.specialization,
                g.course,
                g.education_form,
                g.student_count,
                g.is_active
            FROM groups g
            JOIN faculties f ON g.faculty_id = f.id
            LEFT JOIN departments d ON g.department_id = d.id
            WHERE g.is_active = TRUE
        """
        
        params = []
        if faculty_code:
            query += " AND f.code = %s"
            params.append(faculty_code)
        
        if education_form:
            query += " AND g.education_form = %s"
            params.append(education_form)
        
        query += " ORDER BY g.course, g.code"
        
        cur.execute(query, params)
        groups = cur.fetchall()
        cur.close()
        conn.close()
        
        # Преобразуем в формат, ожидаемый Android приложением
        result = []
        for g in groups:
            result.append({
                'id': g['id'],
                'code': g['code'],
                'name': g['name'],
                'facultyId': g['faculty_id'],
                'facultyCode': g['faculty_code'],
                'facultyName': g['faculty_name'],
                'departmentId': g['department_id'],
                'departmentName': g['department_name'],
                'specialization': g['specialization'],
                'course': g['course'],
                'educationForm': g['education_form'],
                'studentCount': g['student_count'] or 0,
                'isActive': g['is_active']
            })
        
        return jsonify(result), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/groups/<code>', methods=['GET'])
def get_group(code):
    """Получить информацию о группе"""
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("""
            SELECT 
                g.id,
                g.code,
                g.name,
                g.faculty_id,
                f.code as faculty_code,
                f.name_ru as faculty_name,
                g.department_id,
                d.name_ru as department_name,
                g.specialization,
                g.course,
                g.education_form,
                g.student_count,
                g.is_active
            FROM groups g
            JOIN faculties f ON g.faculty_id = f.id
            LEFT JOIN departments d ON g.department_id = d.id
            WHERE g.code = %s AND g.is_active = TRUE
        """, (code,))
        
        group = cur.fetchone()
        cur.close()
        conn.close()
        
        if not group:
            return jsonify({'error': 'Group not found'}), 404
        
        return jsonify({
            'id': group['id'],
            'code': group['code'],
            'name': group['name'],
            'facultyId': group['faculty_id'],
            'facultyCode': group['faculty_code'],
            'facultyName': group['faculty_name'],
            'departmentId': group['department_id'],
            'departmentName': group['department_name'],
            'specialization': group['specialization'],
            'course': group['course'],
            'educationForm': group['education_form'],
            'studentCount': group['student_count'] or 0,
            'isActive': group['is_active']
        }), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/schedule/group/<code>/day/<int:day>', methods=['GET'])
def get_day_schedule(code, day):
    """Получить расписание на день"""
    try:
        week_parity = request.args.get('week')  # odd, even, или null для both
        
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        # Получаем group_id по коду
        cur.execute("SELECT id FROM groups WHERE code = %s AND is_active = TRUE", (code,))
        group_result = cur.fetchone()
        
        if not group_result:
            cur.close()
            conn.close()
            return jsonify({'error': 'Group not found'}), 404
        
        group_id = group_result['id']
        
        # Получаем расписание
        query = """
            SELECT 
                l.id,
                l.group_id,
                l.day_of_week,
                l.lesson_number,
                l.subject,
                l.teacher,
                l.classroom,
                l.lesson_type,
                l.week_parity,
                l.building,
                l.notes,
                b.lesson_start,
                b.lesson_end
            FROM lessons l
            LEFT JOIN bell_schedule b ON l.lesson_number = b.lesson_number
            WHERE l.group_id = %s 
                AND l.day_of_week = %s
                AND l.is_active = TRUE
        """
        
        params = [group_id, day]
        
        if week_parity:
            query += " AND (l.week_parity = %s OR l.week_parity = 'both')"
            params.append(week_parity)
        else:
            query += " AND (l.week_parity = 'both' OR l.week_parity IS NOT NULL)"
        
        query += " ORDER BY l.lesson_number"
        
        cur.execute(query, params)
        lessons = cur.fetchall()
        cur.close()
        conn.close()
        
        result = []
        for l in lessons:
            result.append({
                'id': l['id'],
                'groupId': l['group_id'],
                'dayOfWeek': l['day_of_week'],
                'lessonNumber': l['lesson_number'],
                'subject': l['subject'],
                'teacher': l['teacher'] or '',
                'classroom': l['classroom'] or '',
                'lessonType': l['lesson_type'] or 'lecture',  # Значение по умолчанию, если null
                'weekParity': l['week_parity'] or 'both',  # Значение по умолчанию, если null
                'building': l['building'] or '',
                'notes': l['notes'] or '',
                'timeStart': str(l['lesson_start']) if l['lesson_start'] else None,
                'timeEnd': str(l['lesson_end']) if l['lesson_end'] else None
            })
        
        return jsonify(result), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/schedule/group/<code>/week', methods=['GET'])
def get_week_schedule(code):
    """Получить расписание на неделю"""
    try:
        week_parity = request.args.get('week')
        
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("SELECT id FROM groups WHERE code = %s AND is_active = TRUE", (code,))
        group_result = cur.fetchone()
        
        if not group_result:
            cur.close()
            conn.close()
            return jsonify({'error': 'Group not found'}), 404
        
        group_id = group_result['id']
        
        query = """
            SELECT 
                l.id,
                l.group_id,
                l.day_of_week,
                l.lesson_number,
                l.subject,
                l.teacher,
                l.classroom,
                l.lesson_type,
                l.week_parity,
                l.building,
                l.notes,
                b.lesson_start,
                b.lesson_end
            FROM lessons l
            LEFT JOIN bell_schedule b ON l.lesson_number = b.lesson_number
            WHERE l.group_id = %s AND l.is_active = TRUE
        """
        
        params = [group_id]
        
        if week_parity:
            query += " AND (l.week_parity = %s OR l.week_parity = 'both')"
            params.append(week_parity)
        
        query += " ORDER BY l.day_of_week, l.lesson_number"
        
        cur.execute(query, params)
        lessons = cur.fetchall()
        cur.close()
        conn.close()
        
        result = []
        for l in lessons:
            result.append({
                'id': l['id'],
                'groupId': l['group_id'],
                'dayOfWeek': l['day_of_week'],
                'lessonNumber': l['lesson_number'],
                'subject': l['subject'],
                'teacher': l['teacher'] or '',
                'classroom': l['classroom'] or '',
                'lessonType': l['lesson_type'] or 'lecture',  # Значение по умолчанию, если null
                'weekParity': l['week_parity'] or 'both',  # Значение по умолчанию, если null
                'building': l['building'] or '',
                'notes': l['notes'] or '',
                'timeStart': str(l['lesson_start']) if l['lesson_start'] else None,
                'timeEnd': str(l['lesson_end']) if l['lesson_end'] else None
            })
        
        return jsonify(result), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/exams/group/<code>', methods=['GET'])
def get_exams(code):
    """Получить расписание экзаменов для группы"""
    try:
        parser = ExamScheduleParser()
        exams = parser.parse_exams(code, "exam")
        
        # Форматируем результат
        result = []
        for idx, exam in enumerate(exams, 1):
            result.append({
                'id': idx,
                'groupId': code,
                'subject': exam.get('subject', ''),
                'teacher': exam.get('teacher', ''),
                'date': exam.get('date', ''),
                'time': exam.get('time', ''),
                'classroom': exam.get('classroom', ''),
                'examType': 'exam',
                'notes': None
            })
        
        return jsonify(result), 200
    except Exception as e:
        print(f"Ошибка получения экзаменов для группы {code}: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e), 'exams': []}), 200  # Возвращаем пустой список вместо ошибки

@app.route('/v1/tests/group/<code>', methods=['GET'])
def get_tests(code):
    """Получить расписание зачетов для группы"""
    try:
        parser = ExamScheduleParser()
        tests = parser.parse_exams(code, "test")
        
        # Форматируем результат
        result = []
        for idx, test in enumerate(tests, 1):
            result.append({
                'id': idx,
                'groupId': code,
                'subject': test.get('subject', ''),
                'teacher': test.get('teacher', ''),
                'date': test.get('date', ''),
                'time': test.get('time', ''),
                'classroom': test.get('classroom', ''),
                'examType': 'test',
                'notes': None
            })
        
        return jsonify(result), 200
    except Exception as e:
        print(f"Ошибка получения зачетов для группы {code}: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e), 'tests': []}), 200  # Возвращаем пустой список вместо ошибки

@app.route('/v1/ai/chat', methods=['POST'])
def ai_chat():
    """AI чат для вопросов о расписании"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Необходимо передать JSON данные'}), 400
        
        user_message = data.get('message', '').strip()
        group_code = data.get('group_code')
        
        if not user_message:
            return jsonify({'error': 'Сообщение не может быть пустым'}), 400
        
        # Получаем данные расписания и экзаменов для контекста
        schedule_data = None
        exams_data = None
        if group_code:
            try:
                conn = get_db_connection()
                cur = conn.cursor(cursor_factory=RealDictCursor)
                
                cur.execute("SELECT id FROM groups WHERE code = %s AND is_active = TRUE", (group_code,))
                group_result = cur.fetchone()
                
                if group_result:
                    group_id = group_result['id']
                    # Получаем все занятия (не только 20)
                    cur.execute("""
                        SELECT 
                            l.day_of_week,
                            l.lesson_number,
                            l.subject,
                            l.teacher,
                            l.classroom,
                            l.lesson_type,
                            l.week_parity,
                            l.building,
                            b.lesson_start,
                            b.lesson_end
                        FROM lessons l
                        LEFT JOIN bell_schedule b ON l.lesson_number = b.lesson_number
                        WHERE l.group_id = %s AND l.is_active = TRUE
                        ORDER BY l.day_of_week, l.lesson_number
                    """, (group_id,))
                    
                    lessons = cur.fetchall()
                    schedule_data = {
                        'group_code': group_code,
                        'lessons': [dict(l) for l in lessons]
                    }
                    
                    # Получаем экзамены для приоритетов
                    try:
                        parser = ExamScheduleParser()
                        exams = parser.parse_exams(group_code, "exam")
                        exams_data = [dict(e) for e in exams]
                    except:
                        exams_data = []
                
                cur.close()
                conn.close()
            except Exception as e:
                print(f"Ошибка получения расписания для контекста: {e}")
                # Продолжаем без контекста расписания
        
        # Получаем умный ответ от AI с аналитикой
        ai_response = ai_service.get_response(
            user_message=user_message,
            group_code=group_code,
            schedule_data=schedule_data,
            exams_data=exams_data
        )
        
        return jsonify({
            'response': ai_response,
            'group_code': group_code
        }), 200
        
    except Exception as e:
        print(f"Ошибка AI чата: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e)}), 500

@app.route('/v1/ai/status', methods=['GET'])
def ai_status():
    """Проверка статуса AI сервиса"""
    try:
        is_configured = ai_service.is_configured()
        return jsonify({
            'configured': is_configured,
            'provider': ai_service.provider,
            'message': 'AI сервис настроен' if is_configured else 'AI сервис не настроен. Установите API ключ в переменных окружения.'
        }), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/analytics/group/<code>', methods=['GET'])
def get_schedule_analytics(code):
    """Получить аналитику расписания для группы"""
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("SELECT id FROM groups WHERE code = %s AND is_active = TRUE", (code,))
        group_result = cur.fetchone()
        
        if not group_result:
            cur.close()
            conn.close()
            return jsonify({'error': 'Group not found'}), 404
        
        group_id = group_result['id']
        
        # Получаем все занятия
        cur.execute("""
            SELECT 
                l.day_of_week,
                l.lesson_number,
                l.subject,
                l.teacher,
                l.classroom,
                l.lesson_type,
                l.week_parity
            FROM lessons l
            WHERE l.group_id = %s AND l.is_active = TRUE
            ORDER BY l.day_of_week, l.lesson_number
        """, (group_id,))
        
        lessons = [dict(l) for l in cur.fetchall()]
        
        # Получаем экзамены
        exams_data = []
        try:
            parser = ExamScheduleParser()
            exams = parser.parse_exams(code, "exam")
            exams_data = [dict(e) for e in exams]
        except:
            pass
        
        cur.close()
        conn.close()
        
        # Вычисляем аналитику
        weekly_load = schedule_analytics.calculate_weekly_load(lessons)
        hour_balance = schedule_analytics.calculate_hour_balance(lessons)
        priorities = schedule_analytics.identify_priorities(lessons, exams_data)
        optimal_schedule = schedule_analytics.suggest_optimal_schedule(lessons)
        
        return jsonify({
            'group_code': code,
            'weekly_load': weekly_load,
            'hour_balance': hour_balance,
            'priorities': {
                'high_priority': priorities['high_priority'][:5],
                'medium_priority': priorities['medium_priority'][:5],
                'upcoming_exams': priorities['upcoming_exams']
            },
            'optimization': optimal_schedule
        }), 200
        
    except Exception as e:
        print(f"Ошибка получения аналитики: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e)}), 500

@app.route('/v1/ai/find-next-lesson', methods=['POST'])
def find_next_lesson():
    """Найти следующее занятие по предмету"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Необходимо передать JSON данные'}), 400
        
        subject_query = data.get('subject', '').strip()
        group_code = data.get('group_code')
        
        if not subject_query or not group_code:
            return jsonify({'error': 'Необходимо указать subject и group_code'}), 400
        
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("SELECT id FROM groups WHERE code = %s AND is_active = TRUE", (group_code,))
        group_result = cur.fetchone()
        
        if not group_result:
            cur.close()
            conn.close()
            return jsonify({'error': 'Group not found'}), 404
        
        group_id = group_result['id']
        
        cur.execute("""
            SELECT 
                l.day_of_week,
                l.lesson_number,
                l.subject,
                l.teacher,
                l.classroom,
                l.lesson_type,
                l.week_parity,
                b.lesson_start,
                b.lesson_end
            FROM lessons l
            LEFT JOIN bell_schedule b ON l.lesson_number = b.lesson_number
            WHERE l.group_id = %s AND l.is_active = TRUE
            ORDER BY l.day_of_week, l.lesson_number
        """, (group_id,))
        
        lessons = [dict(l) for l in cur.fetchall()]
        cur.close()
        conn.close()
        
        next_lesson = schedule_analytics.find_next_lesson(lessons, subject_query)
        
        if next_lesson:
            return jsonify({
                'found': True,
                'lesson': next_lesson
            }), 200
        else:
            return jsonify({
                'found': False,
                'message': f'Занятие по "{subject_query}" не найдено'
            }), 200
        
    except Exception as e:
        print(f"Ошибка поиска занятия: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e)}), 500

@app.route('/v1/search', methods=['GET'])
def search_lessons():
    """Поиск занятий по запросу"""
    try:
        query = request.args.get('q', '').strip()
        group_code = request.args.get('group', None)
        
        if not query:
            return jsonify({
                'error': 'Query parameter "q" is required'
            }), 400
        
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        # Строим SQL запрос для поиска
        # Ищем в предметах, преподавателях и аудиториях
        search_pattern = f'%{query}%'
        
        sql = """
            SELECT DISTINCT
                l.id,
                l.lesson_number,
                l.day_of_week,
                COALESCE(
                    TO_CHAR(bs.lesson_start, 'HH24:MI') || '-' || TO_CHAR(bs.lesson_end, 'HH24:MI'),
                    ''
                ) as time,
                l.subject,
                COALESCE(l.teacher, '') as teacher,
                COALESCE(l.classroom, '') as classroom,
                COALESCE(l.lesson_type, 'lecture') as lesson_type,
                COALESCE(l.week_parity, 'both') as week_parity
            FROM lessons l
            LEFT JOIN bell_schedule bs ON l.lesson_number = bs.lesson_number
            INNER JOIN groups g ON l.group_id = g.id
            WHERE (
                LOWER(l.subject) LIKE LOWER(%s) OR
                LOWER(l.teacher) LIKE LOWER(%s) OR
                LOWER(l.classroom) LIKE LOWER(%s)
            )
        """
        params = [search_pattern, search_pattern, search_pattern]
        
        # Если указана группа, фильтруем по ней
        if group_code:
            sql += " AND g.code = %s"
            params.append(group_code)
        
        sql += " ORDER BY l.day_of_week, l.lesson_number"
        
        cur.execute(sql, params)
        lessons = cur.fetchall()
        
        cur.close()
        conn.close()
        
        # Преобразуем в JSON
        result = []
        for lesson in lessons:
            result.append({
                'id': lesson['id'],
                'lessonNumber': lesson['lesson_number'],
                'dayOfWeek': lesson['day_of_week'],
                'time': lesson['time'] or '',
                'subject': lesson['subject'],
                'teacher': lesson['teacher'] or '',
                'classroom': lesson['classroom'] or '',
                'lessonType': lesson['lesson_type'],
                'weekParity': lesson['week_parity']
            })
        
        return jsonify(result), 200
        
    except Exception as e:
        print(f"Ошибка поиска: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({
            'error': str(e)
        }), 500

@app.route('/v1/bell-schedule', methods=['GET'])
def get_bell_schedule():
    """Получить расписание звонков"""
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("""
            SELECT 
                lesson_number,
                lesson_start,
                lesson_end,
                break_time_minutes,
                break_after_lesson_minutes,
                description
            FROM bell_schedule
            ORDER BY lesson_number
        """)
        
        bells = cur.fetchall()
        cur.close()
        conn.close()
        
        result = []
        for b in bells:
            # Преобразуем время в формат HH:MM (без секунд)
            lesson_start = str(b['lesson_start'])[:5] if b['lesson_start'] else None
            lesson_end = str(b['lesson_end'])[:5] if b['lesson_end'] else None
            result.append({
                'lessonNumber': b['lesson_number'],
                'lessonStart': lesson_start,
                'lessonEnd': lesson_end,
                'breakTimeMinutes': b['break_time_minutes'],
                'breakAfterLessonMinutes': b['break_after_lesson_minutes'],
                'description': b['description'] or ''
            })
        
        return jsonify(result), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/admin/parse-excel', methods=['POST'])
def parse_excel():
    """Парсинг Excel файла с расписанием"""
    try:
        # Проверяем наличие файла
        if 'file' not in request.files:
            return jsonify({'error': 'Файл не загружен'}), 400
        
        file = request.files['file']
        if file.filename == '':
            return jsonify({'error': 'Файл не выбран'}), 400
        
        if not allowed_file(file.filename):
            return jsonify({'error': 'Неподдерживаемый формат файла. Используйте .xlsx или .xls'}), 400
        
        # Получаем параметры
        group_code = request.form.get('group_code')
        group_id = request.form.get('group_id', type=int)
        
        if not group_code:
            return jsonify({'error': 'Не указан код группы'}), 400
        
        if not group_id:
            # Пытаемся найти group_id по коду
            conn = get_db_connection()
            cur = conn.cursor(cursor_factory=RealDictCursor)
            cur.execute("SELECT id FROM groups WHERE code = %s AND is_active = TRUE", (group_code,))
            group_result = cur.fetchone()
            cur.close()
            conn.close()
            
            if not group_result:
                return jsonify({'error': f'Группа с кодом {group_code} не найдена'}), 404
            
            group_id = group_result['id']
        
        # Сохраняем файл во временную директорию
        filename = secure_filename(file.filename)
        file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(file_path)
        
        try:
            # Парсим файл
            parser = ExcelScheduleParser(file_path)
            # Отладочная информация
            print(f"[DEBUG] Парсинг файла: {file_path}")
            print(f"[DEBUG] Код группы: {group_code}, ID: {group_id}")
            lessons = parser.parse(group_code, group_id)
            print(f"[DEBUG] Найдено занятий: {len(lessons)}")
            if len(lessons) > 0:
                print(f"[DEBUG] Первое занятие: {lessons[0]}")
            
            # Валидируем данные
            is_valid, errors = parser.validate_lessons(lessons)
            if not is_valid:
                # Если занятий нет, это не ошибка валидации, а пустой файл
                if len(lessons) == 0:
                    return jsonify({
                        'success': True,
                        'message': 'Файл обработан, но занятий не найдено',
                        'lessons_parsed': 0,
                        'lessons_saved': 0,
                        'warnings': ['Файл не содержит занятий или имеет неподдерживаемый формат']
                    }), 200
                
                # Если ошибок валидации немного (<30% от занятий), продолжаем с предупреждениями
                if len(errors) < len(lessons) * 0.3:
                    print(f"[WARNING] Файл {filename}: {len(errors)} ошибок валидации из {len(lessons)} занятий")
                    # Продолжаем обработку, но логируем предупреждения
                else:
                    # Слишком много ошибок - возвращаем ошибку
                    return jsonify({
                        'error': 'Слишком много ошибок валидации',
                        'errors': errors[:10],  # Показываем первые 10
                        'errors_count': len(errors),
                        'lessons_count': len(lessons)
                    }), 400
            
            # Фильтруем невалидные занятия перед сохранением
            valid_lessons = []
            invalid_count = 0
            
            for lesson in lessons:
                # Базовая проверка валидности
                if (lesson.get('subject') and 
                    lesson.get('day_of_week') and 1 <= lesson['day_of_week'] <= 6 and
                    lesson.get('lesson_number') and 1 <= lesson['lesson_number'] <= 7 and
                    lesson.get('group_id') and
                    lesson.get('week_parity')):
                    valid_lessons.append(lesson)
                else:
                    invalid_count += 1
                    # Отладка невалидных занятий
                    if invalid_count <= 3:
                        print(f"[DEBUG] Невалидное занятие: subject={lesson.get('subject', 'N/A')[:30]}, day={lesson.get('day_of_week')}, lesson_num={lesson.get('lesson_number')}, group_id={lesson.get('group_id')}, week_parity={lesson.get('week_parity')}")
            
            if invalid_count > 0:
                print(f"[WARNING] Отфильтровано {invalid_count} невалидных занятий из {len(lessons)}")
                print(f"[DEBUG] Валидных занятий: {len(valid_lessons)}")
            
            # Сохраняем в БД только валидные занятия
            conn = get_db_connection()
            cur = conn.cursor()
            
            saved_count = 0
            updated_count = 0
            errors_save = []
            
            for lesson in valid_lessons:
                try:
                    # Проверяем, нет ли уже такого занятия
                    cur.execute("""
                        SELECT id FROM lessons 
                        WHERE group_id = %s 
                            AND day_of_week = %s 
                            AND lesson_number = %s 
                            AND week_parity = %s
                            AND is_active = TRUE
                    """, (
                        lesson['group_id'],
                        lesson['day_of_week'],
                        lesson['lesson_number'],
                        lesson['week_parity']
                    ))
                    
                    existing = cur.fetchone()
                    
                    if existing:
                        # Обновляем существующее занятие
                        cur.execute("""
                            UPDATE lessons SET
                                subject = %s,
                                teacher = %s,
                                classroom = %s,
                                lesson_type = %s,
                                building = %s,
                                notes = %s,
                                updated_at = NOW()
                            WHERE id = %s
                        """, (
                            lesson['subject'],
                            lesson.get('teacher'),
                            lesson.get('classroom'),
                            lesson['lesson_type'],
                            lesson.get('building'),
                            lesson.get('notes'),
                            existing[0]
                        ))
                        updated_count += 1
                    else:
                        # Создаем новое занятие
                        cur.execute("""
                            INSERT INTO lessons (
                                group_id, day_of_week, lesson_number,
                                subject, teacher, classroom, lesson_type,
                                week_parity, building, notes, is_active, created_at
                            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, TRUE, NOW())
                        """, (
                            lesson['group_id'],
                            lesson['day_of_week'],
                            lesson['lesson_number'],
                            lesson['subject'],
                            lesson.get('teacher'),
                            lesson.get('classroom'),
                            lesson['lesson_type'],
                            lesson['week_parity'],
                            lesson.get('building'),
                            lesson.get('notes')
                        ))
                    
                    saved_count += 1
                except Exception as e:
                    errors_save.append(f"Ошибка сохранения занятия: {str(e)}")
                    print(f"[ERROR] Ошибка сохранения занятия: {str(e)}")
                    print(f"[ERROR] Данные занятия: {lesson}")
            
            conn.commit()
            cur.close()
            conn.close()
            
            # Удаляем временный файл
            try:
                os.remove(file_path)
            except:
                pass
            
            return jsonify({
                'success': True,
                'message': f'Обработано занятий: {saved_count}',
                'lessons_parsed': len(lessons),
                'lessons_saved': saved_count,
                'lessons_updated': updated_count,
                'lessons_filtered': invalid_count,
                'errors': errors_save if errors_save else None,
                'warnings': errors[:5] if not is_valid and len(errors) < len(lessons) * 0.3 else None
            }), 200
            
        except Exception as e:
            # Удаляем временный файл в случае ошибки
            try:
                os.remove(file_path)
            except:
                pass
            return jsonify({'error': f'Ошибка парсинга файла: {str(e)}'}), 500
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/v1/admin/batch-parse', methods=['POST'])
def batch_parse():
    """Пакетная обработка Excel файлов из директории"""
    try:
        data = request.get_json() if request.is_json else request.form.to_dict()
        
        # Получаем путь к директории
        directory = data.get('directory') or request.form.get('directory')
        if not directory:
            return jsonify({'error': 'Не указана директория'}), 400
        
        if not os.path.isdir(directory):
            return jsonify({'error': f'Директория не существует: {directory}'}), 400
        
        # Сканируем директорию на наличие Excel файлов
        excel_files = []
        for ext in ['.xlsx', '.xls']:
            excel_files.extend(Path(directory).glob(f'*{ext}'))
        
        if not excel_files:
            return jsonify({
                'success': True,
                'message': 'Файлы не найдены',
                'files_processed': 0,
                'results': []
            }), 200
        
        results = {
            'total_files': len(excel_files),
            'processed': 0,
            'success': 0,
            'failed': 0,
            'skipped': 0,
            'results': []
        }
        
        # Обрабатываем каждый файл
        for file_path in excel_files:
            filename = file_path.name
            file_result = {
                'file': filename,
                'success': False
            }
            
            try:
                # Извлекаем код группы из имени файла
                group_code, modification_date = parse_filename(filename)
                
                if not group_code:
                    file_result['error'] = 'Не удалось извлечь код группы из имени файла'
                    results['failed'] += 1
                    results['results'].append(file_result)
                    continue
                
                file_result['group_code'] = group_code
                if modification_date:
                    file_result['modification_date'] = modification_date
                
                # Получаем group_id из БД
                conn = get_db_connection()
                cur = conn.cursor(cursor_factory=RealDictCursor)
                cur.execute(
                    "SELECT id FROM groups WHERE code = %s AND is_active = TRUE",
                    (group_code,)
                )
                group_result = cur.fetchone()
                cur.close()
                conn.close()
                
                if not group_result:
                    file_result['error'] = f'Группа "{group_code}" не найдена в БД'
                    results['skipped'] += 1
                    results['results'].append(file_result)
                    continue
                
                group_id = group_result['id']
                file_result['group_id'] = group_id
                
                # Парсим файл
                parser = ExcelScheduleParser(str(file_path))
                lessons = parser.parse(group_code, group_id)
                
                # Валидируем данные
                is_valid, errors = parser.validate_lessons(lessons)
                if not is_valid:
                    # Если занятий нет, это не ошибка валидации
                    if len(lessons) == 0:
                        file_result['success'] = True
                        file_result['lessons_parsed'] = 0
                        file_result['lessons_saved'] = 0
                        file_result['warning'] = 'Файл не содержит занятий'
                        results['success'] += 1
                        results['processed'] += 1
                        results['results'].append(file_result)
                        continue
                    
                    # Если ошибок валидации немного (<30% от занятий), продолжаем
                    if len(errors) < len(lessons) * 0.3:
                        # Продолжаем обработку с предупреждениями
                        file_result['validation_warnings'] = errors[:5]
                    else:
                        # Слишком много ошибок
                        file_result['error'] = 'Слишком много ошибок валидации'
                        file_result['validation_errors'] = errors[:10]
                        file_result['errors_count'] = len(errors)
                        file_result['lessons_count'] = len(lessons)
                        results['failed'] += 1
                        results['results'].append(file_result)
                        continue
                
                # Фильтруем невалидные занятия перед сохранением
                valid_lessons = []
                invalid_count = 0
                
                for lesson in lessons:
                    # Базовая проверка валидности
                    if (lesson.get('subject') and 
                        lesson.get('day_of_week') and 1 <= lesson['day_of_week'] <= 6 and
                        lesson.get('lesson_number') and 1 <= lesson['lesson_number'] <= 7 and
                        lesson.get('group_id') and
                        lesson.get('week_parity')):
                        valid_lessons.append(lesson)
                    else:
                        invalid_count += 1
                
                # Сохраняем в БД только валидные занятия
                conn = get_db_connection()
                cur = conn.cursor()
                
                saved_count = 0
                updated_count = 0
                errors_save = []
                
                for lesson in valid_lessons:
                    try:
                        # Проверяем, нет ли уже такого занятия
                        cur.execute("""
                            SELECT id FROM lessons
                            WHERE group_id = %s
                                AND day_of_week = %s
                                AND lesson_number = %s
                                AND week_parity = %s
                                AND is_active = TRUE
                        """, (
                            lesson['group_id'],
                            lesson['day_of_week'],
                            lesson['lesson_number'],
                            lesson['week_parity']
                        ))
                        
                        existing = cur.fetchone()
                        
                        if existing:
                            # Обновляем существующее занятие
                            cur.execute("""
                                UPDATE lessons SET
                                    subject = %s,
                                    teacher = %s,
                                    classroom = %s,
                                    lesson_type = %s,
                                    building = %s,
                                    notes = %s,
                                    updated_at = NOW()
                                WHERE id = %s
                            """, (
                                lesson['subject'],
                                lesson.get('teacher'),
                                lesson.get('classroom'),
                                lesson['lesson_type'],
                                lesson.get('building'),
                                lesson.get('notes'),
                                existing[0]
                            ))
                            updated_count += 1
                        else:
                            # Создаем новое занятие
                            cur.execute("""
                                INSERT INTO lessons (
                                    group_id, day_of_week, lesson_number,
                                    subject, teacher, classroom, lesson_type,
                                    week_parity, building, notes, is_active, created_at
                                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, TRUE, NOW())
                            """, (
                                lesson['group_id'],
                                lesson['day_of_week'],
                                lesson['lesson_number'],
                                lesson['subject'],
                                lesson.get('teacher'),
                                lesson.get('classroom'),
                                lesson['lesson_type'],
                                lesson['week_parity'],
                                lesson.get('building'),
                                lesson.get('notes')
                            ))
                        
                        saved_count += 1
                    except Exception as e:
                        errors_save.append(f"Ошибка сохранения занятия: {str(e)}")
                
                conn.commit()
                cur.close()
                conn.close()
                
                file_result['success'] = True
                file_result['lessons_parsed'] = len(lessons)
                file_result['lessons_saved'] = saved_count
                file_result['lessons_updated'] = updated_count
                file_result['lessons_filtered'] = invalid_count
                if errors_save:
                    file_result['errors'] = errors_save
                
                results['success'] += 1
                results['processed'] += 1
                
            except Exception as e:
                file_result['error'] = f'Ошибка обработки файла: {str(e)}'
                results['failed'] += 1
            
            results['results'].append(file_result)
        
        return jsonify({
            'success': True,
            'message': f'Обработано файлов: {results["processed"]}/{results["total_files"]}',
            'total_files': results['total_files'],
            'processed': results['processed'],
            'success': results['success'],
            'failed': results['failed'],
            'skipped': results['skipped'],
            'results': results['results']
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/', methods=['GET'])
def root():
    """Корневой endpoint с информацией об API"""
    return jsonify({
        'service': 'BTEU Schedule API',
        'version': '1.0',
        'status': 'running',
        'base_url': '/v1/',
        'endpoints': {
            'health': '/v1/health',
            'faculties': '/v1/faculties',
            'departments': '/v1/departments',
            'groups': '/v1/groups?faculty={code}&form={form}',
            'schedule': '/v1/schedule/group/{code}/week?week={parity}',
            'exams': '/v1/exams/group/{code}',
            'tests': '/v1/tests/group/{code}',
            'bell_schedule': '/v1/bell-schedule',
            'search': '/v1/search?q={query}&group={code}'
        },
        'documentation': 'Все API endpoints находятся под /v1/',
        'example': 'http://localhost:8000/v1/health'
    }), 200

@app.route('/v1/', methods=['GET'])
def api_root():
    """Корневой endpoint API с информацией об endpoints"""
    return jsonify({
        'service': 'BTEU Schedule API',
        'version': '1.0',
        'status': 'running',
        'endpoints': {
            'health': 'GET /v1/health - Проверка работоспособности',
            'faculties': 'GET /v1/faculties - Список факультетов',
            'departments': 'GET /v1/departments - Все кафедры',
            'departments_by_faculty': 'GET /v1/faculties/{id}/departments - Кафедры факультета',
            'groups': 'GET /v1/groups?faculty={code}&form={form} - Список групп',
            'group_info': 'GET /v1/groups/{code} - Информация о группе',
            'schedule_day': 'GET /v1/schedule/group/{code}/day/{day}?week={parity} - Расписание на день',
            'schedule_week': 'GET /v1/schedule/group/{code}/week?week={parity} - Расписание на неделю',
            'exams': 'GET /v1/exams/group/{code} - Экзамены группы',
            'tests': 'GET /v1/tests/group/{code} - Зачеты группы',
            'bell_schedule': 'GET /v1/bell-schedule - Расписание звонков',
            'search': 'GET /v1/search?q={query}&group={code} - Поиск по расписанию'
        }
    }), 200

@app.route('/v1/health', methods=['GET'])
def health_check():
    """Проверка работоспособности сервера и БД"""
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT 1")
        cur.close()
        conn.close()
        return jsonify({'status': 'ok', 'database': 'connected'}), 200
    except Exception as e:
        return jsonify({'status': 'error', 'database': 'disconnected', 'error': str(e)}), 500

# Обработчик для OPTIONS запросов (CORS preflight)
@app.route('/v1/<path:path>', methods=['OPTIONS'])
def handle_options(path):
    """Обработка CORS preflight запросов"""
    return '', 200

# Глобальный обработчик ошибок
@app.errorhandler(404)
def not_found(error):
    """Обработчик 404 с информацией о доступных endpoints"""
    requested_path = request.path
    return jsonify({
        'error': 'Endpoint not found',
        'requested_path': requested_path,
        'message': 'Все API endpoints находятся под /v1/',
        'available_endpoints': {
            'health': '/v1/health',
            'faculties': '/v1/faculties',
            'groups': '/v1/groups',
            'schedule': '/v1/schedule/group/{code}/week',
            'api_info': '/v1/ - Список всех endpoints'
        },
        'example': 'http://localhost:8000/v1/health'
    }), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({'error': 'Internal server error', 'message': str(error)}), 500

@app.errorhandler(Exception)
def handle_exception(e):
    """Глобальный обработчик всех исключений"""
    print(f"Ошибка: {str(e)}")
    import traceback
    traceback.print_exc()
    return jsonify({'error': 'Server error', 'message': str(e)}), 500

if __name__ == '__main__':
    print("=" * 50)
    print("BTEU Schedule API Server")
    print("=" * 50)
    print(f"Database: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")
    print(f"User: {DB_CONFIG['user']}")
    print("=" * 50)
    print("Starting server on http://0.0.0.0:8000")
    print("API endpoints available at: http://localhost:8000/v1/")
    print("=" * 50)
    
    app.run(host='0.0.0.0', port=8000, debug=True)

