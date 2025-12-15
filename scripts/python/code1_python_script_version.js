// ============================================
// КОД ДЛЯ УЗЛА "Code - Process Response" (CODE1)
// ВЕРСИЯ ДЛЯ Python скрипта (download_media.py)
// ============================================
// Этот код обрабатывает JSON ответ от Python скрипта

// Обрабатываем ответ от Execute Command (Python скрипт)
const data = $input.item.json || $json;

// Получаем chat_id из исходных данных
const baseChatId = $json.chatId || $json.chat_id || data.chat_id || data.chatId;

// Python скрипт возвращает JSON в stdout
let result = null;

// Пытаемся распарсить JSON из stdout
if (data.stdout) {
  try {
    result = JSON.parse(data.stdout);
  } catch (e) {
    // Если не JSON, возможно это просто путь к файлу
    const stdout = data.stdout.trim();
    if (stdout && (stdout.indexOf('\\') >= 0 || stdout.indexOf('/') >= 0)) {
      result = {
        success: true,
        file_path: stdout
      };
    }
  }
}

// Если не нашли в stdout, проверяем stderr
if (!result && data.stderr) {
  try {
    result = JSON.parse(data.stderr);
  } catch (e) {
    // Игнорируем ошибку парсинга
  }
}

// Если все еще нет результата, проверяем данные напрямую
if (!result) {
  if (data.success !== undefined || data.file_path) {
    result = data;
  }
}

// Проверяем на ошибки
if (!result || result.success === false || result.error) {
  return {
    json: {
      error: true,
      message: result ? (result.error || 'Ошибка при скачивании') : 'Не удалось получить результат',
      chatId: baseChatId,
      debug: {
        stdout: data.stdout ? data.stdout.substring(0, 200) : null,
        stderr: data.stderr ? data.stderr.substring(0, 200) : null
      }
    }
  };
}

// Если успешно, извлекаем данные
const filePath = result.file_path || null;
const contentType = result.content_type || 'video';

if (!filePath) {
  return {
    json: {
      error: true,
      message: 'Файл не найден после скачивания',
      chatId: baseChatId
    }
  };
}

// Определяем тип по расширению если не указан
let finalContentType = contentType;
if (contentType === 'video' || contentType === 'audio') {
  finalContentType = contentType;
} else {
  const extension = filePath.split('.').pop().toLowerCase();
  if (extension === 'mp4' || extension === 'webm' || extension === 'mov' || 
      extension === 'avi' || extension === 'mkv') {
    finalContentType = 'video';
  } else if (extension === 'mp3' || extension === 'm4a' || extension === 'ogg') {
    finalContentType = 'audio';
  } else {
    finalContentType = 'video'; // По умолчанию
  }
}

// Получаем платформу
const platform = $json.platform || result.platform || null;

// Извлекаем название файла для metadata
const fileName = filePath.split('\\').pop().split('/').pop();
const title = fileName.replace(/\.[^/.]+$/, '');

// Возвращаем результат
if (finalContentType === 'video') {
  return {
    json: {
      file_path: filePath,
      chat_id: baseChatId,
      metadata: {
        title: title,
        platform: platform
      },
      content_type: 'video',
      has_video: true,
      has_audio: false,
      has_text: false
    }
  };
}

if (finalContentType === 'audio') {
  return {
    json: {
      file_path: filePath,
      chat_id: baseChatId,
      metadata: {
        title: title,
        platform: platform
      },
      content_type: 'audio',
      has_video: false,
      has_audio: true,
      has_text: false
    }
  };
}

// Fallback
return {
  json: {
    file_path: filePath,
    chat_id: baseChatId,
    metadata: {
      title: title,
      platform: platform
    },
    content_type: 'video',
    has_video: true,
    has_audio: false,
    has_text: false
  }
};

