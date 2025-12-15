// ============================================
// КОД ДЛЯ УЗЛА "Code - Process Response" (CODE1)
// ВЕРСИЯ С ПОДДЕРЖКОЙ GOOGLE DRIVE
// ============================================
// Этот код обрабатывает ответ от API или yt-dlp
// и подготавливает данные для загрузки на Google Drive

// Обрабатываем ответ от API или yt-dlp
const data = $input.item.json || $json;

// Получаем chat_id
const baseChatId = $json.chatId || $json.chat_id || data.chat_id || data.chatId;

// Пропускаем явные ошибки
if (data.error === true || data.error === 'true' || data.success === false || data.success === 'false' || (data.statusCode && data.statusCode >= 400)) {
  return {
    json: {
      error: true,
      message: data.message || data.error || 'Ошибка при получении контента',
      chatId: baseChatId
    }
  };
}

// Определяем успешность ответа
const isSuccess = data.success === true || data.success === 'true' || data.status === 'success' || data.statusCode === 200 || (data.statusCode >= 200 && data.statusCode < 300) || data.file_path || data.file_url || data.url || data.download_url || (data.data && (data.data.file_path || data.data.url));

if (!isSuccess) {
  return {
    json: {
      error: true,
      message: 'Не удалось получить медиа-файл',
      chatId: baseChatId
    }
  };
}

// Извлекаем путь к файлу
const filePath = data.file_path || data.file_url || data.url || data.download_url || (data.data && (data.data.file_path || data.data.url)) || null;

// Извлекаем метаданные
let metadata = data.metadata || {};
if (!metadata && data.data) {
  if (data.data.metadata) {
    metadata = data.data.metadata;
  } else if (!data.data.file_path) {
    metadata = data.data;
  }
}
if (!metadata) {
  metadata = {};
}

// Определяем тип контента
let contentType = data.type || data.file_type || data.media_type || data.content_type || null;

// Определяем по расширению файла
if (!contentType && filePath) {
  const extension = filePath.split('.').pop().toLowerCase().split('?')[0];
  if (extension === 'mp4' || extension === 'webm' || extension === 'mov' || extension === 'avi' || extension === 'mkv' || extension === 'flv' || extension === 'm4v') {
    contentType = 'video';
  } else if (extension === 'mp3' || extension === 'm4a' || extension === 'ogg' || extension === 'wav' || extension === 'flac' || extension === 'aac') {
    contentType = 'audio';
  }
}

// Получаем chat_id
const chatId = data.chat_id || data.chatId || baseChatId || null;

// Извлекаем текст
const textContent = data.text || data.message || data.content || metadata.text || null;

// Определяем платформу из входящих данных
const platform = data.platform || data.service || $json.platform || 'unknown';

// Приоритет: видео > аудио > текст
let isVideo = filePath && (contentType === 'video' || filePath.toLowerCase().indexOf('.mp4') >= 0 || filePath.toLowerCase().indexOf('.webm') >= 0 || filePath.toLowerCase().indexOf('video') >= 0);
let isAudio = filePath && (contentType === 'audio' || filePath.toLowerCase().indexOf('.mp3') >= 0 || filePath.toLowerCase().indexOf('.m4a') >= 0 || filePath.toLowerCase().indexOf('audio') >= 0);

// Для YouTube/Instagram/TikTok - обычно видео
if (filePath && (platform === 'youtube' || platform === 'instagram' || platform === 'tiktok')) {
  isVideo = true;
  contentType = 'video';
}

// Для Yandex Music - обычно аудио
if (filePath && platform === 'yandex_music') {
  isAudio = true;
  contentType = 'audio';
}

// Формируем название файла для Google Drive
let fileName = 'media';
if (filePath) {
  const pathParts = filePath.split('\\').pop().split('/').pop();
  fileName = pathParts.replace(/\.[^/.]+$/, '') || 'media';
  // Очищаем название от недопустимых символов для Google Drive
  fileName = fileName.replace(/[<>:"/\\|?*]/g, '_').substring(0, 200);
}

// Если файл есть, возвращаем для загрузки на Google Drive
if (filePath) {
  if (isVideo) {
    return {
      json: {
        file_path: filePath,
        chat_id: chatId,
        metadata: {
          title: fileName,
          platform: platform,
          original_title: metadata.title || fileName
        },
        content_type: 'video',
        has_video: true,
        has_audio: false,
        has_text: false,
        // Флаги для Google Drive
        upload_to_drive: true,
        drive_file_name: fileName + '.mp4',
        drive_mime_type: 'video/mp4'
      }
    };
  }
  
  if (isAudio) {
    return {
      json: {
        file_path: filePath,
        chat_id: chatId,
        metadata: {
          title: fileName,
          platform: platform,
          original_title: metadata.title || fileName,
          artist: metadata.artist || ''
        },
        content_type: 'audio',
        has_video: false,
        has_audio: true,
        has_text: false,
        // Флаги для Google Drive
        upload_to_drive: true,
        drive_file_name: fileName + '.mp3',
        drive_mime_type: 'audio/mpeg'
      }
    };
  }
  
  // Если файл есть, но тип не определен - считаем видео по умолчанию
  return {
    json: {
      file_path: filePath,
      chat_id: chatId,
      metadata: {
        title: fileName,
        platform: platform,
        original_title: metadata.title || fileName
      },
      content_type: 'video',
      has_video: true,
      has_audio: false,
      has_text: false,
      // Флаги для Google Drive
      upload_to_drive: true,
      drive_file_name: fileName + '.mp4',
      drive_mime_type: 'video/mp4'
    }
  };
}

// Текст ТОЛЬКО если НЕТ файла вообще
if (textContent && !filePath) {
  // ВАЖНО: Проверяем что chat_id есть
  if (!chatId || chatId === null || chatId === undefined) {
    return {
      json: {
        error: true,
        message: 'Не удалось определить chat_id для отправки сообщения',
        chatId: baseChatId
      }
    };
  }
  
  // ВАЖНО: Проверяем что текст не пустой и является строкой
  if (!textContent || typeof textContent !== 'string' || textContent.trim().length === 0) {
    return {
      json: {
        error: true,
        message: 'Текст для отправки пустой',
        chatId: chatId
      }
    };
  }
  
  // Ограничиваем длину текста до 4096 символов (лимит Telegram)
  let textToSend = String(textContent).trim();
  if (textToSend.length > 4096) {
    textToSend = textToSend.substring(0, 4093) + '...';
  }
  
  // Убеждаемся что chat_id это число или строка
  const finalChatId = String(chatId);
  
  return {
    json: {
      text_content: textToSend,
      text: textToSend,
      chat_id: finalChatId,
      chatId: finalChatId, // Дублируем для совместимости
      metadata: metadata,
      content_type: 'text',
      has_video: false,
      has_audio: false,
      has_text: true,
      // Не загружаем текст на Google Drive
      upload_to_drive: false
    }
  };
}

// Если ничего не определено
return {
  json: {
    file_path: filePath,
    chat_id: chatId,
    metadata: metadata,
    content_type: contentType || 'unknown',
    has_video: false,
    has_audio: false,
    has_text: false,
    error: true,
    message: 'Не удалось определить тип контента',
    upload_to_drive: false
  }
};

