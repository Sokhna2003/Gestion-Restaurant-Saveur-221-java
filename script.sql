-- =====================================================================
-- Saveur 221 — Script de création de la base de données
-- Base commune au Module A (Java Console) et au Module B (PHP Web)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS restaurant_saveur221
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_saveur221;

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


UPDATE utilisateurs 
SET mot_de_passe = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9' 
WHERE email = 'admin@saveur221.sn';

INSERT INTO produits (libelle, description, prix, quantite_stock, seuil_alerte, categorie_id, disponible, image) VALUES
('Thieboudienne', 'Riz au poisson, sauce tomate, légumes', 3500, 20, 5, 1, TRUE, NULL),
('Yassa Poulet', 'Poulet mariné au citron et oignons', 3000, 15, 5, 1, TRUE, NULL),
('Mafé', 'Viande sauce arachide', 3200, 3, 5, 1, TRUE, NULL),          -- stock faible
('Poisson braisé', 'Poisson grillé accompagné de frites', 4000, 0, 5, 1, FALSE, NULL), -- rupture
('Bissap', 'Jus d''hibiscus glacé', 500, 30, 10, 2, TRUE, NULL),
('Bouye', 'Jus de pain de singe', 500, 25, 10, 2, TRUE, NULL),
('Coca-Cola', 'Canette 33cl', 600, 2, 10, 2, TRUE, NULL),              -- stock faible
('Thiakry', 'Dessert au mil et lait caillé', 1000, 12, 5, 3, TRUE, NULL),
('Salade de fruits', 'Fruits frais de saison', 1500, 0, 5, 3, FALSE, NULL); -- rupture


-- Données de test : clients + commandes dans différents statuts
-- À exécuter après avoir déjà les produits en base
-- Utilise des sous-requêtes sur le libellé des produits, donc fonctionne
-- même si les id réels diffèrent chez toi.

-- ---------------------------------------------------------------------
-- Clients de test (mot de passe en clair pour simplifier les tests console ;
-- à hacher côté PHP comme pour les utilisateurs internes)
-- ---------------------------------------------------------------------
INSERT INTO clients (nom, prenom, email, telephone, adresse, mot_de_passe) VALUES
('Diallo', 'Fatou', 'fatou.diallo@test.sn', '771234567', 'Dakar, Sénégal', 'test1234'),
('Ndiaye', 'Moussa', 'moussa.ndiaye@test.sn', '781234567', 'Thiès, Sénégal', 'test1234'),
('Sow', 'Awa', 'awa.sow@test.sn', '761234567', 'Rufisque, Sénégal', 'test1234');

-- ---------------------------------------------------------------------
-- Commande 1 : EN_ATTENTE (Fatou) - 2x Thieboudienne + 1x Bissap
-- ---------------------------------------------------------------------
INSERT INTO commandes (client_id, statut, montant_total)
VALUES ((SELECT id FROM clients WHERE email = 'fatou.diallo@test.sn'), 'EN_ATTENTE', 0);
SET @cmd1 = LAST_INSERT_ID();

INSERT INTO ligne_commandes (commande_id, produit_id, quantite, prix_unitaire)
VALUES
(@cmd1, (SELECT id FROM produits WHERE libelle = 'Thieboudienne'), 2,
    (SELECT prix FROM produits WHERE libelle = 'Thieboudienne')),
(@cmd1, (SELECT id FROM produits WHERE libelle = 'Bissap'), 1,
    (SELECT prix FROM produits WHERE libelle = 'Bissap'));

UPDATE commandes SET montant_total =
    (SELECT SUM(quantite * prix_unitaire) FROM ligne_commandes WHERE commande_id = @cmd1)
    WHERE id = @cmd1;

-- ---------------------------------------------------------------------
-- Commande 2 : EN_PREPARATION (Moussa) - 1x Yassa Poulet
-- ---------------------------------------------------------------------
INSERT INTO commandes (client_id, statut, montant_total)
VALUES ((SELECT id FROM clients WHERE email = 'moussa.ndiaye@test.sn'), 'EN_PREPARATION', 0);
SET @cmd2 = LAST_INSERT_ID();

INSERT INTO ligne_commandes (commande_id, produit_id, quantite, prix_unitaire)
VALUES
(@cmd2, (SELECT id FROM produits WHERE libelle = 'Yassa Poulet'), 1,
    (SELECT prix FROM produits WHERE libelle = 'Yassa Poulet'));

UPDATE commandes SET montant_total =
    (SELECT SUM(quantite * prix_unitaire) FROM ligne_commandes WHERE commande_id = @cmd2)
    WHERE id = @cmd2;

-- ---------------------------------------------------------------------
-- Commande 3 : PRETE (Awa) - 3x Thiakry
-- ---------------------------------------------------------------------
INSERT INTO commandes (client_id, statut, montant_total)
VALUES ((SELECT id FROM clients WHERE email = 'awa.sow@test.sn'), 'PRETE', 0);
SET @cmd3 = LAST_INSERT_ID();

INSERT INTO ligne_commandes (commande_id, produit_id, quantite, prix_unitaire)
VALUES
(@cmd3, (SELECT id FROM produits WHERE libelle = 'Thiakry'), 3,
    (SELECT prix FROM produits WHERE libelle = 'Thiakry'));

UPDATE commandes SET montant_total =
    (SELECT SUM(quantite * prix_unitaire) FROM ligne_commandes WHERE commande_id = @cmd3)
    WHERE id = @cmd3;

-- ---------------------------------------------------------------------
-- Commande 4 : RETIREE (Fatou) - 1x Mafé
-- Utile pour tester qu'on NE PEUT PAS changer son statut ni l'annuler
-- ---------------------------------------------------------------------
INSERT INTO commandes (client_id, statut, montant_total)
VALUES ((SELECT id FROM clients WHERE email = 'fatou.diallo@test.sn'), 'RETIREE', 0);
SET @cmd4 = LAST_INSERT_ID();

INSERT INTO ligne_commandes (commande_id, produit_id, quantite, prix_unitaire)
VALUES
(@cmd4, (SELECT id FROM produits WHERE libelle = 'Mafé'), 1,
    (SELECT prix FROM produits WHERE libelle = 'Mafé'));

UPDATE commandes SET montant_total =
    (SELECT SUM(quantite * prix_unitaire) FROM ligne_commandes WHERE commande_id = @cmd4)
    WHERE id = @cmd4;


-- Utilisateurs internes de test supplémentaires
-- Mots de passe déjà hachés en SHA-256 :
--   gerant123 -> 0adea017a51a0224047865ad5b90b53289a93f01ef1b798ef8ae079b3c161640
--   admin456  -> becf77f3ec82a43422b7712134d1860e3205c6ce778b08417a7389b43f2b4661

USE restaurant_saveur221;

INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role_id, actif) VALUES
('Faye', 'Ibrahima', 'ibrahima.faye@saveur221.sn',
    '0adea017a51a0224047865ad5b90b53289a93f01ef1b798ef8ae079b3c161640',
    (SELECT id FROM roles WHERE libelle = 'GERANT'), TRUE),
('Ba', 'Khady', 'khady.ba@saveur221.sn',
    'becf77f3ec82a43422b7712134d1860e3205c6ce778b08417a7389b43f2b4661',
    (SELECT id FROM roles WHERE libelle = 'ADMIN'), TRUE),
('Sarr', 'Modou', 'modou.sarr@saveur221.sn',
    '0adea017a51a0224047865ad5b90b53289a93f01ef1b798ef8ae079b3c161640',
    (SELECT id FROM roles WHERE libelle = 'GERANT'), FALSE); -- compte désactivé, pour tester le refus de connexion

