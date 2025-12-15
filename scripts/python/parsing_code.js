// ============================================
// КОД ДЛЯ УЗЛА "Code - Parse URL" (ПАРСИНГ)
// ============================================
// Скопируйте этот код в узел Code в n8n
// Название узла: "Code - Parse URL"

const message = $input.item.json.message;
const text = message.text || '';

// Валидация наличия текста
if (!text || text.trim().length === 0) {
  return {
    json: {
      error: true,
      message: 'Сообщение пустое. Отправьте ссылку на контент.',
      chatId: message.chat.id
    }
  };
}

// Извлечение URL
function extractUrl(text) {
  // Ищем URL в тексте
  const urlRegex = /(https?:\/\/[^\s\)\]\>\"\']+)/g;
  const matches = text.match(urlRegex);
  if (!matches || matches.length === 0) {
    return null;
  }
  
  let url = matches[0];
  
  // Убираем лишние символы в конце (точки, запятые, скобки и т.д.)
  url = url.replace(/[.,;:!?)\]}>]+$/, '');
  
  return url;
}

// Валидация URL
function isValidUrl(url) {
  if (!url || typeof url !== 'string') {
    return false;
  }
  
  // Простая проверка: должен начинаться с http:// или https://
  if (url.indexOf('http://') !== 0 && url.indexOf('https://') !== 0) {
    return false;
  }
  
  // Проверяем наличие точки (домена) - точка должна быть после протокола
  const dotIndex = url.indexOf('.');
  if (dotIndex < 0) {
    return false; // Нет точки вообще
  }
  // Точка должна быть после протокола (http:// = 7 символов, https:// = 8 символов)
  if (dotIndex < 7) {
    return false; // Точка слишком рано
  }
  
  // Дополнительная проверка через try-catch (если доступен URL)
  try {
    // В некоторых версиях n8n может не быть URL, поэтому делаем простую проверку
    if (typeof URL !== 'undefined') {
      const urlObj = new URL(url);
      return urlObj.protocol === 'http:' || urlObj.protocol === 'https:';
    }
  } catch (e) {
    // Если URL не поддерживается, используем простую проверку
    // Но если дошли сюда, значит базовая проверка прошла
  }
  
  return true;
}

// Определение платформы
function detectPlatform(url) {
  const lowerUrl = url.toLowerCase();
  if (lowerUrl.includes('youtube.com') || lowerUrl.includes('youtu.be')) {
    return 'youtube';
  } else if (lowerUrl.includes('instagram.com')) {
    return 'instagram';
  } else if (lowerUrl.includes('twitter.com') || lowerUrl.includes('x.com')) {
    return 'twitter';
  } else if (lowerUrl.includes('music.yandex')) {
    return 'yandex_music';
  } else if (lowerUrl.includes('tiktok.com') || lowerUrl.includes('vm.tiktok.com')) {
    return 'tiktok';
  }
  return 'unknown';
}

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

// ВАЖНО: В n8n Code узлы должны возвращать данные в формате { json: {...} }
return {
  json: {
    url: url,
    platform: platform,
    chatId: message.chat.id,
    messageId: message.message_id,
    error: false
  }
};

