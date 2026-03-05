# Slide Crops (Mermaid)

Use VS Code Markdown preview to view and right-click "Save Image" for each diagram.

## Slide 2 – Identity and Access Management
```mermaid
erDiagram
  UserType {
    string value "Guest | Registered | Staff"
  }
  Role {
    string roleId
    string roleName
    string description
  }
  Staff {
    string staffId
    string name
    string email
  }
  User {
    string uid
    string userName
    string email
    string password
    UserType userType
  }
  Role ||--o{ Staff : assigns
  Staff ||--|| User : maps
  User }o--|| UserType : has
```

## Slide 3 – Knowledge Base & Persona Modeling
```mermaid
erDiagram
  Character {
    string characterId
    string name
    text background
    string image
    string personality "max 500"
  }
  HistoricalContext {
    string contextId
    string name
    text description
  }
  CharacterDocument {
    string docId
    string title
    text content
    datetime uploadDate
  }
  HistoricalContextDocument {
    string docId
    string title
    text content
    datetime uploadDate
  }
  Character ||--o{ CharacterDocument : sources
  HistoricalContext ||--o{ HistoricalContextDocument : sources
```

## Slide 4 – Conversational Flow & Assessment
```mermaid
erDiagram
  ChatSession {
    string sessionId
    datetime createDate
  }
  Message {
    string messageId
    text content
    boolean isFromBot
    datetime timestamp
  }
  Quiz {
    string quizId
    string title
  }
  QuizResult {
    string resultId
    int score
    datetime takenDate
  }
  Question {
    string questionId
    text content
    text options
    string correctAnswer
  }
  QuizAnswerDetail {
    string detailId
    string selectedOption
    boolean isCorrect
  }
  ChatSession ||--o{ Message : logs
  Quiz ||--o{ QuizResult : grades
  QuizResult ||--o{ QuizAnswerDetail : answers
  Question ||--o{ QuizAnswerDetail : prompts
```
