# 🚀 SkillMatch – Plateforme de mise en relation intelligente candidats / entreprises

[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📖 Présentation

**SkillMatch** est une plateforme web et mobile (PWA) qui facilite l’employabilité des candidats et simplifie le processus de recrutement pour les entreprises.  
Grâce à un **matching automatique** basé sur le niveau scolaire et les compétences, chaque candidat voit uniquement les offres pertinentes, et chaque entreprise reçoit des candidatures qualifiées.

Le projet est construit avec une **architecture microservices** (Spring Boot, Spring Cloud) pour garantir évolutivité, résilience et maintenabilité.

---

##  Fonctionnalités principales

###  Candidat
- Inscription / connexion sécurisée (JWT)
- Gestion de profil (nom, prénom, compétences, niveau scolaire, téléphone, adresse)
- Upload de CV (PDF) et photo de profil
- Visualisation des offres recommandées avec **score de compatibilité**
- Postulation en un clic
- Suivi de l’historique des candidatures (statuts : EN_ATTENTE, ACCEPTÉE, REFUSÉE, ENTRETIEN)
- Notifications en temps réel (changement de statut, invitation à un entretien)

###  Entreprise
- Création et gestion du profil entreprise (logo, description, secteur…)
- Publication, modification et suppression (logique) d’offres d’emploi
- Dashboard avec liste des offres actives et nombre de candidatures reçues
- Consultation détaillée des candidatures (score matching, CV, statut)
- Planification d’entretiens (date, lieu, notes) – la candidature passe automatiquement à `ENTRETIEN`
- Statistiques : total candidatures, entretiens programmés

###  Administrateur
- Supervision globale : nombre d’utilisateurs, candidats, entreprises, offres, candidatures
- Gestion des utilisateurs (activation/désactivation, changement de rôle)
- Modération des fichiers téléchargés (CV, logos) avec **analyse IA simulée** (score de conformité)
- Modération des offres (consultation et suppression si inappropriées)
- Téléchargement sécurisé des fichiers via un tunnel API Gateway

###  Fonctionnalités transverses
- **Matching automatique** : calcul d’un score 0‑100 basé sur niveau scolaire (40%) et compétences (60%)
- **Notifications** : alertes internes à la plateforme (via microservice dédié)
- **API Gateway** : point d’entrée unique (port 8080) avec routage et transmission du token JWT
- **Service Discovery** : Eureka pour l’enregistrement dynamique des microservices
- **Resilience4j** : circuit breakers et fallbacks sur tous les appels inter-services
- **PWA** : installation sur mobile / bureau, navigation hors ligne partielle

---

##  Architecture technique

### Microservices

| Service               | Port | Base de données     | Responsabilité principale |
|-----------------------|------|---------------------|----------------------------|
| Auth Service          | 8081 | skillmatch_auth     | Authentification, JWT, rôles, administration utilisateurs |
| Candidat Service      | 8082 | skillmatch_candidats| Profil candidat, CV, compétences, niveau scolaire |
| Entreprise Service    | 8083 | skillmatch_entreprises | Profil entreprise, logo, secteur |
| Offre Service         | 8084 | skillmatch_offres   | CRUD offres, recherche, enrichissement (nom entreprise, nb candidatures) |
| Candidature Service   | 8085 | skillmatch_candidatures | Postulation, score matching, statuts, entretiens, notifications |
| API Gateway           | 8080 | -                   | Routage, agrégation, sécurité |
| Eureka Server         | 8761 | -                   | Service discovery |

### Stack technologique

- **Backend** : Java 17, Spring Boot 3.2, Spring Cloud (OpenFeign, Gateway, Eureka)
- **Résilience** : Resilience4j (circuit breaker, retry, timeout)
- **Base de données** : MySQL (une base par service) / H2 pour les tests
- **Frontend** : HTML5, Bootstrap 5, JavaScript (appels `fetch` vers l’API Gateway), Service Worker, manifest.json
- **Sécurité** : JWT (jjwt), Spring Security, BCrypt
- **Communication** : Feign clients avec fallbacks
- **Stockage fichiers** : système de fichiers local (`uploads/cvs`, `uploads/photos`, `uploads/logos`)
- **Tests** : JUnit 5, Mockito, @WebMvcTest, @SpringBootTest, H2

### Schéma d’architecture (simplifié)
![](C:\Users\Hp\Desktop\CDPI\projet find'etude\conception skillmatching\schema-explicatif.jpg)
