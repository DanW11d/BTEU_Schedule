"""
AI сервис для обработки запросов о расписании
Использует OpenAI API или Google Gemini API для ответов на вопросы пользователей
Поддерживает гибридный подход: on-device кэш + облачный LLM
"""
import os
import json
import hashlib
from typing import Optional, Dict, List, Tuple
from dotenv import load_dotenv
from schedule_analytics import ScheduleAnalytics

load_dotenv()

class AIService:
    """Сервис для работы с AI моделями с поддержкой кэширования и аналитики"""
    
    def __init__(self):
        self.api_key = os.getenv('OPENAI_API_KEY') or os.getenv('GEMINI_API_KEY')
        self.provider = os.getenv('AI_PROVIDER', 'openai')  # 'openai' или 'gemini'
        self.cache = {}  # Простой in-memory кэш (в продакшене использовать Redis)
        self.analytics = ScheduleAnalytics()
        
    def _get_schedule_context(
        self, 
        group_code: Optional[str] = None,
        schedule_data: Optional[Dict] = None,
        analytics_data: Optional[Dict] = None
    ) -> str:
        """Получает расширенный контекст расписания с аналитикой"""
        context = """Вы - умный AI-ассистент для приложения расписания занятий БТЭУ (Белорусский торгово-экономический университет).

Ваши возможности:
1. Анализ расписания: нагрузка на неделю, баланс часов, приоритеты занятий
2. Поиск конкретных занятий: "Когда у меня следующая пара по матану?"
3. Планирование: предложения по оптимизации расписания
4. Ответы на вопросы о расписании, преподавателях, аудиториях, экзаменах

Стиль ответов:
- Краткие, информативные ответы
- Используйте данные аналитики для более умных ответов
- Для вопросов типа "когда следующая пара по X" - дайте точный ответ с днем и временем
- Предлагайте рекомендации на основе анализа нагрузки
- Будьте дружелюбны и полезны

Формат ответов:
- Для простых вопросов: краткий прямой ответ
- Для аналитики: структурированный ответ с цифрами
- Для планирования: конкретные рекомендации
"""
        
        if group_code:
            context += f"\n\nГруппа пользователя: {group_code}"
        
        if schedule_data:
            context += f"\n\nДанные расписания:\n{json.dumps(schedule_data, ensure_ascii=False, indent=2)}"
        
        if analytics_data:
            context += f"\n\nАналитика расписания:\n{json.dumps(analytics_data, ensure_ascii=False, indent=2)}"
            context += "\n\nИспользуйте эту аналитику для более умных ответов о нагрузке, балансе часов и приоритетах."
        
        return context
    
    def _get_cache_key(self, user_message: str, group_code: Optional[str] = None) -> str:
        """Генерирует ключ кэша для запроса"""
        key_string = f"{user_message.lower().strip()}_{group_code or ''}"
        return hashlib.md5(key_string.encode()).hexdigest()
    
    def _check_cache(self, cache_key: str) -> Optional[str]:
        """Проверяет кэш (для простых запросов)"""
        return self.cache.get(cache_key)
    
    def _save_to_cache(self, cache_key: str, response: str):
        """Сохраняет ответ в кэш"""
        # Ограничиваем размер кэша (последние 100 запросов)
        if len(self.cache) > 100:
            # Удаляем самый старый элемент
            oldest_key = next(iter(self.cache))
            del self.cache[oldest_key]
        self.cache[cache_key] = response
    
    def _call_openai(self, user_message: str, context: str) -> str:
        """Вызов OpenAI API"""
        try:
            import openai
            
            if not self.api_key:
                return "Извините, AI сервис не настроен. Пожалуйста, настройте API ключ."
            
            openai.api_key = self.api_key
            
            response = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=[
                    {"role": "system", "content": context},
                    {"role": "user", "content": user_message}
                ],
                max_tokens=500,
                temperature=0.7
            )
            
            return response.choices[0].message.content.strip()
        except ImportError:
            return "Библиотека openai не установлена. Установите: pip install openai"
        except Exception as e:
            return f"Ошибка при обращении к AI: {str(e)}"
    
    def _call_gemini(self, user_message: str, context: str) -> str:
        """Вызов Google Gemini API"""
        try:
            import google.generativeai as genai
            
            if not self.api_key:
                return "Извините, AI сервис не настроен. Пожалуйста, настройте API ключ."
            
            genai.configure(api_key=self.api_key)
            model = genai.GenerativeModel('gemini-pro')
            
            full_prompt = f"{context}\n\nПользователь: {user_message}\n\nАссистент:"
            
            response = model.generate_content(full_prompt)
            return response.text.strip()
        except ImportError:
            return "Библиотека google-generativeai не установлена. Установите: pip install google-generativeai"
        except Exception as e:
            return f"Ошибка при обращении к AI: {str(e)}"
    
    def _call_ollama(self, user_message: str, context: str) -> str:
        """Вызов локальной модели Ollama (если установлена)"""
        try:
            import requests
            
            ollama_url = os.getenv('OLLAMA_URL', 'http://localhost:11434/api/generate')
            model_name = os.getenv('OLLAMA_MODEL', 'llama2')
            
            full_prompt = f"{context}\n\nПользователь: {user_message}\n\nАссистент:"
            
            response = requests.post(
                ollama_url,
                json={
                    "model": model_name,
                    "prompt": full_prompt,
                    "stream": False
                },
                timeout=30
            )
            
            if response.status_code == 200:
                return response.json().get('response', 'Извините, не удалось получить ответ.')
            else:
                return f"Ошибка Ollama: {response.status_code}"
        except ImportError:
            return "Библиотека requests не установлена."
        except Exception as e:
            return f"Ошибка при обращении к Ollama: {str(e)}"
    
    def _process_smart_query(
        self, 
        user_message: str, 
        schedule_data: Optional[Dict] = None
    ) -> Optional[str]:
        """
        Обрабатывает умные запросы локально (без LLM) для быстрых ответов
        
        Returns:
            Ответ или None, если нужен LLM
        """
        message_lower = user_message.lower()
        
        # Поиск следующего занятия по предмету
        if any(word in message_lower for word in ['когда', 'следующ', 'ближайш', 'следующая пара']):
            if schedule_data and 'lessons' in schedule_data:
                # Извлекаем название предмета из запроса
                subject_keywords = ['по', 'предмет', 'матан', 'математик', 'физик', 'химия']
                for keyword in subject_keywords:
                    if keyword in message_lower:
                        # Пытаемся найти предмет в запросе
                        words = message_lower.split()
                        for i, word in enumerate(words):
                            if word in ['по', 'предмет'] and i + 1 < len(words):
                                subject_query = ' '.join(words[i+1:])
                                next_lesson = self.analytics.find_next_lesson(
                                    schedule_data['lessons'], 
                                    subject_query
                                )
                                if next_lesson:
                                    day_name = next_lesson.get('day_name', '')
                                    lesson_num = next_lesson.get('lesson_number', '')
                                    subject = next_lesson.get('subject', '')
                                    classroom = next_lesson.get('classroom', '')
                                    days_until = next_lesson.get('days_until', 0)
                                    
                                    if days_until == 0:
                                        return f"Следующая пара по {subject} сегодня, {lesson_num} пара. Аудитория: {classroom}"
                                    elif days_until == 1:
                                        return f"Следующая пара по {subject} завтра ({day_name}), {lesson_num} пара. Аудитория: {classroom}"
                                    else:
                                        return f"Следующая пара по {subject} через {days_until} дня(ей) ({day_name}), {lesson_num} пара. Аудитория: {classroom}"
        
        # Вопросы о нагрузке
        if any(word in message_lower for word in ['нагрузка', 'сколько пар', 'сколько занятий']):
            if schedule_data and 'lessons' in schedule_data:
                analytics = self.analytics.calculate_weekly_load(schedule_data['lessons'])
                return (
                    f"Нагрузка на неделю:\n"
                    f"• Всего занятий: {analytics['total_lessons']}\n"
                    f"• Всего часов: {analytics['total_hours']}\n"
                    f"• В среднем в день: {analytics['average_per_day']} часов"
                )
        
        return None
    
    def get_response(
        self, 
        user_message: str, 
        group_code: Optional[str] = None,
        schedule_data: Optional[Dict] = None,
        exams_data: Optional[List[Dict]] = None,
        use_cache: bool = True
    ) -> str:
        """
        Получить умный ответ от AI на вопрос пользователя
        
        Args:
            user_message: Вопрос пользователя
            group_code: Код группы (опционально)
            schedule_data: Данные расписания для контекста (опционально)
            exams_data: Данные экзаменов для приоритетов (опционально)
            use_cache: Использовать ли кэш
        
        Returns:
            Ответ AI
        """
        if not user_message or not user_message.strip():
            return "Пожалуйста, задайте вопрос о расписании."
        
        # Проверяем кэш для простых запросов
        if use_cache:
            cache_key = self._get_cache_key(user_message, group_code)
            cached_response = self._check_cache(cache_key)
            if cached_response:
                return cached_response
        
        # Пытаемся обработать локально (быстрый ответ)
        local_response = self._process_smart_query(user_message, schedule_data)
        if local_response:
            if use_cache:
                self._save_to_cache(cache_key, local_response)
            return local_response
        
        # Вычисляем аналитику для контекста
        analytics_data = None
        if schedule_data and 'lessons' in schedule_data:
            lessons = schedule_data['lessons']
            weekly_load = self.analytics.calculate_weekly_load(lessons)
            hour_balance = self.analytics.calculate_hour_balance(lessons)
            priorities = self.analytics.identify_priorities(lessons, exams_data)
            
            analytics_data = {
                'weekly_load': weekly_load,
                'hour_balance': hour_balance,
                'priorities': {
                    'high_priority_count': len(priorities['high_priority']),
                    'upcoming_exams': priorities['upcoming_exams']
                }
            }
        
        # Получаем расширенный контекст
        context = self._get_schedule_context(
            group_code=group_code,
            schedule_data=schedule_data,
            analytics_data=analytics_data
        )
        
        # Вызываем LLM для сложных запросов
        if self.provider == 'gemini':
            response = self._call_gemini(user_message, context)
        elif self.provider == 'ollama':
            response = self._call_ollama(user_message, context)
        else:  # openai по умолчанию
            response = self._call_openai(user_message, context)
        
        # Сохраняем в кэш
        if use_cache:
            self._save_to_cache(cache_key, response)
        
        return response
    
    def is_configured(self) -> bool:
        """Проверяет, настроен ли AI сервис"""
        return bool(self.api_key)

