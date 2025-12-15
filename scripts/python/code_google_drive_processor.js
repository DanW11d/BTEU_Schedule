// ============================================
// КОД ДЛЯ УЗЛА "Code - Process Google Drive"
// ============================================
// Этот узел идет ПОСЛЕ узла "Google Drive - Upload"
// Обрабатывает результат загрузки и формирует сообщение

// Получаем данные от Google Drive
const driveData = $input.item.json || $json;

// Получаем исходные данные (из узла "Code - Process Response")
// Используем $() для получения данных из предыдущего узла
let originalData = {};
try {
  // Пытаемся получить данные из предыдущего узла
  const previousNode = $('Code - Process Response');
  if (previousNode && previousNode.item && previousNode.item.json) {
    originalData = previousNode.item.json;
  }
} catch (e) {
  // Если не удалось, используем текущие данные
  originalData = $json;
}

// Получаем ID файла и ссылку
const fileId = driveData.id || driveData.fileId || driveData.file?.id || null;
const shareableLink = driveData.webViewLink || driveData.webContentLink || 
                      driveData.web_link || 
                      (fileId ? `https://drive.google.com/file/d/${fileId}/view` : null);

// Получаем chat_id из исходных данных
const chatId = originalData.chat_id || originalData.chatId || driveData.chat_id || null;

// Получаем тип контента
const contentType = originalData.content_type || driveData.content_type || 'file';

// Получаем название
const title = originalData.metadata?.title || 
              originalData.metadata?.original_title || 
              driveData.name || 
              'Медиа файл';

// Получаем путь к локальному файлу (для прямой отправки)
const localFilePath = originalData.file_path || null;

// Формируем сообщение для отправки
let message = '';
let emoji = '📁';

if (contentType === 'video') {
  emoji = '📹';
  message = `✅ Видео успешно загружено на Google Drive!\n\n${emoji} ${title}\n\n🔗 Скачать: ${shareableLink || 'Ссылка не доступна'}`;
} else if (contentType === 'audio') {
  emoji = '🎵';
  const artist = originalData.metadata?.artist || '';
  const artistText = artist ? `\n👤 Исполнитель: ${artist}` : '';
  message = `✅ Аудио успешно загружено на Google Drive!\n\n${emoji} ${title}${artistText}\n\n🔗 Скачать: ${shareableLink || 'Ссылка не доступна'}`;
} else {
  emoji = '📁';
  message = `✅ Файл успешно загружен на Google Drive!\n\n${emoji} ${title}\n\n🔗 Скачать: ${shareableLink || 'Ссылка не доступна'}`;
}

// Настройки отправки
// Можно выбрать: отправлять файл напрямую, только ссылку, или оба варианта
const sendFile = true; // Отправлять файл напрямую в Telegram
const sendLink = true; // Отправлять ссылку на Google Drive

// Возвращаем результат
return {
  json: {
    chat_id: chatId,
    chatId: chatId, // Дублируем для совместимости
    
    // Для отправки файла
    file_path: localFilePath,
    content_type: contentType,
    has_video: originalData.has_video || false,
    has_audio: originalData.has_audio || false,
    has_text: false,
    
    // Для отправки ссылки
    text: message,
    text_content: message,
    drive_link: shareableLink,
    drive_file_id: fileId,
    
    // Флаги отправки
    send_file: sendFile && localFilePath ? true : false,
    send_link: sendLink && shareableLink ? true : false,
    
    // Метаданные
    metadata: {
      title: title,
      platform: originalData.metadata?.platform || 'unknown',
      drive_file_id: fileId,
      drive_link: shareableLink
    }
  }
};

