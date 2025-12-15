// ============================================
// РАБОЧИЙ КОД ДЛЯ УЗЛА "Parse URL"
// ============================================
// Скопируйте ВЕСЬ этот код в узел Code в n8n
// Название узла: "Parse URL"

// Получаем сообщение из Telegram
// В n8n данные могут быть в разных форматах в зависимости от режима узла
let message = null;
let text = '';

// Пробуем разные способы доступа к данным
if (typeof $input !== 'undefined' && $input && $input.item && $input.item.json && $input.item.json.message) {
  message = $input.item.json.message;
} else if (typeof $json !== 'undefined' && $json && $json.message) {
  message = $json.message;
} else if (typeof $input !== 'undefined' && $input && $input.json && $input.json.message) {
  message = $input.json.message;
}

if (message) {
  text = message.text || '';
}

// Проверка: сообщение не пустое
if (!text || text.trim().length === 0) {
  const chatId = (message && message.chat && message.chat.id) || null;
  return {
    json: {
      error: true,
      message: 'Сообщение пустое. Отправьте ссылку на контент.',
      chatId: chatId
    }
  };
}

// Функция извлечения URL из текста
function extractUrl(text) {
  // Ищем URL в тексте (http:// или https://)
  const urlRegex = /(https?:\/\/[^\s\)\]\>\"\']+)/g;
  const matches = text.match(urlRegex);
  
  if (!matches || matches.length === 0) {
    return null;
  }
  
  let url = matches[0];
  
  // Убираем лишние символы в конце (точки, запятые, скобки)
  url = url.replace(/[.,;:!?)\]}>]+$/, '');
  
  return url;
}

// Функция проверки валидности URL
function isValidUrl(url) {
  if (!url || typeof url !== 'string') {
    return false;
  }
  
  // Должен начинаться с http:// или https://
  if (url.indexOf('http://') !== 0 && url.indexOf('https://') !== 0) {
    return false;
  }
  
  // Должна быть точка (домен)
  const dotIndex = url.indexOf('.');
  if (dotIndex < 0 || dotIndex < 7) {
    return false;
  }
  
  return true;
}

// Функция определения платформы
function detectPlatform(url) {
  const lowerUrl = url.toLowerCase();
  
  if (lowerUrl.indexOf('youtube.com') >= 0 || lowerUrl.indexOf('youtu.be') >= 0) {
    return 'youtube';
  } else if (lowerUrl.indexOf('instagram.com') >= 0) {
    return 'instagram';
  } else if (lowerUrl.indexOf('twitter.com') >= 0 || lowerUrl.indexOf('x.com') >= 0) {
    return 'twitter';
  } else if (lowerUrl.indexOf('music.yandex') >= 0) {
    return 'yandex_music';
  } else if (lowerUrl.indexOf('tiktok.com') >= 0 || lowerUrl.indexOf('vm.tiktok.com') >= 0) {
    return 'tiktok';
  }
  
  return 'unknown';
}

// Извлекаем URL
const url = extractUrl(text);

// Получаем chatId для ошибок
const chatId = (message && message.chat && message.chat.id) || null;
const messageId = (message && message.message_id) || null;

// Проверка: URL найден
if (!url) {
  return {
    json: {
      error: true,
      message: 'Не найдена ссылка в сообщении. Отправьте ссылку на контент.',
      chatId: chatId
    }
  };
}

// Проверка: URL валидный
if (!isValidUrl(url)) {
  return {
    json: {
      error: true,
      message: 'Неверный формат URL',
      chatId: chatId
    }
  };
}

// Определяем платформу
const platform = detectPlatform(url);

// Возвращаем результат
// ВАЖНО: В n8n Code узлы должны возвращать данные в формате { json: {...} }
return {
  json: {
    url: url,
    platform: platform,
    chatId: chatId,
    messageId: messageId,
    error: false
  }
};

