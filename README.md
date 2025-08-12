# 📅 Telegram Bot для расписания колледжа

Телеграм-бот на Java + Spring Boot для отображения расписания занятий колледжа.  
Администратор загружает Excel-файл с расписанием, бот парсит его и отправляет студентам актуальное расписание по запросу.

---

## 🚀 Функционал
- 📂 Загрузка Excel-файла администратором
- 📅 Получение расписания пользователям
- 👥 Разделение ролей: администратор и пользователь
- 💾 Хранение данных в PostgreSQL (Spring Data JPA)

---

## 🛠 Технологии
- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **PostgreSQL**
- **Apache POI** (парсинг Excel)
- **TelegramBots** (Java API для Telegram)
- **Docker** (опционально)

---

## 📦 Запуск проекта

### 1. Клонировать репозиторий
```bash
git clone https://github.com/username/college-schedule-bot.git
cd college-schedule-bot
