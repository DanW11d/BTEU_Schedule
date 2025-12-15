// ============================================
// ИСПРАВЛЕННЫЙ КОД ДЛЯ УЗЛА "Code - Process Response"
// ============================================
// Скопируйте ВЕСЬ этот код в узел Code в n8n
// Название узла: "Process Response"
// Режим: "Run Once for All Items"

// Получаем все элементы из входных данных
const items = $input.all();

// Если нет данных, возвращаем ошибку
if (!items || items.length === 0) {
  return [{
    json: {
      error: true,
      message: 'Нет данных для обработки',
      has_video: false,
      has_audio: false,
      has_text: false
    }
  }];
}

// Обрабатываем каждый элемент
const results = [];

for (let i = 0; i < items.length; i++) {
  const item = items[i].json || items[i];
  const result = {
    error: false,
    has_video: false,
    has_audio: false,
    has_text: false,
    chat_id: null,
    platform: null,
    file_path: null,
    text_content: null
  };

  // ВАЖНО: Сохраняем chat_id и platform из исходных данных ДО обработки ошибок
  // Пытаемся получить из разных источников (предыдущие узлы могут передавать данные)
  result.chat_id = item.chatId || item.chat_id || 
                    (typeof $json !== 'undefined' && ($json.chatId || $json.chat_id)) || null;
  result.platform = item.platform || 
                    (typeof $json !== 'undefined' && $json.platform) || null;

  // ВАЖНО: Проверяем наличие ошибки в самом начале
  if (item.error) {
    // Если это строка "JSON parameter needs to be valid JSON" или другой текст ошибки
    if (typeof item.error === 'string') {
      result.error = true;
      result.message = item.error;
      // chat_id и platform уже получены выше
      results.push({ json: result });
      continue;
    }
    
    // Если это объект ошибки
    if (typeof item.error === 'object') {
      const errorCode = item.error.code || null;
      const errorMessage = item.error.message || item.error || 'Ошибка при получении контента';
      
      // chat_id и platform уже получены выше
      
      // Определяем тип контента по платформе для более точного сообщения
      const isVideoPlatform = result.platform === 'youtube' || result.platform === 'instagram' || result.platform === 'tiktok';
      const isAudioPlatform = result.platform === 'yandex_music';
      
      // Если это ошибка подключения
      if (errorCode === 'EHOSTUNREACH' || errorCode === 'ECONNREFUSED' || errorCode === 'ETIMEDOUT' || errorCode === 'ENOTFOUND') {
        result.error = true;
        result.message = 'Сервер API недоступен. Проверьте, что сервер запущен и доступен по адресу http://172.18.0.7:6022';
        result.content_type = isVideoPlatform ? 'video' : (isAudioPlatform ? 'audio' : 'unknown');
        result.has_video = isVideoPlatform;
        result.has_audio = isAudioPlatform;
        results.push({ json: result });
        continue;
      }
      
      // Общая обработка ошибок подключения
      if (errorMessage && (errorMessage.indexOf('connect') >= 0 || errorMessage.indexOf('ECONN') >= 0 || errorMessage.indexOf('EHOST') >= 0)) {
        result.error = true;
        result.message = 'Не удалось подключиться к серверу API. Проверьте настройки подключения.';
        result.content_type = isVideoPlatform ? 'video' : (isAudioPlatform ? 'audio' : 'unknown');
        result.has_video = isVideoPlatform;
        result.has_audio = isAudioPlatform;
        results.push({ json: result });
        continue;
      }
      
      // Другая ошибка
      result.error = true;
      result.message = errorMessage;
      result.content_type = isVideoPlatform ? 'video' : (isAudioPlatform ? 'audio' : 'unknown');
      result.has_video = isVideoPlatform;
      result.has_audio = isAudioPlatform;
      results.push({ json: result });
      continue;
    }
  }

  // chat_id и platform уже получены выше

  // Проверяем успешность ответа
  const isSuccess = item.success === true || item.success === 'true' || 
                    item.status === 'success' || 
                    item.statusCode === 200 || 
                    (item.statusCode >= 200 && item.statusCode < 300) ||
                    item.file_path || item.file_url || item.url || item.download_url ||
                    (item.data && (item.data.file_path || item.data.url));

  if (!isSuccess) {
    result.error = true;
    result.message = item.message || 'Не удалось получить медиа-файл';
    results.push({ json: result });
    continue;
  }

  // Извлекаем путь к файлу
  const filePath = item.file_path || item.file_url || item.url || item.download_url || 
                   (item.data && (item.data.file_path || item.data.url)) || null;

  // Извлекаем метаданные
  let metadata = item.metadata || {};
  if (!metadata && item.data) {
    if (item.data.metadata) {
      metadata = item.data.metadata;
    } else if (!item.data.file_path) {
      metadata = item.data;
    }
  }
  if (!metadata) {
    metadata = {};
  }

  // Определяем тип контента
  let contentType = item.type || item.file_type || item.media_type || item.content_type || null;

  // Определяем по расширению файла
  if (!contentType && filePath) {
    const extension = filePath.split('.').pop().toLowerCase().split('?')[0];
    if (extension === 'mp4' || extension === 'webm' || extension === 'mov' || extension === 'avi' || extension === 'mkv' || extension === 'flv' || extension === 'm4v') {
      contentType = 'video';
    } else if (extension === 'mp3' || extension === 'm4a' || extension === 'ogg' || extension === 'wav' || extension === 'flac' || extension === 'aac') {
      contentType = 'audio';
    }
  }

  // ВАЖНО: Безопасная проверка для Twitter (если используется старый формат)
  // Проверяем наличие item.videoUrl и item.media БЕЗ обращения к undefined
  let videoUrl = null;
  let textContent = null;

  // Безопасная проверка videoUrl
  if (item.videoUrl) {
    videoUrl = item.videoUrl;
  } else if (item.media && Array.isArray(item.media) && item.media.length > 0) {
    // Проверяем, что media[0] существует перед обращением к videoUrl
    if (item.media[0] && item.media[0].videoUrl) {
      videoUrl = item.media[0].videoUrl;
    }
  }

  // Безопасная проверка text
  if (item.text) {
    textContent = item.text;
  }

  // Если есть filePath - это МЕДИА-ФАЙЛ
  if (filePath || videoUrl) {
    const mediaPath = filePath || videoUrl;
    const lowerPath = mediaPath ? mediaPath.toLowerCase() : '';
    
    // Определяем тип контента по платформе (ПРИОРИТЕТ!)
    const isVideoPlatform = result.platform === 'youtube' || result.platform === 'instagram' || result.platform === 'tiktok' || result.platform === 'twitter';
    const isAudioPlatform = result.platform === 'yandex_music';
    
    // Определяем тип по расширению файла
    const isVideo = contentType === 'video' || 
                    (mediaPath && (
                      lowerPath.indexOf('.mp4') >= 0 || 
                      lowerPath.indexOf('.webm') >= 0 || 
                      lowerPath.indexOf('.mov') >= 0 ||
                      lowerPath.indexOf('video') >= 0
                    )) ||
                    isVideoPlatform; // ПРИОРИТЕТ: платформа определяет тип!
    
    const isAudio = contentType === 'audio' || 
                    (mediaPath && (
                      lowerPath.indexOf('.mp3') >= 0 || 
                      lowerPath.indexOf('.m4a') >= 0 || 
                      lowerPath.indexOf('.ogg') >= 0 ||
                      lowerPath.indexOf('audio') >= 0
                    )) ||
                    isAudioPlatform; // ПРИОРИТЕТ: платформа определяет тип!
    
    if (isVideo) {
      result.file_path = mediaPath;
      result.content_type = 'video';
      result.has_video = true;
      result.metadata = metadata;
      results.push({ json: result });
      continue;
    }
    
    if (isAudio) {
      result.file_path = mediaPath;
      result.content_type = 'audio';
      result.has_audio = true;
      result.metadata = metadata;
      results.push({ json: result });
      continue;
    }
    
    // Если файл есть, но тип не определен - считаем видео по умолчанию для YouTube/Instagram/TikTok/Twitter
    if (isVideoPlatform) {
      result.file_path = mediaPath || null;
      result.content_type = 'video';
      result.has_video = true;
      result.metadata = metadata;
      results.push({ json: result });
      continue;
    }
    
    // Если файл есть, но тип не определен - считаем видео по умолчанию
    result.file_path = mediaPath || null;
    result.content_type = 'video';
    result.has_video = true;
    result.metadata = metadata;
    results.push({ json: result });
    continue;
  }

  // Текст ТОЛЬКО если НЕТ файла вообще
  if (textContent && !filePath && !videoUrl) {
    // Проверяем что chat_id есть
    if (!result.chat_id) {
      result.error = true;
      result.message = 'Не удалось определить chat_id для отправки сообщения';
      results.push({ json: result });
      continue;
    }
    
    // Проверяем что текст не пустой
    if (typeof textContent !== 'string' || textContent.trim().length === 0) {
      result.error = true;
      result.message = 'Текст для отправки пустой';
      results.push({ json: result });
      continue;
    }
    
    // Ограничиваем длину текста до 4096 символов
    let textToSend = String(textContent).trim();
    if (textToSend.length > 4096) {
      textToSend = textToSend.substring(0, 4093) + '...';
    }
    
    result.text_content = textToSend;
    result.text = textToSend;
    result.content_type = 'text';
    result.has_text = true;
    result.metadata = metadata;
    results.push({ json: result });
    continue;
  }

  // Если ничего не определено
  result.error = true;
  result.message = 'Не удалось определить тип контента';
  result.content_type = contentType || 'unknown';
  results.push({ json: result });
}

// Возвращаем результаты
return results;

