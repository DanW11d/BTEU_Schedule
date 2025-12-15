// Упрощенная версия кода для Code1 узла в n8n
// Исправлена ошибка "Unexpected token ':'"

// Получаем все элементы из Merge
const items = $input.all();

// Получаем chat_id
let baseChatId = $json.chatId || $json.chat_id;
if (!baseChatId && items.length > 0) {
  const firstItem = items[0];
  if (firstItem && firstItem.json) {
    baseChatId = firstItem.json.chat_id || firstItem.json.chatId;
  }
}

if (!items || items.length === 0) {
  return {
    json: {
      error: true,
      message: 'Не получено ни одного ответа от серверов',
      chatId: baseChatId
    }
  };
}

// Функция для нормализации данных
function normalizeApiResponse(item) {
  const data = item.json || item;
  
  // Пропускаем ошибки
  if (data.error === true || data.error === 'true' || data.success === false || data.success === 'false') {
    return null;
  }
  
  if (data.statusCode && data.statusCode >= 400) {
    return null;
  }
  
  // Проверяем успешность
  let isSuccess = false;
  if (data.success === true || data.success === 'true' || data.status === 'success' || data.statusCode === 200) {
    isSuccess = true;
  }
  if (data.statusCode >= 200 && data.statusCode < 300) {
    isSuccess = true;
  }
  if (data.file_path || data.file_url || data.url || data.download_url) {
    isSuccess = true;
  }
  if (data.data && data.data.file_path) {
    isSuccess = true;
  }
  if (data.data && data.data.url) {
    isSuccess = true;
  }
  
  if (!isSuccess) {
    return null;
  }
  
  // Извлекаем путь к файлу
  let filePath = data.file_path || data.file_url || data.url || data.download_url || null;
  if (!filePath && data.data) {
    filePath = data.data.file_path || data.data.url || null;
  }
  
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
  
  // Определяем по расширению
  if (!contentType && filePath) {
    const parts = filePath.split('.');
    if (parts.length > 1) {
      const ext = parts[parts.length - 1].toLowerCase().split('?')[0];
      if (ext === 'mp4' || ext === 'webm' || ext === 'mov' || ext === 'avi' || ext === 'mkv' || ext === 'flv' || ext === 'm4v') {
        contentType = 'video';
      } else if (ext === 'mp3' || ext === 'm4a' || ext === 'ogg' || ext === 'wav' || ext === 'flac' || ext === 'aac') {
        contentType = 'audio';
      }
    }
  }
  
  // Получаем chat_id
  const chatId = data.chat_id || data.chatId || baseChatId || null;
  
  // Извлекаем текст
  let textContent = data.text || data.message || data.content || null;
  if (!textContent && metadata && metadata.text) {
    textContent = metadata.text;
  }
  
  return {
    file_path: filePath,
    chat_id: chatId,
    metadata: metadata,
    content_type: contentType,
    text_content: textContent,
    text: textContent,
    has_file: !!filePath,
    priority: filePath ? 1 : (textContent ? 3 : 2)
  };
}

// Фильтруем результаты
const successfulItems = [];
for (let i = 0; i < items.length; i++) {
  const normalized = normalizeApiResponse(items[i]);
  if (normalized && normalized.chat_id) {
    successfulItems.push(normalized);
  }
}

if (successfulItems.length === 0) {
  return {
    json: {
      error: true,
      message: 'Не удалось получить медиа-файл ни с одной платформы',
      chatId: baseChatId
    }
  };
}

// Сортируем по приоритету
successfulItems.sort(function(a, b) {
  if (a.priority !== b.priority) {
    return a.priority - b.priority;
  }
  const typePriority = { 'video': 1, 'audio': 2, 'text': 3, 'unknown': 4 };
  const aType = typePriority[a.content_type] || 4;
  const bType = typePriority[b.content_type] || 4;
  return aType - bType;
});

// Группируем результаты
const videos = [];
const audios = [];
const texts = [];

for (let i = 0; i < successfulItems.length; i++) {
  const item = successfulItems[i];
  const contentType = item.content_type ? item.content_type.toLowerCase() : '';
  const filePath = (item.file_path || '').toLowerCase();
  
  if (item.has_file) {
    if (contentType === 'video' || filePath.indexOf('.mp4') >= 0 || filePath.indexOf('.webm') >= 0 || filePath.indexOf('.mov') >= 0 || filePath.indexOf('video') >= 0) {
      videos.push(item);
    } else if (contentType === 'audio' || filePath.indexOf('.mp3') >= 0 || filePath.indexOf('.m4a') >= 0 || filePath.indexOf('.ogg') >= 0 || filePath.indexOf('audio') >= 0) {
      audios.push(item);
    }
  } else if (item.text_content && !item.has_file) {
    texts.push({
      text_content: item.text_content,
      text: item.text_content,
      chat_id: item.chat_id,
      metadata: item.metadata,
      content_type: 'text'
    });
  }
}

// Приоритет: видео > аудио > текст
if (videos.length > 0) {
  const bestVideo = videos[0];
  return {
    json: {
      file_path: bestVideo.file_path,
      chat_id: bestVideo.chat_id,
      metadata: bestVideo.metadata || {},
      content_type: 'video',
      has_video: true,
      has_audio: audios.length > 0,
      has_text: texts.length > 0
    }
  };
}

if (audios.length > 0) {
  const bestAudio = audios[0];
  return {
    json: {
      file_path: bestAudio.file_path,
      chat_id: bestAudio.chat_id,
      metadata: bestAudio.metadata || {},
      content_type: 'audio',
      has_video: false,
      has_audio: true,
      has_text: texts.length > 0
    }
  };
}

// Текст только если НЕТ медиа-файлов
if (texts.length > 0) {
  const bestText = texts[0];
  if (!bestText.chat_id) {
    return {
      json: {
        error: true,
        message: 'Не удалось определить chat_id для отправки сообщения',
        chatId: baseChatId
      }
    };
  }
  
  const textToSend = bestText.text || bestText.text_content || 'Контент не найден';
  
  return {
    json: {
      text_content: textToSend,
      text: textToSend,
      chat_id: bestText.chat_id,
      metadata: bestText.metadata || {},
      content_type: 'text',
      has_video: false,
      has_audio: false,
      has_text: true
    }
  };
}

// Если ничего не определено
const lastItem = successfulItems[0];
return {
  json: {
    file_path: lastItem.file_path || null,
    chat_id: lastItem.chat_id,
    metadata: lastItem.metadata || {},
    content_type: lastItem.content_type || 'unknown',
    has_video: false,
    has_audio: false,
    has_text: false,
    error: true,
    message: 'Не удалось определить тип контента'
  }
};

