# Structure de la base de données Firestore

Ce document décrit les principales collections et les champs stockés dans la base de données Firestore utilisée dans l'application de gestion collaborative de projets.

---

## 📁 Collection : `users`
Contient les données des utilisateurs enregistrés.

### Champs :
- `id` *(Document ID)* : identifiant unique de l'utilisateur
- `email` *(String)* : adresse email
- `displayName` *(String)* : nom affiché
- `photoUrl` *(String, nullable)* : URL de la photo de profil
- `phoneNumber` *(String, nullable)* : numéro de téléphone
- `bio` *(String)* : biographie
- `position` *(String)* : poste dans l’équipe
- `department` *(String)* : département
- `skills` *(Array<String>)* : compétences
- `projectIds` *(Array<String>)* : projets associés
- `lastActive` *(Timestamp)* : date de dernière activité
- `createdAt`, `lastLoginAt` *(Timestamp)* : dates de création et dernière connexion
- `status` *(Enum)* : statut (ACTIVE, AWAY, BUSY, OFFLINE)
- `role` *(Enum)* : rôle de l'utilisateur (ADMIN, MANAGER, MEMBER)
- `isEmailVerified` *(Boolean)* : email vérifié
- `fcmToken` *(String)* : token de notifications push
- `preferences` *(Map)* : préférences utilisateur
- `totalTasks`, `completedTasks`, `activeProjects` *(Int)* : métriques utilisateur

---

## 📁 Collection : `projects`
Contient les informations des projets collaboratifs.

### Champs :
- `id` *(Document ID)* : identifiant du projet
- `name` *(String)* : nom du projet
- `description` *(String)* : description du projet
- `ownerId` *(String)* : ID du propriétaire
- `members` *(Array<ProjectMember>)* : membres du projet
- `status` *(Enum)* : état du projet (IN_PROGRESS, COMPLETED, etc.)
- `priority` *(Enum)* : priorité (LOW, MEDIUM, HIGH, URGENT)
- `deadline` *(Date)* : date limite
- `createdAt`, `updatedAt` *(Timestamp)* : dates de création et modification
- `tags` *(Array<String>)* : étiquettes du projet
- `totalTasks`, `completedTasks` *(Int)* : statistiques des tâches
- `isCompleted`, `isArchived` *(Boolean)* : statut final et archivage
- `milestones` *(Array<Milestone>)* : jalons du projet
- `budgetAmount`, `budgetCurrency`, `actualCost` *(Double/String)* : données budgétaires
- `estimatedHours`, `actualHours` *(Float)* : temps estimé/réel
- `attachments` *(Array<FileAttachment>)* : fichiers liés
- `parentProjectId`, `subProjects` *(String / Array)* : hiérarchie
- `settings` *(ProjectSettings)* : paramètres du projet

---

## 📁 Collection : `tasks`
Contient les tâches associées aux projets.

### Champs :
- `id` *(Document ID)* : identifiant de la tâche
- `title`, `description` *(String)* : titre et description
- `richDescription` *(RichTextContent)* : contenu enrichi
- `projectId`, `parentTaskId` *(String)* : rattachement au projet/tâche
- `subtasks`, `dependencies`, `checklists` *(Array)* : structure complexe
- `assignedTo`, `createdBy` *(String / Array)* : attribution
- `status` *(Enum)* : état (TODO, IN_PROGRESS, COMPLETED…)
- `priority` *(Enum)* : priorité
- `startDate`, `dueDate`, `completedAt` *(Date)* : dates de vie de la tâche
- `isCompleted`, `isOverdue` *(Boolean)* : état d’achèvement
- `estimatedHours`, `actualHours` *(Float)* : suivi du temps
- `tags`, `watchers`, `attachments`, `comments` *(Array)* : informations supplémentaires
- `lastActivity` *(TaskActivity)* : dernière activité

---

## 📁 Collection : `chat_chats`
Contient les discussions (groupes, projets, directs).

### Champs :
- `id` *(Document ID)* : identifiant du chat
- `type` *(Enum)* : DIRECT, GROUP, PROJECT
- `name` *(String, nullable)* : nom du chat
- `participants` *(Array<String>)* : liste des participants
- `projectId` *(String, nullable)* : projet associé
- `lastMessage` *(Message)* : dernier message
- `unreadCount` *(Map<String, Int>)* : messages non lus par utilisateur
- `createdAt`, `updatedAt` *(Timestamp)* : dates de création/modification

---

## 📁 Collection : `chat_messages`
Contient les messages échangés dans les chats.

### Champs :
- `id` *(Document ID)* : identifiant du message
- `chat_id` *(String)* : identifiant du chat
- `sender_id`, `sender_name` *(String)* : émetteur du message
- `content` *(String)* : contenu du message
- `type` *(Enum)* : TEXT, IMAGE, FILE, SYSTEM
- `attachments` *(Array<FileAttachment>)* : pièces jointes
- `read_by` *(Array<String>)* : utilisateurs ayant lu le message
- `sent_at` *(Timestamp)* : date d’envoi
- `status` *(Enum)* : SENDING, SENT, DELIVERED, READ

---

## 📁 Collection : `comments`
Commentaires liés aux tâches et projets.

### Champs :
- `id`, `projectId`, `taskId`, `userId` *(String)* : références
- `authorName` *(String)* : auteur
- `content` *(String)* : contenu du commentaire
- `createdAt`, `updatedAt` *(Timestamp)* : dates
- `attachmentIds`, `mentions` *(Array)* : fichiers ou mentions
- `parentId` *(String, nullable)* : commentaire parent
- `isEdited` *(Boolean)* : modifié ou non

---

## 📁 Collection : `skills`
Compétences ajoutées dans le système.

### Champs :
- `id` *(Document ID)* : identifiant de la compétence
- `name` *(String)* : nom de la compétence (ex. "Frontend")
- `category` *(String)* : catégorie (facultatif)
- `description` *(String)* : description (facultatif)
- `usageCount` *(Int)* : nombre d’utilisations
- `createdBy` *(String)* : ID de l’utilisateur ayant créé
- `createdAt` *(Timestamp)* : date de création

---

## 📁 Collection : `attachments`
Fichiers liés aux commentaires, tâches ou projets.

### Champs :
- `id` *(Document ID)* : identifiant du fichier
- `name` *(String)* : nom du fichier
- `download_url` *(String)* : lien de téléchargement
- `mime_type` *(String)* : type MIME
- `size` *(Int)* : taille en octets
- `storage_path` *(String)* : chemin de stockage
- `uploaded_by`, `uploaded_by_id` *(String)* : auteur du dépôt
- `uploaded_at` *(Timestamp)* : date d’upload
- `type` *(String)* : type fonctionnel (optionnel)
- `comment_id`, `task_id`, `project_id` *(String, nullable)* : rattachement

---