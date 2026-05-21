-- ============================================
-- SKILLMATINGCHPFE - Script complet de base de données
-- ============================================

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS skillmatchingApp;
USE skillmatchingApp;

-- Désactiver les contraintes pendant la création
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. TABLE DES UTILISATEURS (Auth Service)
-- ============================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NULL,              -- NULL pour OAuth2 (Google/GitHub)
                       role ENUM('CANDIDAT', 'ENTREPRISE', 'ADMIN') NOT NULL,
                       enabled BOOLEAN DEFAULT TRUE,            -- Pour bloquer/débloquer
                       provider VARCHAR(50) DEFAULT 'LOCAL',    -- LOCAL, GOOGLE, GITHUB
                       provider_id VARCHAR(255),                -- ID unique du provider OAuth2
                       date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       date_modification TIMESTAMP NULL,
                       derniere_connexion TIMESTAMP NULL,

                       INDEX idx_email (email),
                       INDEX idx_provider (provider, provider_id),
                       INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. TABLE DES CANDIDATS (Profile Service)
-- ============================================
DROP TABLE IF EXISTS candidats;
CREATE TABLE candidats (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL UNIQUE,
                           nom VARCHAR(100) NOT NULL,
                           prenom VARCHAR(100) NOT NULL,
                           telephone VARCHAR(20),
                           adresse VARCHAR(255),
                           ville VARCHAR(100),
                           pays VARCHAR(50) DEFAULT 'France',
                           bio TEXT,
                           competences TEXT,                        -- Stocké en CSV : "Java,Spring,React"
                           linkedin_url VARCHAR(255),
                           portfolio_url VARCHAR(255),
                           photo_path VARCHAR(255),                -- Chemin de la photo uploadée
                           cv_path VARCHAR(255),                   -- Chemin du CV uploadé
                           niveau_scolaire VARCHAR(100),            -- ex: "Bac+3", "Master", "Doctorat"
                           diplome VARCHAR(255),                   -- ex: "Licence Informatique"
                           annee_diplome INT,
                           ecole VARCHAR(255),
                           disponibilite ENUM('IMMEDIATE', 'UN_MOIS', 'TROIS_MOIS', 'PLUS') DEFAULT 'IMMEDIATE',
                           mobilite VARCHAR(255),                  -- ex: "Paris, Lyon, Remote"
                           validation_statut ENUM('EN_ATTENTE', 'VALIDE', 'REJETE') DEFAULT 'EN_ATTENTE',
                           date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           date_modification TIMESTAMP NULL,

                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           INDEX idx_user (user_id),
                           INDEX idx_validation (validation_statut),
                           INDEX idx_competences (competences(255)),
                           FULLTEXT INDEX ft_competences (competences)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. TABLE DES EXPÉRIENCES (Profile Service)
-- ============================================
DROP TABLE IF EXISTS experiences;
CREATE TABLE experiences (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             candidat_id BIGINT NOT NULL,
                             poste VARCHAR(150) NOT NULL,
                             entreprise_nom VARCHAR(150) NOT NULL,
                             ville VARCHAR(100),
                             pays VARCHAR(50),
                             date_debut DATE NOT NULL,
                             date_fin DATE NULL,                      -- NULL si poste actuel
                             en_poste BOOLEAN DEFAULT FALSE,
                             type_contrat ENUM('CDI', 'CDD', 'STAGE', 'ALTERNANCE', 'FREELANCE', 'AUTRE') DEFAULT 'CDI',
                             description TEXT,
                             competences_utilisees TEXT,              -- Compétences spécifiques à l'expérience

                             FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE CASCADE,
                             INDEX idx_candidat (candidat_id),
                             INDEX idx_dates (date_debut, date_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. TABLE DES FORMATIONS (Profile Service)
-- ============================================
DROP TABLE IF EXISTS formations;
CREATE TABLE formations (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            candidat_id BIGINT NOT NULL,
                            diplome VARCHAR(255) NOT NULL,
                            etablissement VARCHAR(255) NOT NULL,
                            ville VARCHAR(100),
                            pays VARCHAR(50),
                            date_debut DATE NOT NULL,
                            date_fin DATE NULL,
                            en_cours BOOLEAN DEFAULT FALSE,
                            mention VARCHAR(100),
                            description TEXT,

                            FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE CASCADE,
                            INDEX idx_candidat (candidat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. TABLE DES ENTREPRISES (Entreprise Service)
-- ============================================
DROP TABLE IF EXISTS entreprises;
CREATE TABLE entreprises (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL UNIQUE,
                             nom_entreprise VARCHAR(150) NOT NULL,
                             siret VARCHAR(14),
                             secteur VARCHAR(100),
                             sous_secteur VARCHAR(100),
                             description TEXT,
                             site_web VARCHAR(255),
                             logo_path VARCHAR(255),                  -- Chemin du logo uploadé
                             telephone VARCHAR(20),
                             contact_email VARCHAR(150),
                             adresse VARCHAR(255),
                             ville VARCHAR(100),
                             code_postal VARCHAR(10),
                             pays VARCHAR(50) DEFAULT 'France',
                             taille ENUM('TPE', 'PME', 'ETI', 'GRAND_GROUPE', 'STARTUP') DEFAULT 'PME',
                             effectif INT,
                             annee_creation INT,
                             linkedin_url VARCHAR(255),
                             twitter_url VARCHAR(255),
                             validation_statut ENUM('EN_ATTENTE', 'VALIDE', 'REJETE') DEFAULT 'EN_ATTENTE',
                             date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             date_modification TIMESTAMP NULL,

                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             INDEX idx_user (user_id),
                             INDEX idx_secteur (secteur),
                             INDEX idx_ville (ville)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 6. TABLE DES OFFRES D'EMPLOI (Offre Service)
-- ============================================
DROP TABLE IF EXISTS offres;
CREATE TABLE offres (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        entreprise_id BIGINT NOT NULL,
                        titre VARCHAR(150) NOT NULL,
                        description TEXT NOT NULL,
                        missions TEXT,
                        profil_recherche TEXT,
                        competences_requises TEXT,               -- CSV : "Java,Spring,React"
                        competences_plus TEXT,                   -- Compétences optionnelles
                        niveau_requis VARCHAR(100),              -- "Bac+3", "Master", etc.
                        type_contrat ENUM('CDI', 'CDD', 'STAGE', 'ALTERNANCE', 'FREELANCE', 'INTERIM') DEFAULT 'CDI',
                        salaire_min DECIMAL(10,2),
                        salaire_max DECIMAL(10,2),
                        salaire_texte VARCHAR(100),              -- "35-45k€", "Selon profil"
                        ville VARCHAR(100),
                        code_postal VARCHAR(10),
                        pays VARCHAR(50) DEFAULT 'France',
                        teletravail ENUM('AUCUN', 'PARTIEL', 'COMPLET') DEFAULT 'AUCUN',
                        active BOOLEAN DEFAULT TRUE,
                        date_publication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        date_expiration DATE NULL,
                        date_modification TIMESTAMP NULL,
                        nombre_vues INT DEFAULT 0,
                        reference VARCHAR(50),                   -- Référence interne

                        FOREIGN KEY (entreprise_id) REFERENCES entreprises(id) ON DELETE CASCADE,
                        INDEX idx_entreprise (entreprise_id),
                        INDEX idx_active (active),
                        INDEX idx_date (date_publication),
                        INDEX idx_ville (ville),
                        INDEX idx_contrat (type_contrat),
                        FULLTEXT INDEX ft_offre (titre, description, competences_requises)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 7. TABLE DES CANDIDATURES (Matching Service)
-- ============================================
DROP TABLE IF EXISTS candidatures;
CREATE TABLE candidatures (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              candidat_id BIGINT NOT NULL,
                              offre_id BIGINT NOT NULL,
                              score_matching INT DEFAULT 0,            -- Score de 0 à 100
                              score_competences INT DEFAULT 0,         -- Sous-score compétences
                              score_experience INT DEFAULT 0,          -- Sous-score expérience
                              score_formation INT DEFAULT 0,           -- Sous-score formation
                              lettre_motivation TEXT,
                              statut ENUM('EN_ATTENTE', 'VUE', 'ENTRETIEN_PROGRAMME', 'ACCEPTE', 'REFUSE', 'ARCHIVE') DEFAULT 'EN_ATTENTE',
                              date_postulation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              date_modification TIMESTAMP NULL,
                              date_reponse TIMESTAMP NULL,
                              commentaire_entreprise TEXT,             -- Commentaire du recruteur
                              note_interne INT,                        -- Note interne (1-5 étoiles)

                              FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE CASCADE,
                              FOREIGN KEY (offre_id) REFERENCES offres(id) ON DELETE CASCADE,
                              UNIQUE KEY unique_candidature (candidat_id, offre_id),
                              INDEX idx_candidat (candidat_id),
                              INDEX idx_offre (offre_id),
                              INDEX idx_statut (statut),
                              INDEX idx_score (score_matching),
                              INDEX idx_date (date_postulation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 8. TABLE DES ENTRETIENS (Matching Service)
-- ============================================
DROP TABLE IF EXISTS entretiens;
CREATE TABLE entretiens (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            candidature_id BIGINT NOT NULL,
                            type_entretien ENUM('TELEPHONIQUE', 'VIDEO', 'PRESENTIEL', 'TECHNIQUE', 'RH', 'FINAL') DEFAULT 'VIDEO',
                            date_entretien DATETIME NOT NULL,
                            duree INT DEFAULT 60,                    -- Durée en minutes
                            lieu VARCHAR(255),                       -- Adresse ou lien visio (Zoom, Teams, Meet)
                            lien_visio VARCHAR(500),
                            participants VARCHAR(500),               -- Noms des participants côté entreprise
                            notes TEXT,
                            compte_rendu TEXT,
                            statut ENUM('PROGRAMME', 'CONFIRME', 'TERMINE', 'ANNULE', 'REPROGRAMME') DEFAULT 'PROGRAMME',
                            date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            date_modification TIMESTAMP NULL,

                            FOREIGN KEY (candidature_id) REFERENCES candidatures(id) ON DELETE CASCADE,
                            INDEX idx_candidature (candidature_id),
                            INDEX idx_date (date_entretien),
                            INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 9. TABLE DES FAVORIS (Candidats favoris)
-- ============================================
DROP TABLE IF EXISTS favoris;
CREATE TABLE favoris (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         candidat_id BIGINT NOT NULL,
                         offre_id BIGINT NOT NULL,
                         date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE CASCADE,
                         FOREIGN KEY (offre_id) REFERENCES offres(id) ON DELETE CASCADE,
                         UNIQUE KEY unique_favori (candidat_id, offre_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 10. TABLE DES NOTIFICATIONS
-- ============================================
DROP TABLE IF EXISTS notifications;
CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               type ENUM('CANDIDATURE_RECUE', 'CANDIDATURE_ACCEPTEE', 'CANDIDATURE_REFUSEE',
                                   'ENTRETIEN_PROGRAMME', 'OFFRE_EXPIREE', 'PROFIL_VALIDE',
                                   'PROFIL_REJETE', 'MESSAGE', 'AUTRE') DEFAULT 'AUTRE',
                               titre VARCHAR(255) NOT NULL,
                               message TEXT,
                               lu BOOLEAN DEFAULT FALSE,
                               lien VARCHAR(500),                       -- Lien vers la ressource concernée
                               date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               INDEX idx_user (user_id),
                               INDEX idx_lu (lu),
                               INDEX idx_date (date_creation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 11. TABLE DES LOGS D'ACTIVITÉ (RGPD)
-- ============================================
DROP TABLE IF EXISTS logs_activite;
CREATE TABLE logs_activite (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT,
                               action VARCHAR(100) NOT NULL,            -- "CONNEXION", "POSTULER", "MODIFIER_PROFIL", etc.
                               details TEXT,
                               adresse_ip VARCHAR(45),
                               user_agent TEXT,
                               date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                               INDEX idx_user (user_id),
                               INDEX idx_date (date_action),
                               INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 12. TABLE DES CONSENTEMENTS RGPD
-- ============================================
DROP TABLE IF EXISTS consentements_rgpd;
CREATE TABLE consentements_rgpd (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    user_id BIGINT NOT NULL,
                                    type_consentement ENUM(
                                        'DONNEES_PERSONNELLES',
                                        'CV_STOCKAGE',
                                        'PHOTO_STOCKAGE',
                                        'LOGO_STOCKAGE',
                                        'EMAIL_MARKETING',
                                        'PARTAGE_TIERS',
                                        'PROFIL_PUBLIC',
                                        'COLLECTE_STATISTIQUES'
                                        ) NOT NULL,
                                    accepte BOOLEAN NOT NULL DEFAULT TRUE,
                                    date_consentement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    date_retrait TIMESTAMP NULL,
                                    adresse_ip VARCHAR(45),
                                    user_agent TEXT,
                                    preuve_consentement TEXT,                -- Hash ou référence du consentement

                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                    UNIQUE KEY unique_consent (user_id, type_consentement),
                                    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 13. TABLE DES DEMANDES RGPD
-- ============================================
DROP TABLE IF EXISTS demandes_rgpd;
CREATE TABLE demandes_rgpd (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               type_demande ENUM(
                                   'ACCES_DONNEES',                     -- Droit d'accès
                                   'RECTIFICATION',                     -- Droit de rectification
                                   'EFFACEMENT',                        -- Droit à l'oubli
                                   'PORTABILITE',                       -- Portabilité des données
                                   'OPPOSITION',                        -- Droit d'opposition
                                   'LIMITATION'                         -- Limitation du traitement
                                   ) NOT NULL,
                               statut ENUM('EN_ATTENTE', 'EN_COURS', 'TRAITE', 'REFUSE') DEFAULT 'EN_ATTENTE',
                               details TEXT,
                               reponse TEXT,
                               date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               date_traitement TIMESTAMP NULL,
                               traite_par BIGINT,

                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (traite_par) REFERENCES users(id) ON DELETE SET NULL,
                               INDEX idx_user (user_id),
                               INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 14. TABLE DES TOKENS (Blacklist JWT)
-- ============================================
DROP TABLE IF EXISTS tokens_blacklist;
CREATE TABLE tokens_blacklist (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  token_hash VARCHAR(255) NOT NULL UNIQUE,
                                  user_id BIGINT,
                                  date_expiration TIMESTAMP NOT NULL,
                                  date_revocation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  raison VARCHAR(255),

                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                  INDEX idx_token (token_hash),
                                  INDEX idx_expiration (date_expiration)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 15. TABLE DES STATISTIQUES (Vue matérialisée)
-- ============================================
DROP TABLE IF EXISTS stats_quotidiennes;
CREATE TABLE stats_quotidiennes (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    date_stat DATE NOT NULL UNIQUE,
                                    nombre_inscriptions INT DEFAULT 0,
                                    nombre_connexions INT DEFAULT 0,
                                    nombre_offres_publiees INT DEFAULT 0,
                                    nombre_candidatures INT DEFAULT 0,
                                    nombre_entretiens_programmes INT DEFAULT 0,
                                    taux_conversion DECIMAL(5,2) DEFAULT 0,

                                    INDEX idx_date (date_stat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Réactiver les contraintes
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- DONNÉES DE DÉMONSTRATION
-- ============================================

-- Utilisateurs de test
-- Mot de passe : password123 (BCrypt)
INSERT INTO users (email, password, role, enabled, provider) VALUES
                                                                 ('admin@skillmatch.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE, 'LOCAL'),
                                                                 ('paul.dupont@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CANDIDAT', TRUE, 'LOCAL'),
                                                                 ('marie.martin@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CANDIDAT', TRUE, 'LOCAL'),
                                                                 ('rh@techcorp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ENTREPRISE', TRUE, 'LOCAL'),
                                                                 ('recrutement@innovatec.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ENTREPRISE', TRUE, 'LOCAL'),
                                                                 ('candidat.github@email.com', NULL, 'CANDIDAT', TRUE, 'GITHUB'),
                                                                 ('entreprise.google@email.com', NULL, 'ENTREPRISE', TRUE, 'GOOGLE');

-- Candidats
INSERT INTO candidats (user_id, nom, prenom, telephone, adresse, ville, bio, competences, linkedin_url, portfolio_url, niveau_scolaire, diplome, ecole, validation_statut) VALUES
                                                                                                                                                                               (2, 'Dupont', 'Paul', '0612345678', '15 rue de Paris', 'Paris', 'Passionné de développement web et nouvelles technologies. 5 ans d''expérience en développement Full Stack.', 'JavaScript,React,Node.js,TypeScript,Python,Docker,Git,SQL,MongoDB,GraphQL', 'https://linkedin.com/in/pauldupont', 'https://pauldupont.dev', 'Bac+3', 'Licence Informatique', 'Université Paris-Saclay', 'VALIDE'),
                                                                                                                                                                               (3, 'Martin', 'Marie', '0623456789', '8 avenue des Champs-Élysées', 'Paris', 'Data Scientist avec une passion pour l''IA et le Machine Learning.', 'Python,R,TensorFlow,SQL,PowerBI,Tableau,Spark,Hadoop', 'https://linkedin.com/in/mariemartin', NULL, 'Bac+5', 'Master Data Science', 'Polytechnique', 'VALIDE'),
                                                                                                                                                                               (6, 'Dev', 'GitHub', '0601020304', 'Lyon', 'Lyon', 'Développeur passionné par l''open source.', 'Java,Spring,Kubernetes,Docker,Go,Rust', 'https://linkedin.com/in/githubdev', 'https://github.com/devgithub', 'Bac+5', 'Master Informatique', 'EPITA', 'EN_ATTENTE');

-- Expériences
INSERT INTO experiences (candidat_id, poste, entreprise_nom, ville, date_debut, date_fin, en_poste, type_contrat, description, competences_utilisees) VALUES
                                                                                                                                                          (1, 'Développeur Full Stack', 'XYZ Tech', 'Paris', '2022-01-15', '2024-01-14', FALSE, 'CDI', 'Développement d''applications web avec React et Node.js. Mise en place de microservices.', 'React,Node.js,TypeScript,Docker,MongoDB'),
                                                                                                                                                          (1, 'Développeur Frontend', 'WebAgency', 'Lyon', '2020-03-01', '2021-12-31', FALSE, 'CDI', 'Création d''interfaces utilisateur responsives.', 'JavaScript,React,CSS,HTML'),
                                                                                                                                                          (1, 'Stagiaire Développeur', 'StartupTech', 'Lille', '2019-06-01', '2019-08-31', FALSE, 'STAGE', 'Stage de fin d''études en développement web.', 'PHP,Laravel,MySQL'),
                                                                                                                                                          (2, 'Data Scientist', 'DataCorp', 'Paris', '2021-01-01', NULL, TRUE, 'CDI', 'Analyse de données massives et création de modèles prédictifs.', 'Python,R,TensorFlow,SQL,Spark');

-- Formations
INSERT INTO formations (candidat_id, diplome, etablissement, ville, date_debut, date_fin, en_cours, mention) VALUES
                                                                                                                 (1, 'Licence Informatique', 'Université Paris-Saclay', 'Orsay', '2017-09-01', '2020-06-30', FALSE, 'Bien'),
                                                                                                                 (2, 'Master Data Science', 'École Polytechnique', 'Palaiseau', '2019-09-01', '2021-06-30', FALSE, 'Très Bien'),
                                                                                                                 (2, 'Licence Mathématiques', 'Université Lyon 1', 'Lyon', '2016-09-01', '2019-06-30', FALSE, 'Bien');

-- Entreprises
INSERT INTO entreprises (user_id, nom_entreprise, siret, secteur, description, site_web, telephone, contact_email, adresse, ville, taille, validation_statut) VALUES
                                                                                                                                                                  (4, 'TechCorp', '12345678901234', 'Technologie', 'Entreprise innovante spécialisée en solutions web et mobiles. Nous accompagnons nos clients dans leur transformation digitale.', 'https://techcorp.com', '0198765432', 'contact@techcorp.com', '25 rue de l''Innovation', 'Paris', 'PME', 'VALIDE'),
                                                                                                                                                                  (5, 'Innovatec', '98765432109876', 'Intelligence Artificielle', 'Startup spécialisée en IA et analyse de données. Nous développons des solutions sur mesure pour les entreprises.', 'https://innovatec.fr', '0187654321', 'recrutement@innovatec.fr', '10 avenue du Futur', 'Lyon', 'STARTUP', 'VALIDE');

-- Offres d'emploi
INSERT INTO offres (entreprise_id, titre, description, missions, profil_recherche, competences_requises, competences_plus, niveau_requis, type_contrat, salaire_texte, ville, teletravail, active) VALUES
                                                                                                                                                                                                       (1, 'Développeur Web Full Stack',
                                                                                                                                                                                                        'Nous recherchons un développeur web passionné pour rejoindre notre équipe dynamique. Vous travaillerez sur des projets innovants en utilisant les dernières technologies.',
                                                                                                                                                                                                        'Concevoir et développer des applications web\nParticiper à l''architecture des solutions\nCode review et mentorat\nVeille technologique',
                                                                                                                                                                                                        'Vous avez au moins 2 ans d''expérience\nVous maîtrisez JavaScript et ses frameworks\nVous êtes autonome et proactif',
                                                                                                                                                                                                        'JavaScript,React,Node.js,TypeScript',
                                                                                                                                                                                                        'Docker,GraphQL,AWS,Python',
                                                                                                                                                                                                        'Bac+3', 'CDI', '35-45k€', 'Paris', 'PARTIEL', TRUE),

                                                                                                                                                                                                       (1, 'Data Analyst',
                                                                                                                                                                                                        'Nous recherchons un Data Analyst pour analyser nos données et créer des dashboards pertinents. Vous travaillerez en étroite collaboration avec les équipes métier.',
                                                                                                                                                                                                        'Analyser les données clients\nCréer des dashboards et rapports\nIdentifier des tendances et opportunités\nPrésenter les résultats aux équipes',
                                                                                                                                                                                                        'Vous avez une formation en statistiques ou data science\nVous maîtrisez les outils de visualisation\nVous avez un bon sens de l''analyse',
                                                                                                                                                                                                        'Python,SQL,PowerBI,Excel',
                                                                                                                                                                                                        'R,Tableau,Google Analytics',
                                                                                                                                                                                                        'Bac+3', 'CDI', '38-48k€', 'Paris', 'PARTIEL', TRUE),

                                                                                                                                                                                                       (2, 'Ingénieur Machine Learning',
                                                                                                                                                                                                        'Rejoignez notre équipe R&D pour développer des modèles de Machine Learning innovants.',
                                                                                                                                                                                                        'Développer des modèles ML\nOptimiser les algorithmes existants\nParticiper aux publications scientifiques\nCollaborer avec les équipes produit',
                                                                                                                                                                                                        'PhD ou Master en ML/IA\nExpérience en Deep Learning\nPublications scientifiques appréciées',
                                                                                                                                                                                                        'Python,TensorFlow,PyTorch,Keras,Scikit-learn',
                                                                                                                                                                                                        'Spark,Kubernetes,Docker',
                                                                                                                                                                                                        'Bac+5', 'CDI', '50-65k€', 'Lyon', 'COMPLET', TRUE),

                                                                                                                                                                                                       (2, 'Développeur Mobile React Native',
                                                                                                                                                                                                        'Développez des applications mobiles cross-platform pour nos clients.',
                                                                                                                                                                                                        'Développer des apps React Native\nIntégrer des APIs REST\nOptimiser les performances\nParticiper aux sprints agiles',
                                                                                                                                                                                                        'Expérience React Native\nConnaissance iOS et Android\nBon relationnel',
                                                                                                                                                                                                        'JavaScript,React Native,TypeScript,Redux',
                                                                                                                                                                                                        'Swift,Kotlin,Firebase',
                                                                                                                                                                                                        'Bac+3', 'CDI', '40-50k€', 'Lyon', 'PARTIEL', TRUE);

-- Candidatures
INSERT INTO candidatures (candidat_id, offre_id, score_matching, score_competences, score_experience, score_formation, statut) VALUES
                                                                                                                                   (1, 1, 88, 90, 85, 80, 'EN_ATTENTE'),
                                                                                                                                   (1, 2, 75, 70, 75, 80, 'VUE'),
                                                                                                                                   (1, 4, 72, 75, 70, 70, 'ENTRETIEN_PROGRAMME'),
                                                                                                                                   (2, 2, 68, 65, 70, 75, 'EN_ATTENTE'),
                                                                                                                                   (2, 3, 95, 95, 90, 95, 'ACCEPTE'),
                                                                                                                                   (2, 1, 45, 40, 50, 45, 'REFUSE');

-- Entretiens
INSERT INTO entretiens (candidature_id, type_entretien, date_entretien, duree, lieu, lien_visio, statut) VALUES
                                                                                                             (3, 'VIDEO', '2025-01-15 14:00:00', 60, 'Visio', 'https://meet.google.com/abc-defg-hij', 'PROGRAMME'),
                                                                                                             (5, 'PRESENTIEL', '2025-01-20 10:00:00', 90, 'Lyon - 10 avenue du Futur', NULL, 'PROGRAMME');

-- Notifications
INSERT INTO notifications (user_id, type, titre, message, lu) VALUES
                                                                  (2, 'CANDIDATURE_RECUE', 'Nouvelle candidature', 'Votre candidature pour "Développeur Web Full Stack" a bien été envoyée.', FALSE),
                                                                  (2, 'ENTRETIEN_PROGRAMME', 'Entretien programmé', 'Votre entretien pour "Développeur Mobile" est programmé le 15/01/2025.', FALSE),
                                                                  (4, 'CANDIDATURE_RECUE', 'Nouveau candidat', 'Paul Dupont a postulé à l''offre "Développeur Web Full Stack".', FALSE);

-- Consentements RGPD
INSERT INTO consentements_rgpd (user_id, type_consentement, accepte, adresse_ip) VALUES
                                                                                     (2, 'DONNEES_PERSONNELLES', TRUE, '192.168.1.1'),
                                                                                     (2, 'CV_STOCKAGE', TRUE, '192.168.1.1'),
                                                                                     (2, 'PHOTO_STOCKAGE', TRUE, '192.168.1.1'),
                                                                                     (2, 'EMAIL_MARKETING', FALSE, '192.168.1.1'),
                                                                                     (4, 'DONNEES_PERSONNELLES', TRUE, '192.168.1.2'),
                                                                                     (4, 'LOGO_STOCKAGE', TRUE, '192.168.1.2');

-- Logs d'activité
INSERT INTO logs_activite (user_id, action, details, adresse_ip) VALUES
                                                                     (2, 'CONNEXION', 'Connexion réussie', '192.168.1.1'),
                                                                     (2, 'POSTULER', 'Candidature offre #1', '192.168.1.1'),
                                                                     (4, 'CONNEXION', 'Connexion réussie', '192.168.1.2'),
                                                                     (4, 'PUBLIER_OFFRE', 'Publication offre Développeur Web', '192.168.1.2');

-- Stats quotidiennes
INSERT INTO stats_quotidiennes (date_stat, nombre_inscriptions, nombre_connexions, nombre_offres_publiees, nombre_candidatures) VALUES
                                                                                                                                    (CURDATE(), 3, 15, 2, 5),
                                                                                                                                    (DATE_SUB(CURDATE(), INTERVAL 1 DAY), 2, 12, 1, 3),
                                                                                                                                    (DATE_SUB(CURDATE(), INTERVAL 2 DAY), 5, 20, 3, 8);

-- ============================================
-- PROCÉDURES STOCKÉES UTILES
-- ============================================

DELIMITER //

-- Nettoyer les tokens expirés
CREATE PROCEDURE nettoyer_tokens_expires()
BEGIN
    DELETE FROM tokens_blacklist WHERE date_expiration < NOW();
END //

-- Obtenir les statistiques globales
CREATE PROCEDURE get_stats_globales()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM users WHERE enabled = TRUE) AS utilisateurs_actifs,
        (SELECT COUNT(*) FROM candidats WHERE validation_statut = 'VALIDE') AS candidats_valides,
        (SELECT COUNT(*) FROM candidats WHERE validation_statut = 'EN_ATTENTE') AS candidats_en_attente,
        (SELECT COUNT(*) FROM entreprises WHERE validation_statut = 'VALIDE') AS entreprises_valides,
        (SELECT COUNT(*) FROM offres WHERE active = TRUE) AS offres_actives,
        (SELECT COUNT(*) FROM candidatures WHERE statut = 'EN_ATTENTE') AS candidatures_en_attente,
        (SELECT AVG(score_matching) FROM candidatures) AS score_moyen,
        (SELECT COUNT(*) FROM entretiens WHERE statut = 'PROGRAMME') AS entretiens_a_venir;
END //

-- Obtenir le top des compétences recherchées
CREATE PROCEDURE get_top_competences(IN limite INT)
BEGIN
    -- Cette procédure nécessite de parser le CSV des compétences
    -- Version simplifiée qui compte les occurrences
    SELECT
        SUBSTRING_INDEX(SUBSTRING_INDEX(competences_requises, ',', n.n), ',', -1) AS competence,
        COUNT(*) AS nombre
    FROM offres
             CROSS JOIN (
        SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL
        SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL
        SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    ) n
    WHERE n.n <= LENGTH(competences_requises) - LENGTH(REPLACE(competences_requises, ',', '')) + 1
      AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(competences_requises, ',', n.n), ',', -1)) != ''
    GROUP BY competence
    ORDER BY nombre DESC
    LIMIT limite;
END //

DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

DELIMITER //

-- Trigger pour logger les connexions
CREATE TRIGGER after_user_update_login
    AFTER UPDATE ON users
    FOR EACH ROW
BEGIN
    IF NEW.derniere_connexion IS NOT NULL AND
       (OLD.derniere_connexion IS NULL OR NEW.derniere_connexion != OLD.derniere_connexion) THEN
        INSERT INTO logs_activite (user_id, action, details)
        VALUES (NEW.id, 'CONNEXION', CONCAT('Connexion réussie via ', NEW.provider));
    END IF;
END //

-- Trigger pour archiver les candidatures refusées
CREATE TRIGGER after_candidature_refusee
    AFTER UPDATE ON candidatures
    FOR EACH ROW
BEGIN
    IF NEW.statut = 'REFUSE' AND OLD.statut != 'REFUSE' THEN
        -- Créer une notification
        INSERT INTO notifications (user_id, type, titre, message)
        SELECT c.user_id, 'CANDIDATURE_REFUSEE',
               CONCAT('Candidature refusée - ', o.titre),
               CONCAT('Votre candidature pour "', o.titre, '" a été refusée.')
        FROM candidats c
                 JOIN offres o ON o.id = NEW.offre_id
        WHERE c.id = NEW.candidat_id;
    END IF;
END //

DELIMITER ;

-- ============================================
-- VUES
-- ============================================

-- Vue : Candidats avec leurs compétences et expériences
CREATE OR REPLACE VIEW v_candidats_complets AS
SELECT
    c.id,
    c.nom,
    c.prenom,
    c.bio,
    c.competences,
    c.niveau_scolaire,
    c.diplome,
    c.validation_statut,
    u.email,
    u.date_creation,
    COUNT(DISTINCT e.id) AS nombre_experiences,
    COUNT(DISTINCT cand.id) AS nombre_candidatures,
    AVG(cand.score_matching) AS score_matching_moyen
FROM candidats c
         JOIN users u ON u.id = c.user_id
         LEFT JOIN experiences e ON e.candidat_id = c.id
         LEFT JOIN candidatures cand ON cand.candidat_id = c.id
GROUP BY c.id, u.email;

-- Vue : Offres avec infos entreprise
CREATE OR REPLACE VIEW v_offres_completes AS
SELECT
    o.id,
    o.titre,
    o.description,
    o.competences_requises,
    o.niveau_requis,
    o.type_contrat,
    o.salaire_texte,
    o.ville,
    o.teletravail,
    o.date_publication,
    o.active,
    e.nom_entreprise,
    e.secteur,
    e.logo_path,
    COUNT(DISTINCT c.id) AS nombre_candidatures,
    AVG(c.score_matching) AS score_moyen_candidats
FROM offres o
         JOIN entreprises e ON e.id = o.entreprise_id
         LEFT JOIN candidatures c ON c.offre_id = o.id
GROUP BY o.id, e.id;

-- Vue : Dashboard Admin
CREATE OR REPLACE VIEW v_dashboard_admin AS
SELECT
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM candidats WHERE validation_statut = 'VALIDE') AS candidats_valides,
    (SELECT COUNT(*) FROM candidats WHERE validation_statut = 'EN_ATTENTE') AS candidats_attente,
    (SELECT COUNT(*) FROM entreprises WHERE validation_statut = 'VALIDE') AS entreprises_valides,
    (SELECT COUNT(*) FROM offres WHERE active = TRUE) AS offres_actives,
    (SELECT COUNT(*) FROM candidatures WHERE statut = 'EN_ATTENTE') AS candidatures_attente,
    (SELECT COUNT(*) FROM entretiens WHERE statut IN ('PROGRAMME', 'CONFIRME')) AS entretiens_a_venir;

-- ============================================
-- FIN DU SCRIPT
-- ============================================