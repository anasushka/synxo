# Synxo

Веб-приложение на Spring Boot с SQLite в качестве базы данных.

## Требования

### Локальный запуск
- **JDK 21**

  Ubuntu:
  ```bash
  sudo apt install openjdk-21-jdk -y
  ```
  Другие ОС — [adoptium.net](https://adoptium.net/)

### Запуск через Docker
- **Docker** + **Docker Compose**

  Ubuntu:
  ```bash
  sudo apt install docker.io docker-compose-v2 -y
  ```
  Другие ОС — [docker.com](https://www.docker.com/)

---

## Запуск локально

### 1. Клонировать репозиторий

```bash
git clone <url-репозитория>
cd demo
```

### 2. Запустить приложение

**Linux / macOS:**
```bash
./gradlew bootRun
```

**Windows:**
```cmd
gradlew.bat bootRun
```

Приложение поднимется на [http://localhost:8080](http://localhost:8080).

База данных и папка для загрузок создадутся автоматически:
```
demo/
├── data/synxo.db     ← файл базы данных SQLite
└── uploads/          ← загружаемые файлы (аватарки и т.п.)
```

---

## Запуск через Docker

```bash
docker compose --profile containerized up --build
```

Приложение будет доступно на [http://localhost:8080](http://localhost:8080).

Данные сохраняются в Docker-томах `synxo_data` и `synxo_uploads` и не пропадают при перезапуске контейнера.

---

## Сборка JAR-файла

```bash
./gradlew bootJar        # Linux / macOS
gradlew.bat bootJar      # Windows
```

Собранный файл появится в `build/libs/synxo-0.0.1-SNAPSHOT.jar`. Запустить его можно так:

```bash
java -jar build/libs/synxo-0.0.1-SNAPSHOT.jar
```

---

## Запуск тестов

```bash
./gradlew test           # Linux / macOS
gradlew.bat test         # Windows
```

---

## Стек

| Компонент | Версия |
|-----------|--------|
| Java | 21 |
| Spring Boot | 4.0.6 |
| База данных | SQLite |
| Сборка | Gradle |
