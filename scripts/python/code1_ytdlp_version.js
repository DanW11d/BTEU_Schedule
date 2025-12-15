// ============================================
// КОД ДЛЯ УЗЛА "Code - Process Response" (CODE1)
// ВЕРСИЯ ДЛЯ yt-dlp (Execute Command)
// ============================================
// Этот код обрабатывает вывод команды yt-dlp

// Обрабатываем ответ от Execute Command (yt-dlp)
const data = $input.item.json || $json;

// Получаем chat_id из исходных данных (переданных от парсинга)
const baseChatId = $json.chatId || $json.chat_id || data.chat_id || data.chatId;

// yt-dlp выводит информацию в stdout/stderr
const stdout = data.stdout || '';
const stderr = data.stderr || '';
const exitCode = data.exitCode !== undefined ? data.exitCode : (data.error ? 1 : 0);

// Извлекаем путь к скачанному файлу из вывода yt-dlp
// yt-dlp выводит различные форматы:
// - "[download] Destination: путь/к/файлу.mp4"
// - "[ExtractAudio] Destination: путь/к/файлу.mp3"
// - Или просто путь к файлу если использован --print after_move:filepath
let filePath = null;

// Вариант 1: Ищем в stdout (основной вывод)
if (stdout) {
  // Паттерн 1: [download] Destination: путь
  const downloadMatch = stdout.match(/\[download\]\s+Destination:\s+(.+?)(?:\n|$)/i);
  if (downloadMatch && downloadMatch[1]) {
    filePath = downloadMatch[1].trim();
  }
  
  // Паттерн 2: [ExtractAudio] Destination: путь (для аудио)
  if (!filePath) {
    const audioMatch = stdout.match(/\[ExtractAudio\]\s+Destination:\s+(.+?)(?:\n|$)/i);
    if (audioMatch && audioMatch[1]) {
      filePath = audioMatch[1].trim();
    }
  }
  
  // Паттерн 3: Если использован --print after_move:filepath, путь будет в первой строке
  if (!filePath) {
    const lines = stdout.split('\n').filter(line => line.trim().length > 0);
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      // Проверяем, является ли строка путем к файлу
      if ((line.indexOf('\\') >= 0 || line.indexOf('/') >= 0) && 
          (line.indexOf('.mp4') >= 0 || line.indexOf('.mp3') >= 0 || 
           line.indexOf('.webm') >= 0 || line.indexOf('.m4a') >= 0)) {
        filePath = line;
        break;
      }
    }
  }
  
  // Паттерн 4: Ищем любой путь к файлу с расширением
  if (!filePath) {
    const pathMatch = stdout.match(/([A-Za-z]:[\\\/][^\s\n]+\.(mp4|mp3|webm|m4a|ogg|wav|avi|mov|mkv))/i);
    if (pathMatch && pathMatch[1]) {
      filePath = pathMatch[1].trim();
    }
  }
}

// Вариант 2: Ищем в stderr (ошибки или дополнительная информация)
if (!filePath && stderr) {
  const stderrMatch = stderr.match(/\[download\]\s+Destination:\s+(.+?)(?:\n|$)/i);
  if (stderrMatch && stderrMatch[1]) {
    filePath = stderrMatch[1].trim();
  }
}

// Вариант 3: Проверяем наличие filePath в данных напрямую
if (!filePath) {
  filePath = data.filePath || data.file_path || null;
}

// Проверяем успешность выполнения
if (exitCode !== 0 && !filePath) {
  const errorMessage = stderr || stdout || 'Неизвестная ошибка при скачивании';
  return {
    json: {
      error: true,
      message: 'Ошибка при скачивании: ' + errorMessage.substring(0, 200),
      chatId: baseChatId
    }
  };
}

// Если файл не найден
if (!filePath) {
  return {
    json: {
      error: true,
      message: 'Не удалось найти скачанный файл. Проверьте вывод команды.',
      chatId: baseChatId,
      debug: {
        stdout: stdout.substring(0, 500),
        stderr: stderr.substring(0, 500),
        exitCode: exitCode
      }
    }
  };
}

// Определяем тип файла по расширению
const lowerPath = filePath.toLowerCase();
const extension = filePath.split('.').pop().toLowerCase().split('?')[0];

const isVideo = extension === 'mp4' || extension === 'webm' || extension === 'mov' || 
                extension === 'avi' || extension === 'mkv' || extension === 'flv' || 
                extension === 'm4v' || lowerPath.indexOf('video') >= 0;

const isAudio = extension === 'mp3' || extension === 'm4a' || extension === 'ogg' || 
                extension === 'wav' || extension === 'flac' || extension === 'aac' ||
                lowerPath.indexOf('audio') >= 0;

// Получаем платформу из исходных данных
const platform = $json.platform || data.platform || null;

// Определяем тип контента
let contentType = 'video'; // По умолчанию видео
if (isAudio && !isVideo) {
  contentType = 'audio';
}

// Для YouTube/Instagram/TikTok - обычно видео
if (platform === 'youtube' || platform === 'instagram' || platform === 'tiktok') {
  contentType = 'video';
}

// Для Yandex.Music - обычно аудио
if (platform === 'yandex_music') {
  contentType = 'audio';
}

// Возвращаем результат
if (contentType === 'video' || isVideo) {
  return {
    json: {
      file_path: filePath,
      chat_id: baseChatId,
      metadata: {
        title: filePath.split('\\').pop().split('/').pop().replace(/\.[^/.]+$/, ''),
        platform: platform
      },
      content_type: 'video',
      has_video: true,
      has_audio: false,
      has_text: false
    }
  };
}

if (contentType === 'audio' || isAudio) {
  return {
    json: {
      file_path: filePath,
      chat_id: baseChatId,
      metadata: {
        title: filePath.split('\\').pop().split('/').pop().replace(/\.[^/.]+$/, ''),
        platform: platform
      },
      content_type: 'audio',
      has_video: false,
      has_audio: true,
      has_text: false
    }
  };
}

// Fallback - считаем видео
return {
  json: {
    file_path: filePath,
    chat_id: baseChatId,
    metadata: {
      title: filePath.split('\\').pop().split('/').pop().replace(/\.[^/.]+$/, ''),
      platform: platform
    },
    content_type: 'video',
    has_video: true,
    has_audio: false,
    has_text: false
  }
};

