# 🏆 RankBot

**RankBot** is a Discord bot that automatically renames users based on their current **League of Legends rank**. It preserves users' personal nicknames while appending clean and up-to-date rank information.

---

## 🚀 Features

- 🔄 Link Discord users to their League of Legends accounts
- 🏅 Automatically updates users' nicknames based on their current rank
- ✍️ Allows personalized nicknames alongside automated rank info
- ❌ Unlink users and reset their names
- 📦 Fully Dockerized with database support

---

## ⚙️ Technologies

- **Java 21**
- **Spring Boot (Web)**
- **JDA (Java Discord API)**
- **PostgreSQL**
- **Docker & Docker Compose**

---

## 📦 Setup & Installation

### 🔧 Prerequisites
- Discord bot initialized
- Docker & Docker Compose installed
- `.env` file with your API keys

### 🛠️ Example `.env`

```env
RIOT_API_KEY=your-riot-api-key
DISCORD_BOT_TOKEN=your-discord-bot-token
```

### ▶️ Start the Bot
setup discord bot and add to server
configure `.env.template` with your API key and discord bot token
rename `.env.template` to `.env`
```bash
docker-compose up --build
```

The bot and database will launch together via Docker Compose.

---

## 🧩 Configuration

- The bot needs the **`application.commands` permission** on your server.
- Make sure the bot's role is **higher** than the roles it needs to rename.

---

## 📜 Commands

| Slash Command              | Description                                                    |
|----------------------------|----------------------------------------------------------------|
| `/setuser <Summoner-Name>` | Links the Discord user to a League account. Format: `Joki#ANT` |
| `/help`                    | Displays this help message                                     |
| `/removeuser`              | Unlinks the user and removes them from the database            |
| `/setnickname`             | Allows the user to personalize their nickname                  |

---

## 🛢️ Database

- Uses PostgreSQL (runs via Docker)
- Table structure is initialized at startup (or can be adjusted manually via SQL)

---

## 👥 Contributing & License

This project is currently unlicensed. Feel free to build on it and use the code however you'd like. 🎉

---

## 📬 Contact

Questions, suggestions, or bugs? Open an issue or contact the developer on Discord ✉️

---

## ❤️ Example Nickname Format

A user with the original name `rong` will be renamed to:

```
rong ~ G 2 | 10 LP
```

Their nickname stays intact – and the rank stays fresh.
