"""
Модуль для обработки входящих email с заявками на перевозку
Поддерживает Gmail API и IMAP
"""

import imaplib
import email
from email.header import decode_header
from email.mime.text import MIMEText
import smtplib
from typing import List, Dict, Optional, Tuple
import logging
from datetime import datetime
import re

logger = logging.getLogger(__name__)


class EmailHandler:
    """Обработчик email для получения и отправки писем"""
    
    def __init__(self, config: Dict):
        """
        Инициализация обработчика email
        
        Args:
            config: Словарь с настройками:
                - email: адрес почты
                - password: пароль или app password
                - imap_server: IMAP сервер (например, 'imap.gmail.com')
                - imap_port: порт IMAP (обычно 993)
                - smtp_server: SMTP сервер (например, 'smtp.gmail.com')
                - smtp_port: порт SMTP (обычно 465 или 587)
                - use_ssl: использовать SSL (True/False)
        """
        self.email = config.get('email')
        self.password = config.get('password')
        self.imap_server = config.get('imap_server', 'imap.gmail.com')
        self.imap_port = config.get('imap_port', 993)
        self.smtp_server = config.get('smtp_server', 'smtp.gmail.com')
        self.smtp_port = config.get('smtp_port', 465)
        self.use_ssl = config.get('use_ssl', True)
        self.use_tls = config.get('use_tls', False)
        
        # Фильтры для поиска заявок (опционально, можно отключить для проверки всех писем)
        self.subject_filter = config.get('subject_filter', None)  # None = проверять все письма
        self.sender_filter = config.get('sender_filter', None)
        self.label_filter = config.get('label_filter', None)
        self.check_all_emails = config.get('check_all_emails', True)  # Проверять все непрочитанные
    
    def connect_imap(self) -> Optional[imaplib.IMAP4_SSL]:
        """Подключение к IMAP серверу"""
        try:
            if self.use_ssl:
                mail = imaplib.IMAP4_SSL(self.imap_server, self.imap_port)
            else:
                mail = imaplib.IMAP4(self.imap_server, self.imap_port)
                if self.use_tls:
                    mail.starttls()
            
            mail.login(self.email, self.password)
            logger.info(f"Успешное подключение к IMAP: {self.imap_server}")
            return mail
        except Exception as e:
            logger.error(f"Ошибка подключения к IMAP: {e}")
            return None
    
    def get_unread_emails(self, limit: int = 10) -> List[Dict]:
        """
        Получить список непрочитанных писем
        
        Args:
            limit: максимальное количество писем для обработки
            
        Returns:
            Список словарей с информацией о письмах
        """
        mail = self.connect_imap()
        if not mail:
            return []
        
        emails = []
        try:
            mail.select("inbox")
            
            # Формируем поисковый запрос
            # Если check_all_emails=True, проверяем все непрочитанные письма
            # Детектор заявок определит, является ли письмо заявкой
            search_query = 'UNSEEN'
            if not self.check_all_emails:
                # Используем фильтры только если не проверяем все письма
                if self.subject_filter:
                    search_query += f' SUBJECT "{self.subject_filter}"'
                if self.sender_filter:
                    search_query += f' FROM "{self.sender_filter}"'
            
            status, messages = mail.search(None, search_query)
            
            if status != 'OK':
                logger.warning("Не удалось выполнить поиск писем")
                mail.close()
                mail.logout()
                return []
            
            email_ids = messages[0].split()
            # Обрабатываем только последние N писем
            email_ids = email_ids[-limit:] if len(email_ids) > limit else email_ids
            
            for num in email_ids:
                try:
                    status, data = mail.fetch(num, '(RFC822)')
                    if status != 'OK':
                        continue
                    
                    email_message = email.message_from_bytes(data[0][1])
                    
                    # Извлекаем информацию о письме
                    subject = self._decode_header(email_message["Subject"])
                    sender = self._decode_header(email_message["From"])
                    date = email_message["Date"]
                    
                    # Извлекаем текст письма
                    body = self._extract_text(email_message)
                    
                    email_info = {
                        'id': num.decode(),
                        'subject': subject,
                        'sender': sender,
                        'date': date,
                        'body': body,
                        'raw_message': email_message
                    }
                    
                    emails.append(email_info)
                    logger.info(f"Найдено письмо: {subject} от {sender}")
                    
                except Exception as e:
                    logger.error(f"Ошибка обработки письма {num}: {e}")
                    continue
            
            mail.close()
            mail.logout()
            
        except Exception as e:
            logger.error(f"Ошибка при получении писем: {e}")
            try:
                mail.close()
                mail.logout()
            except:
                pass
        
        return emails
    
    def mark_as_read(self, email_id: str) -> bool:
        """Пометить письмо как прочитанное"""
        mail = self.connect_imap()
        if not mail:
            return False
        
        try:
            mail.select("inbox")
            mail.store(email_id.encode(), '+FLAGS', '\\Seen')
            mail.close()
            mail.logout()
            logger.info(f"Письмо {email_id} помечено как прочитанное")
            return True
        except Exception as e:
            logger.error(f"Ошибка при пометке письма как прочитанного: {e}")
            try:
                mail.close()
                mail.logout()
            except:
                pass
            return False
    
    def archive_email(self, email_id: str) -> bool:
        """Переместить письмо в архив"""
        mail = self.connect_imap()
        if not mail:
            return False
        
        try:
            mail.select("inbox")
            # Создаем папку Archive, если её нет
            try:
                mail.create('Archive')
            except:
                pass
            
            mail.copy(email_id.encode(), 'Archive')
            mail.store(email_id.encode(), '+FLAGS', '\\Deleted')
            mail.expunge()
            mail.close()
            mail.logout()
            logger.info(f"Письмо {email_id} перемещено в архив")
            return True
        except Exception as e:
            logger.error(f"Ошибка при архивировании письма: {e}")
            try:
                mail.close()
                mail.logout()
            except:
                pass
            return False
    
    def send_email(self, to: str, subject: str, body: str, is_html: bool = False) -> bool:
        """
        Отправить email
        
        Args:
            to: адрес получателя
            subject: тема письма
            body: тело письма
            is_html: является ли тело HTML
            
        Returns:
            True если отправка успешна, False иначе
        """
        try:
            msg = MIMEText(body, 'html' if is_html else 'plain', 'utf-8')
            msg['Subject'] = subject
            msg['From'] = self.email
            msg['To'] = to
            
            if self.use_ssl:
                server = smtplib.SMTP_SSL(self.smtp_server, self.smtp_port)
            else:
                server = smtplib.SMTP(self.smtp_server, self.smtp_port)
                if self.use_tls:
                    server.starttls()
            
            server.login(self.email, self.password)
            server.send_message(msg)
            server.quit()
            
            logger.info(f"Email отправлен: {to} - {subject}")
            return True
        except Exception as e:
            logger.error(f"Ошибка отправки email: {e}")
            return False
    
    def _decode_header(self, header: Optional[str]) -> str:
        """Декодировать заголовок email"""
        if not header:
            return ""
        
        try:
            decoded_parts = decode_header(header)
            decoded_str = ""
            for part, encoding in decoded_parts:
                if isinstance(part, bytes):
                    if encoding:
                        decoded_str += part.decode(encoding)
                    else:
                        decoded_str += part.decode('utf-8', errors='ignore')
                else:
                    decoded_str += part
            return decoded_str
        except Exception as e:
            logger.warning(f"Ошибка декодирования заголовка: {e}")
            return str(header) if header else ""
    
    def _extract_text(self, email_message) -> str:
        """Извлечь текстовое содержимое из email"""
        body = ""
        
        if email_message.is_multipart():
            for part in email_message.walk():
                content_type = part.get_content_type()
                content_disposition = str(part.get("Content-Disposition"))
                
                # Пропускаем вложения
                if "attachment" in content_disposition:
                    continue
                
                # Предпочитаем text/plain, но используем text/html если нет plain
                if content_type == "text/plain":
                    try:
                        body = part.get_payload(decode=True).decode('utf-8', errors='ignore')
                        break
                    except:
                        pass
                elif content_type == "text/html" and not body:
                    try:
                        # Извлекаем текст из HTML (простой вариант)
                        html_body = part.get_payload(decode=True).decode('utf-8', errors='ignore')
                        # Убираем HTML теги (простой regex)
                        body = re.sub(r'<[^>]+>', '', html_body)
                        body = re.sub(r'\s+', ' ', body).strip()
                    except:
                        pass
        else:
            # Простое письмо без multipart
            try:
                body = email_message.get_payload(decode=True).decode('utf-8', errors='ignore')
            except:
                body = str(email_message.get_payload())
        
        return body.strip()


# Пример использования
if __name__ == "__main__":
    # Настройка логирования
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Пример конфигурации
    config = {
        'email': 'your_email@gmail.com',
        'password': 'your_app_password',
        'imap_server': 'imap.gmail.com',
        'imap_port': 993,
        'smtp_server': 'smtp.gmail.com',
        'smtp_port': 465,
        'use_ssl': True,
        'subject_filter': 'Заявка',
        'sender_filter': None  # Можно указать конкретного отправителя
    }
    
    handler = EmailHandler(config)
    
    # Получаем непрочитанные письма
    emails = handler.get_unread_emails(limit=5)
    print(f"Найдено писем: {len(emails)}")
    
    for email_info in emails:
        print(f"\nПисьмо: {email_info['subject']}")
        print(f"От: {email_info['sender']}")
        print(f"Текст: {email_info['body'][:200]}...")

