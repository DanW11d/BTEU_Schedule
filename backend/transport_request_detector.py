"""
Модуль для определения, является ли письмо заявкой на перевозку
Использует анализ содержимого и структуры письма
"""

import re
import logging
from typing import Dict, Tuple

logger = logging.getLogger(__name__)


class RequestDetector:
    """Класс для определения заявок на перевозку"""
    
    def __init__(self, config: Dict = None):
        """
        Инициализация детектора
        
        Args:
            config: Конфигурация с ключевыми словами и правилами
        """
        self.config = config or {}
        
        # Ключевые слова и фразы, указывающие на заявку
        self.keywords = self.config.get('keywords', [
            'заявка', 'заявка на перевозку', 'задача для водителя',
            'перевозка', 'доставка', 'груз', 'маршрут',
            'забор груза', 'доставка и выгрузка',
            'транспортное средство', 'водитель',
            'адрес', 'контактное лицо', 'телефон'
        ])
        
        # Обязательные разделы для заявки
        self.required_sections = self.config.get('required_sections', [
            r'забор\s+груза',
            r'доставка',
            r'адрес',
            r'информация\s+о\s+грузе|груз'
        ])
        
        # Минимальное количество ключевых слов для определения заявки
        self.min_keywords = self.config.get('min_keywords', 3)
        
        # Минимальное количество обязательных разделов
        self.min_sections = self.config.get('min_sections', 2)
        
        # Дополнительные паттерны для определения заявки
        self.patterns = [
            r'дата\s+и\s+время',  # Дата и время
            r'гос\.?\s*номер',     # Гос. номер
            r'вес.*объем|объем.*вес',  # Вес/Объем
            r'тип\s+кузова',       # Тип кузова
            r'контактное\s+лицо',  # Контактное лицо
        ]
    
    def is_transport_request(self, email_info: Dict) -> Tuple[bool, float, str]:
        """
        Определяет, является ли письмо заявкой на перевозку
        
        Args:
            email_info: Информация о письме (subject, body, sender)
        
        Returns:
            Tuple[bool, float, str]: (является_заявкой, уверенность, причина)
            - является_заявкой: True если это заявка
            - уверенность: от 0.0 до 1.0
            - причина: объяснение решения
        """
        subject = email_info.get('subject', '').lower()
        body = email_info.get('body', '').lower()
        sender = email_info.get('sender', '').lower()
        
        # Объединяем subject и body для анализа
        full_text = f"{subject} {body}"
        
        # Счетчики для определения уверенности
        keyword_matches = 0
        section_matches = 0
        pattern_matches = 0
        
        # Проверка ключевых слов
        for keyword in self.keywords:
            if keyword.lower() in full_text:
                keyword_matches += 1
        
        # Проверка обязательных разделов
        for section_pattern in self.required_sections:
            if re.search(section_pattern, full_text, re.IGNORECASE):
                section_matches += 1
        
        # Проверка дополнительных паттернов
        for pattern in self.patterns:
            if re.search(pattern, full_text, re.IGNORECASE):
                pattern_matches += 1
        
        # Вычисляем уверенность
        # Базовые критерии
        has_keywords = keyword_matches >= self.min_keywords
        has_sections = section_matches >= self.min_sections
        
        # Дополнительные признаки
        has_patterns = pattern_matches >= 2
        has_structure = self._check_structure(body)
        
        # Вычисляем уверенность (0.0 - 1.0)
        confidence = 0.0
        
        # Базовые критерии дают больше веса
        if has_keywords:
            confidence += 0.3
        if has_sections:
            confidence += 0.3
        if has_patterns:
            confidence += 0.2
        if has_structure:
            confidence += 0.2
        
        # Дополнительные бонусы
        if keyword_matches >= 5:
            confidence += 0.1
        if section_matches >= 3:
            confidence += 0.1
        
        # Ограничиваем максимум 1.0
        confidence = min(confidence, 1.0)
        
        # Определяем, является ли это заявкой
        is_request = (has_keywords and has_sections) or (confidence >= 0.7)
        
        # Формируем причину
        reasons = []
        if has_keywords:
            reasons.append(f"найдено ключевых слов: {keyword_matches}")
        if has_sections:
            reasons.append(f"найдено разделов: {section_matches}")
        if has_patterns:
            reasons.append(f"найдено паттернов: {pattern_matches}")
        if has_structure:
            reasons.append("найдена структура заявки")
        
        reason = ", ".join(reasons) if reasons else "недостаточно признаков заявки"
        
        logger.debug(
            f"Анализ письма: заявка={is_request}, уверенность={confidence:.2f}, "
            f"ключевые слова={keyword_matches}, разделы={section_matches}, "
            f"паттерны={pattern_matches}, структура={has_structure}"
        )
        
        return is_request, confidence, reason
    
    def _check_structure(self, body: str) -> bool:
        """
        Проверяет наличие структуры заявки в тексте
        
        Ищет характерные разделы и форматирование
        """
        if not body:
            return False
        
        # Проверяем наличие нумерованных списков или разделов
        structure_indicators = [
            r'1\.\s*забор',           # "1. Забор"
            r'2\.\s*доставка',        # "2. Доставка"
            r'забор\s+груза',         # "Забор груза"
            r'доставка\s+и\s+выгрузка', # "Доставка и выгрузка"
            r'информация\s+о\s+грузе',  # "Информация о грузе"
            r'транспортное\s+средство', # "Транспортное средство"
            r'ключевые\s+требования',    # "Ключевые требования"
        ]
        
        found_indicators = 0
        for indicator in structure_indicators:
            if re.search(indicator, body, re.IGNORECASE):
                found_indicators += 1
        
        # Если найдено минимум 3 индикатора структуры - это похоже на заявку
        return found_indicators >= 3
    
    def get_detection_stats(self, email_info: Dict) -> Dict:
        """
        Возвращает детальную статистику анализа письма
        
        Полезно для отладки и улучшения алгоритма
        """
        subject = email_info.get('subject', '').lower()
        body = email_info.get('body', '').lower()
        full_text = f"{subject} {body}"
        
        stats = {
            'keyword_matches': [],
            'section_matches': [],
            'pattern_matches': [],
            'structure_detected': False
        }
        
        # Найденные ключевые слова
        for keyword in self.keywords:
            if keyword.lower() in full_text:
                stats['keyword_matches'].append(keyword)
        
        # Найденные разделы
        for section_pattern in self.required_sections:
            match = re.search(section_pattern, full_text, re.IGNORECASE)
            if match:
                stats['section_matches'].append(section_pattern)
        
        # Найденные паттерны
        for pattern in self.patterns:
            match = re.search(pattern, full_text, re.IGNORECASE)
            if match:
                stats['pattern_matches'].append(pattern)
        
        # Структура
        stats['structure_detected'] = self._check_structure(body)
        
        return stats


# Пример использования
if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    
    detector = RequestDetector()
    
    # Тестовые примеры
    test_emails = [
        {
            'subject': 'Заявка на перевозку',
            'body': '''
            ЗАДАЧА ДЛЯ ВОДИТЕЛЯ
            
            Маршрут и график:
            
            1. Забор груза:
               · Дата и время: 20 ноября 2025 года.
               · Адрес: Московская область, ул. Ленина, д. 93.
               · Контактное лицо: Иванов, тел.: 8-999-123-45-67.
            
            2. Доставка и выгрузка:
               · Дата и время: 20 ноября 2025 года.
               · Адрес: Московская область, дом 36.
            
            Информация о грузе:
            · Наименование: Мебель.
            · Вес/Объем: 20 тонн / 1 м³.
            · Тип кузова/погрузки: Тент, задняя.
            
            Транспортное средство и водитель:
            · Автомобиль: Ford, гос. номер: A992X0750.
            · Водитель: Петров Петр.
            ''',
            'sender': 'logistics@company.com'
        },
        {
            'subject': 'Обычное письмо',
            'body': 'Привет, как дела?',
            'sender': 'friend@mail.com'
        },
        {
            'subject': 'Вопрос по доставке',
            'body': 'Когда будет доставка?',
            'sender': 'client@mail.com'
        }
    ]
    
    for i, email in enumerate(test_emails, 1):
        is_request, confidence, reason = detector.is_transport_request(email)
        print(f"\n{'='*60}")
        print(f"Письмо {i}: {email['subject']}")
        print(f"Является заявкой: {is_request}")
        print(f"Уверенность: {confidence:.2%}")
        print(f"Причина: {reason}")
        
        if is_request:
            stats = detector.get_detection_stats(email)
            print(f"Статистика: {stats}")

