# Application Android de Gestion Collaborative de Projets

## Présentation
Cette application Android vise à faciliter la gestion collaborative de projets pour des équipes de toutes tailles. Conçue avec des pratiques modernes de développement, elle offre une suite complète d’outils pour la planification, la gestion des tâches, la communication d’équipe et le suivi de la progression.

## État actuel du développement
L’application est actuellement en développement actif avec les composants suivants déjà implémentés :

### Architecture principale
- **Langage** : Kotlin
- **UI** : Jetpack Compose
- **Architecture** : MVVM (Modèle-Vue-VueModèle)
- **Services backend** : Firebase (Authentication, Firestore, Storage, Cloud Messaging)
- **Injection de dépendances** : Hilt
- **Navigation** : Jetpack Navigation Component

### Fonctionnalités implémentées

#### Authentification & Gestion des utilisateurs
- Inscription et connexion par e-mail/mot de passe
- Intégration de Google Sign-In
- Gestion du profil utilisateur
- Contrôle d’accès basé sur les rôles (Admin, Chef de projet, Membre)
- Préférences et paramètres utilisateurs

#### Gestion de projets
- Création, modification et suppression de projets
- Tableau de bord avec indicateurs clés
- Suivi de l’état d’avancement des projets
- Paramètres de configuration des projets
- Gestion des membres associés à un projet

#### Gestion des tâches
- Création et affectation des tâches
- Dépendances et hiérarchisation (sous-tâches)
- Suivi de l’état d’avancement des tâches
- Gestion des délais
- Niveaux de priorité
- Attribution intelligente basée sur les compétences et la charge
- Suivi du temps de réalisation des tâches

#### Communication
- Système de chat en temps réel
- Commentaires liés aux tâches
- Mentions (@) pour notifier un utilisateur
- Partage de fichiers dans les conversations
- Notifications pour les événements importants

#### Gestion de fichiers
- Téléversement de fichiers liés aux projets et aux tâches
- Partage de fichiers avec les membres de l’équipe
- Aperçu des fichiers standards
- Historique des versions de documents

### Modèles de données
L’application utilise les modèles principaux suivants :

- **Utilisateur** : Informations, préférences et identifiants
- **Projet** : Détails, membres, paramètres, statistiques
- **Tâche** : Éléments de travail avec statut, affectation, dépendances
- **Commentaire** : Discussions liées aux projets/tâches
- **Message** : Communication en temps réel
- **Fichier** : Pièces jointes et métadonnées
- **Temps** : Enregistrements de temps de travail

### Composants UI
- Implémentation Material Design 3 avec Jetpack Compose
- Interfaces responsives pour tous types d’écrans
- Composants personnalisés pour la gestion de projets
- Graphiques et visualisations interactives

### Intégration d'IA
- Recommandations intelligentes de tâches via IA
  
## Détails techniques

### Intégration Firebase
- **Authentication** : Connexion sécurisée multi-fournisseur
- **Firestore** : Base NoSQL en temps réel
- **Storage** : Stockage des fichiers et avatars utilisateurs
- **Cloud Messaging** : Notifications push pour alertes en direct

### Bibliothèques Jetpack utilisées
- **Compose** : Interface déclarative moderne
- **ViewModel** : Gestion des états de l’UI
- **LiveData / Flow** : Modèles de données observables
- **Room** : Abstraction de SQLite pour cache local
- **WorkManager** : Exécution de tâches en arrière-plan
- **Navigation** : Gestion de la navigation dans l’app

### Bibliothèques tierces
- **Retrofit** : Client HTTP pour API REST
- **Coil** : Chargement et mise en cache d’images
- **Timber** : Outils de logs
- **Vico** : Visualisation de données (graphiques)
- **KotlinX DateTime** : Gestion des dates et heures

## Installation et configuration

### Prérequis
- Android Studio Arctic Fox (2020.3.1) ou version supérieure
- JDK 11 ou plus récent
- Android SDK 31 (Android 12) ou plus
- Compte Firebase configuré

### Étapes d’installation
1. Cloner le dépôt du projet
2. Créer un projet Firebase et le configurer pour Android
3. Télécharger le fichier `google-services.json` et le placer dans le dossier `app`
4. Configurer Firebase Authentication, Firestore et Storage via la console Firebase
5. Compiler et exécuter l’application depuis Android Studio

## Évolutions futures
- Développement de la version iOS
- Outils d’analyse et de reporting avancés
- Intégration avec des services tiers (Jira, GitHub, Slack)
- Amélioration des fonctionnalités hors-ligne
- Gestion étendue des fichiers et historique


