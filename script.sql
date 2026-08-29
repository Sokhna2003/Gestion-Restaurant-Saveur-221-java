-- =====================================================================
-- Saveur 221 — Script de création de la base de données
-- Base commune au Module A (Java Console) et au Module B (PHP Web)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS restaurant_saveur221
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE saveur221;

-- ---------------------------------------------------------------------
-- Table : roles
-- Rôles du personnel interne (ADMIN, GERANT)
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------
-- Table : utilisateurs
-- Personnel interne (accède au Module A Java) : admin et gérant
-- ---------------------------------------------------------------------
CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_utilisateur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ---------------------------------------------------------------------
-- Table : clients
-- Clients du restaurant (s'inscrivent et commandent via le Module B PHP)
-- ---------------------------------------------------------------------
CREATE TABLE clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    mot_de_passe VARCHAR(255) NOT NULL,
    date_inscription DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Table : categories
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- Table : produits
-- Règle métier : si quantite_stock = 0, disponible doit être mis à FALSE
-- (géré au niveau applicatif, pas par contrainte SQL)
-- ON DELETE RESTRICT : une catégorie contenant des produits ne peut pas
-- être supprimée (règle métier n°9 du sujet)
-- ---------------------------------------------------------------------
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL,
    description TEXT,
    prix DECIMAL(10,2) NOT NULL CHECK (prix >= 0),
    quantite_stock INT NOT NULL DEFAULT 0 CHECK (quantite_stock >= 0),
    seuil_alerte INT NOT NULL DEFAULT 5,
    categorie_id INT NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    image VARCHAR(255),
    CONSTRAINT fk_produit_categorie FOREIGN KEY (categorie_id) REFERENCES categories(id)
        ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Table : commandes
-- statut : EN_ATTENTE -> EN_PREPARATION -> PRETE -> RETIREE, ou ANNULEE
-- ---------------------------------------------------------------------
CREATE TABLE commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    date_commande DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('EN_ATTENTE', 'EN_PREPARATION', 'PRETE', 'RETIREE', 'ANNULEE')
        NOT NULL DEFAULT 'EN_ATTENTE',
    montant_total DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (montant_total >= 0),
    CONSTRAINT fk_commande_client FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- ---------------------------------------------------------------------
-- Table : ligne_commandes
-- prix_unitaire figé au moment de l'achat (ne suit pas les changements
-- de prix ultérieurs du produit)
-- ---------------------------------------------------------------------
CREATE TABLE ligne_commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commande_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL CHECK (quantite > 0),
    prix_unitaire DECIMAL(10,2) NOT NULL CHECK (prix_unitaire >= 0),
    CONSTRAINT fk_ligne_commande FOREIGN KEY (commande_id) REFERENCES commandes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ligne_produit FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- ---------------------------------------------------------------------
-- Table : paiements
-- Règle métier : somme des paiements d'une commande <= montant_total
-- (géré au niveau applicatif)
-- ---------------------------------------------------------------------
CREATE TABLE paiements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commande_id INT NOT NULL,
    montant DECIMAL(10,2) NOT NULL CHECK (montant > 0),
    date_paiement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mode_paiement VARCHAR(50) NOT NULL,
    CONSTRAINT fk_paiement_commande FOREIGN KEY (commande_id) REFERENCES commandes(id)
);

-- ---------------------------------------------------------------------
-- Table : avis
-- Règle métier : un seul avis par commande (contrainte UNIQUE sur commande_id)
-- possible uniquement après le statut RETIREE (géré au niveau applicatif)
-- ---------------------------------------------------------------------
CREATE TABLE avis (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    commande_id INT NOT NULL UNIQUE,
    note INT NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire TEXT,
    date_avis DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avis_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_avis_commande FOREIGN KEY (commande_id) REFERENCES commandes(id)
);

-- =====================================================================
-- Données de départ (seed) — rôles + un compte admin pour les tests
-- Mot de passe en clair ici pour le développement ; à hacher côté
-- application avant tout usage réel (ex: password_hash en PHP, BCrypt en Java)
-- =====================================================================

INSERT INTO roles (libelle) VALUES ('ADMIN'), ('GERANT');

INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role_id, actif)
VALUES ('Diop', 'Awa', 'admin@saveur221.sn', 'admin123', 1, TRUE);

INSERT INTO categories (nom, description) VALUES
    ('Plats', 'Plats principaux du restaurant'),
    ('Boissons', 'Boissons fraîches et chaudes'),
    ('Desserts', 'Desserts et pâtisseries');