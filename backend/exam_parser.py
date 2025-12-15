"""
Парсер экзаменов и зачетов с сайта BTEU
Парсит расписание экзаменов и зачетов для конкретных групп
"""
import requests
from bs4 import BeautifulSoup
from datetime import datetime
from typing import List, Dict, Optional
from urllib.parse import urljoin
import re


class ExamScheduleParser:
    """Парсер расписания экзаменов и зачетов с сайта BTEU"""
    
    BASE_URL = "https://bteu.by/studentu/obrazovanie-i-praktika/raspisanie-ekzamenov-i-zachetov"
    
    def __init__(self):
        """Инициализация парсера"""
        self.session = requests.Session()
        self.session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124"
        })
    
    def _get_page_content(self, url: str) -> Optional[BeautifulSoup]:
        """Получает содержимое страницы"""
        try:
            response = self.session.get(url, timeout=10)
            response.raise_for_status()
            return BeautifulSoup(response.text, "html.parser")
        except Exception as e:
            print(f"Ошибка получения страницы {url}: {e}")
            return None
    
    def _parse_date(self, date_str: str) -> Optional[str]:
        """Парсит дату из строки"""
        try:
            # Пытаемся распарсить различные форматы дат
            # Примеры: "15.01.2024", "15 января 2024", "15.01.24"
            date_str = date_str.strip()
            
            # Удаляем лишние пробелы
            date_str = re.sub(r'\s+', ' ', date_str)
            
            # Пытаемся найти дату в формате ДД.ММ.ГГГГ или ДД.ММ.ГГ
            date_match = re.search(r'(\d{1,2})[./](\d{1,2})[./](\d{2,4})', date_str)
            if date_match:
                day, month, year = date_match.groups()
                if len(year) == 2:
                    year = f"20{year}"
                return f"{day.zfill(2)}.{month.zfill(2)}.{year}"
            
            return date_str
        except:
            return date_str
    
    def _parse_time(self, time_str: str) -> Optional[str]:
        """Парсит время из строки"""
        try:
            # Пытаемся найти время в формате ЧЧ:ММ
            time_match = re.search(r'(\d{1,2}):(\d{2})', time_str)
            if time_match:
                hour, minute = time_match.groups()
                return f"{hour.zfill(2)}:{minute}"
            return time_str.strip()
        except:
            return time_str.strip()
    
    def _find_group_section(self, soup: BeautifulSoup, group_code: str) -> Optional[BeautifulSoup]:
        """Находит секцию с расписанием для конкретной группы"""
        if not soup:
            return None
        
        # Ищем все текстовые элементы, содержащие код группы
        group_code_upper = group_code.upper()
        group_code_lower = group_code.lower()
        
        # Ищем заголовки, таблицы, списки с кодом группы
        for element in soup.find_all(['h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p', 'div', 'table', 'ul', 'ol']):
            text = element.get_text()
            if group_code_upper in text.upper() or group_code_lower in text.lower():
                # Возвращаем родительский элемент или сам элемент
                return element.find_parent(['div', 'section', 'article']) or element
        
        return None
    
    def _parse_exam_table(self, table_element) -> List[Dict]:
        """Парсит таблицу с экзаменами/зачетами"""
        exams = []
        
        if not table_element:
            return exams
        
        # Ищем все строки таблицы
        rows = table_element.find_all('tr')
        
        for row in rows[1:]:  # Пропускаем заголовок
            cells = row.find_all(['td', 'th'])
            if len(cells) < 3:  # Минимум: дата, предмет, преподаватель
                continue
            
            try:
                # Пытаемся извлечь данные из ячеек
                date_str = cells[0].get_text(strip=True) if len(cells) > 0 else ""
                subject = cells[1].get_text(strip=True) if len(cells) > 1 else ""
                teacher = cells[2].get_text(strip=True) if len(cells) > 2 else ""
                classroom = cells[3].get_text(strip=True) if len(cells) > 3 else ""
                time_str = cells[4].get_text(strip=True) if len(cells) > 4 else ""
                
                # Определяем тип (экзамен или зачет)
                exam_type = "exam"  # По умолчанию экзамен
                if any(word in subject.lower() for word in ['зачет', 'зачёт', 'zachet']):
                    exam_type = "test"
                elif any(word in subject.lower() for word in ['экзамен', 'exam']):
                    exam_type = "exam"
                
                # Парсим дату и время
                parsed_date = self._parse_date(date_str)
                parsed_time = self._parse_time(time_str)
                
                if parsed_date and subject:
                    exams.append({
                        "date": parsed_date,
                        "subject": subject,
                        "teacher": teacher or "",
                        "classroom": classroom or "",
                        "time": parsed_time or "",
                        "type": exam_type
                    })
            except Exception as e:
                print(f"Ошибка парсинга строки таблицы: {e}")
                continue
        
        return exams
    
    def _parse_exam_list(self, list_element) -> List[Dict]:
        """Парсит список с экзаменами/зачетами"""
        exams = []
        
        if not list_element:
            return exams
        
        # Ищем элементы списка
        items = list_element.find_all(['li', 'div', 'p'])
        
        for item in items:
            text = item.get_text(strip=True)
            if not text:
                continue
            
            try:
                # Пытаемся извлечь данные из текста
                # Формат может быть разным: "15.01.2024 - Предмет - Преподаватель - Аудитория - 10:00"
                parts = re.split(r'[-–—]', text)
                
                if len(parts) >= 2:
                    date_str = parts[0].strip()
                    subject = parts[1].strip()
                    teacher = parts[2].strip() if len(parts) > 2 else ""
                    classroom = parts[3].strip() if len(parts) > 3 else ""
                    time_str = parts[4].strip() if len(parts) > 4 else ""
                    
                    # Определяем тип
                    exam_type = "exam"
                    if any(word in subject.lower() for word in ['зачет', 'зачёт', 'zachet']):
                        exam_type = "test"
                    
                    parsed_date = self._parse_date(date_str)
                    parsed_time = self._parse_time(time_str)
                    
                    if parsed_date and subject:
                        exams.append({
                            "date": parsed_date,
                            "subject": subject,
                            "teacher": teacher,
                            "classroom": classroom,
                            "time": parsed_time,
                            "type": exam_type
                        })
            except Exception as e:
                print(f"Ошибка парсинга элемента списка: {e}")
                continue
        
        return exams
    
    def parse_exams(self, group_code: str, exam_type: str = "exam") -> List[Dict]:
        """
        Парсит экзамены или зачеты для конкретной группы
        
        Args:
            group_code: Код группы (например, "S-41", "П-1")
            exam_type: Тип ("exam" для экзаменов, "test" для зачетов)
        
        Returns:
            Список словарей с данными экзаменов/зачетов
        """
        soup = self._get_page_content(self.BASE_URL)
        if not soup:
            return []
        
        # Находим секцию для группы
        group_section = self._find_group_section(soup, group_code)
        if not group_section:
            print(f"Не найдена секция для группы {group_code}")
            return []
        
        exams = []
        
        # Пытаемся найти таблицу
        table = group_section.find('table')
        if table:
            exams = self._parse_exam_table(table)
        
        # Если таблицы нет, пытаемся найти список
        if not exams:
            list_elem = group_section.find(['ul', 'ol', 'div'])
            if list_elem:
                exams = self._parse_exam_list(list_elem)
        
        # Фильтруем по типу
        if exam_type == "exam":
            exams = [e for e in exams if e.get("type") == "exam"]
        elif exam_type == "test":
            exams = [e for e in exams if e.get("type") == "test"]
        
        return exams
    
    def parse_all(self, group_code: str) -> Dict[str, List[Dict]]:
        """
        Парсит все экзамены и зачеты для группы
        
        Args:
            group_code: Код группы
        
        Returns:
            Словарь с ключами "exams" и "tests"
        """
        return {
            "exams": self.parse_exams(group_code, "exam"),
            "tests": self.parse_exams(group_code, "test")
        }


def main():
    """Тестирование парсера"""
    parser = ExamScheduleParser()
    
    # Тестируем на примере группы
    group_code = "S-41"
    print(f"Парсинг экзаменов для группы {group_code}...")
    exams = parser.parse_exams(group_code, "exam")
    print(f"Найдено экзаменов: {len(exams)}")
    for exam in exams:
        print(f"  - {exam}")
    
    print(f"\nПарсинг зачетов для группы {group_code}...")
    tests = parser.parse_exams(group_code, "test")
    print(f"Найдено зачетов: {len(tests)}")
    for test in tests:
        print(f"  - {test}")


if __name__ == '__main__':
    main()

