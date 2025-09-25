# MCP Server Setup Guide

## Prerequisites
- Docker Desktop installed
- MySQL Workbench installed

## 1. Start Redis with Docker

```bash
# Pull and run Redis container
docker run -d --name mcp-redis -p 6379:6379 redis:7-alpine

# To stop Redis later
docker stop mcp-redis

# To start Redis again
docker start mcp-redis
```

## 2. Start MySQL with Docker (Optional - if you prefer Docker over local MySQL)

```bash
# Pull and run MySQL container
docker run -d --name mcp-mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=mcp_server \
  -e MYSQL_USER=mcp_user \
  -e MYSQL_PASSWORD=SecureMCPPassword123! \
  -p 3306:3306 \
  mysql:8.0

# To stop MySQL later
docker stop mcp-mysql

# To start MySQL again
docker start mcp-mysql
```

## 3. Install and Setup Ollama

### Install Ollama:
1. Download from: https://ollama.ai/download
2. Install following the instructions for your platform

### Pull Llama3 7B model:
```bash
# Pull the llama3 7B model (~4GB download)
ollama pull llama3

# Verify installation
ollama list

# Test the model
ollama run llama3 "Hello, how are you?"
```

### Start Ollama API server:
```bash
# Ollama automatically starts the API server at http://localhost:11434
# You can verify it's running by visiting: http://localhost:11434/api/tags
```

## 4. Database Setup

1. Open MySQL Workbench
2. Connect to your MySQL server (localhost:3306)
3. Create database `chatbot` if not exists
4. Create user `mcp_user` with password `SecureMCPPassword123!`
5. Grant all privileges on `chatbot` database to `mcp_user`

## 5. Verify Setup

### Check Redis:
```bash
docker exec -it mcp-redis redis-cli ping
# Should return: PONG
```

### Check MySQL:
- Connect via MySQL Workbench to localhost:3306
- Should see `chatbot` database

### Check Ollama:
```bash
curl http://localhost:11434/api/tags
# Should return JSON with available models
```

## 6. Environment Variables

Make sure your `.env` file has the correct values:
- `REDIS_HOST=localhost`
- `DB_HOST=localhost` (or your MySQL server address)
- `OLLAMA_BASE_URL=http://localhost:11434`
- `OPENAI_API_KEY=your-actual-openai-key` (if using OpenAI)

## Ready to Run!

Once all services are running, you can start your Spring Boot application:
```bash
./mvnw spring-boot:run
```

The application will be available at: http://localhost:8080