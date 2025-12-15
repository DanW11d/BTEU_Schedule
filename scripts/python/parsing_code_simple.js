// ============================================
// ПРОСТОЙ РАБОЧИЙ КОД ДЛЯ УЗЛА "Parse URL"
// ============================================
// Скопируйте ВЕСЬ этот код в узел Code в n8n
// Название узла: "Parse URL"
// Режим узла: "Run Once for Each Item"

// Получаем сообщение из Telegram
const message = $json.message;
const text = message.text || '';

// Проверка: сообщение не пустое
if (!text || text.trim().length === 0) {
  return {
    json: {
      error: true,
      message: 'Сообщение пустое. Отправьте ссылку на контент.',
      chatId: message.chat.id
    }
  };
}

// Функция извлечения URL из текста
function extractUrl(text) {
  const urlRegex = /(https?:\/\/[^\s\)\]\>\"\']+)/g;
  const matches = text.match(urlRegex);
  if (!matches || matches.length === 0) {
    return null;
  }
  let url = matches[0];
  url = url.replace(/[.,;:!?)\]}>]+$/, '');
  return url;
}

// Функция проверки валидности URL
function isValidUrl(url) {
  if (!url || typeof url !== 'string') {
    return false;
  }
  if (url.indexOf('http://') !== 0 && url.indexOf('https://') !== 0) {
    return false;
  }
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

if (!url) {
  return {
    json: {
      error: true,
      message: 'Не найдена ссылка в сообщении. Отправьте ссылку на контент.',
      chatId: message.chat.id
    }
  };
}

if (!isValidUrl(url)) {
  return {
    json: {
      error: true,
      message: 'Неверный формат URL',
      chatId: message.chat.id
    }
  };
}

const platform = detectPlatform(url);

return {
  json: {
    url: url,
    platform: platform,
    chatId: message.chat.id,
    messageId: message.message_id,
    error: false
  }
};



























