// ============================================
// КОД ДЛЯ УЗЛА "Code - Process Response" (CODE1)
// ============================================
// Скопируйте этот код в узел Code в n8n
// Название узла: "Code - Process Response"
// Этот узел идет ПОСЛЕ HTTP Request узлов

// Обрабатываем ответ от API (теперь только один ответ)
const data = $input.item.json || $json;

// Получаем chat_id из исходных данных (они должны быть переданы от парсинга)
const baseChatId = $json.chatId || $json.chat_id || data.chat_id || data.chatId;

// ВАЖНО: Сохраняем platform из исходных данных ДО обработки ошибок
const platform = $json.platform || data.platform || null;

// Обрабатываем ошибки из HTTP Request (когда данные приходят через выход "Error")
// Проверяем ошибку в разных местах структуры данных
const errorCode = data.error && data.error.code ? data.error.code : (data.code || null);
const errorMessage = data.error && data.error.message ? data.error.message : (data.message || null);

if (errorCode || errorMessage || data.error) {
  // Если это ошибка подключения, но мы знаем платформу - определяем тип контента
  if (errorCode === 'EHOSTUNREACH' || errorCode === 'ECONNREFUSED' || errorCode === 'ETIMEDOUT' || errorCode === 'ENOTFOUND') {
    // Определяем тип контента по платформе
    const isVideoPlatform = platform === 'youtube' || platform === 'instagram' || platform === 'tiktok';
    const isAudioPlatform = platform === 'yandex_music';
    
    return {
      json: {
        error: true,
        message: 'Сервер API недоступен. Проверьте, что сервер запущен и доступен по адресу http://172.18.0.7:6022',
        chatId: baseChatId,
        platform: platform,
        // Сохраняем информацию о типе контента для отладки
        content_type: isVideoPlatform ? 'video' : (isAudioPlatform ? 'audio' : 'unknown'),
        has_video: isVideoPlatform,
        has_audio: isAudioPlatform,
        has_text: false
      }
    };
  }
  
  // Общая обработка ошибок подключения
  if (errorMessage && (errorMessage.indexOf('connect') >= 0 || errorMessage.indexOf('ECONN') >= 0 || errorMessage.indexOf('EHOST') >= 0)) {
    // Определяем тип контента по платформе
    const isVideoPlatform = platform === 'youtube' || platform === 'instagram' || platform === 'tiktok';
    const isAudioPlatform = platform === 'yandex_music';
    
    return {
      json: {
        error: true,
        message: 'Не удалось подключиться к серверу API. Проверьте настройки подключения.',
        chatId: baseChatId,
        platform: platform,
        content_type: isVideoPlatform ? 'video' : (isAudioPlatform ? 'audio' : 'unknown'),
        has_video: isVideoPlatform,
        has_audio: isAudioPlatform,
        has_text: false
      }
    };
  }
  
  // Если есть объект error, но код не определен
  if (data.error && typeof data.error === 'object') {
    return {
      json: {
        error: true,
        message: errorMessage || 'Ошибка при получении контента от API сервера',
        chatId: baseChatId,
        platform: platform,
        has_video: false,
        has_audio: false,
        has_text: false
      }
    };
  }
}

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

// Извлекаем путь к файлу (ПРИОРИТЕТ - если есть filePath, это медиа-файл!)
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

// Платформа уже получена выше (при обработке ошибок)
// Если не была получена, получаем здесь
if (!platform) {
  const platformFromData = $json.platform || data.platform || null;
}

// Получаем chat_id
const chatId = data.chat_id || data.chatId || baseChatId || null;

// Извлекаем текст (только для информации, НЕ для отправки если есть файл)
const textContent = data.text || data.message || data.content || metadata.text || null;

// КРИТИЧНО: Если есть filePath - это МЕДИА-ФАЙЛ, а не текст!
// Приоритет: файл > все остальное
// ВАЖНО: Также проверяем платформу, даже если filePath это URL (не файл)
if (filePath || platform) {
  // Определяем тип по расширению, платформе или содержимому
  const lowerPath = filePath ? filePath.toLowerCase() : '';
  
  // Определяем тип контента по платформе (ПРИОРИТЕТ!)
  const isVideoPlatform = platform === 'youtube' || platform === 'instagram' || platform === 'tiktok';
  const isAudioPlatform = platform === 'yandex_music';
  
  // Определяем тип по расширению файла
  const isVideo = contentType === 'video' || 
                  (filePath && (
                    lowerPath.indexOf('.mp4') >= 0 || 
                    lowerPath.indexOf('.webm') >= 0 || 
                    lowerPath.indexOf('.mov') >= 0 ||
                    lowerPath.indexOf('video') >= 0
                  )) ||
                  isVideoPlatform; // ПРИОРИТЕТ: платформа определяет тип!
  
  const isAudio = contentType === 'audio' || 
                  (filePath && (
                    lowerPath.indexOf('.mp3') >= 0 || 
                    lowerPath.indexOf('.m4a') >= 0 || 
                    lowerPath.indexOf('.ogg') >= 0 ||
                    lowerPath.indexOf('audio') >= 0
                  )) ||
                  isAudioPlatform; // ПРИОРИТЕТ: платформа определяет тип!
  
  if (isVideo) {
    return {
      json: {
        file_path: filePath,
        chat_id: chatId,
        metadata: metadata,
        content_type: 'video',
        has_video: true,
        has_audio: false,
        has_text: false
      }
    };
  }
  
  if (isAudio) {
    return {
      json: {
        file_path: filePath,
        chat_id: chatId,
        metadata: metadata,
        content_type: 'audio',
        has_video: false,
        has_audio: true,
        has_text: false
      }
    };
  }
  
  // Если файл есть, но тип не определен - считаем видео по умолчанию для YouTube/Instagram/TikTok
  if (platform === 'youtube' || platform === 'instagram' || platform === 'tiktok') {
    return {
      json: {
        file_path: filePath || null,
        chat_id: chatId,
        metadata: metadata,
        content_type: 'video',
        has_video: true,
        has_audio: false,
        has_text: false,
        platform: platform
      }
    };
  }
  
  // Если файл есть, но тип не определен - считаем видео по умолчанию
  return {
    json: {
      file_path: filePath || null,
      chat_id: chatId,
      metadata: metadata,
      content_type: 'video',
      has_video: true,
      has_audio: false,
      has_text: false,
      platform: platform
    }
  };
}

// ВАЖНО: Если filePath нет, но платформа определена - определяем тип по платформе
if (!filePath && platform) {
  const isVideoPlatform = platform === 'youtube' || platform === 'instagram' || platform === 'tiktok';
  const isAudioPlatform = platform === 'yandex_music';
  
  if (isVideoPlatform) {
    return {
      json: {
        file_path: null,
        chat_id: chatId,
        metadata: metadata,
        content_type: 'video',
        has_video: true,
        has_audio: false,
        has_text: false,
        platform: platform,
        error: true,
        message: 'Не удалось получить файл, но платформа определена как видео'
      }
    };
  }
  
  if (isAudioPlatform) {
    return {
      json: {
        file_path: null,
        chat_id: chatId,
        metadata: metadata,
        content_type: 'audio',
        has_video: false,
        has_audio: true,
        has_text: false,
        platform: platform,
        error: true,
        message: 'Не удалось получить файл, но платформа определена как аудио'
      }
    };
  }
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
      has_text: true
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
    message: 'Не удалось определить тип контента'
  }
};

