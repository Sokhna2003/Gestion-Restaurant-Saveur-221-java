# Saveur 221 — Module A (Java Console)

Application console destinée au **personnel interne** du restaurant (gérant, administrateur).
Partage la même base de données MySQL que le Module B (PHP Web).

## Prérequis

- **JDK 17** (obligatoire — le projet utilise des switch expressions modernes)
- Maven 3.8+
- MySQL (ex: via XAMPP)

## Configuration de la base de données

1. Crée la base en exécutant `database/script.sql` (fourni avec le Module PHP) dans phpMyAdmin ou en ligne de commande MySQL. Ça crée la base **`restaurant_saveur221`** avec ses 9 tables et quelques données de départ (2 rôles, 1 compte admin, 3 catégories).
2. Si tes identifiants MySQL diffèrent de `root` sans mot de passe (par défaut sur XAMPP), adapte-les dans :
   `src/main/java/com/saveur221/config/DatabaseConfig.java`

## Lancer le projet

Depuis la racine du projet (là où se trouve `pom.xml`) :

```bash
mvn clean package
java -jar target/saveur221-console.jar
```

La première commande compile le projet et génère un jar exécutable (avec le connecteur MySQL inclus). La seconde lance l'application.

## Compte de test

À la connexion, utilise le compte admin créé par le script SQL :

- **Email** : `admin@saveur221.sn`
- **Mot de passe** : `admin123`

D'autres comptes de test (GERANT, compte désactivé) sont disponibles dans les scripts de données de test fournis séparément.

## Lancer les tests unitaires

```bash
mvn test
```

## Structure du projet

```
src/main/java/com/saveur221/
├── Main.java          # point d'entrée : login -> menu principal
├── config/            # connexion base de données, hachage des mots de passe
├── entities/           # POJOs : Role, Utilisateur, Client, Categorie, Produit,
│                        Commande, LigneCommande, Paiement, Avis, ProduitVendu
├── enums/               # StatutCommande, RoleType
├── exceptions/           # exceptions métier (BusinessException et filles)
├── repository/           # accès aux données (JDBC pur, sans ORM)
├── service/              # logique métier et règles de gestion
└── view/                 # menus console (affichage/saisie uniquement)
```

### Règle d'architecture importante

Seul `MenuPrincipalView.java` a le droit d'appeler la couche `Service`.
Toutes les autres classes de `view/` (CategorieView, ProduitView, CommandeView,
UtilisateurView, StatistiqueView, PaiementView) se limitent à l'affichage et à
la saisie : elles ne connaissent aucun Service, aucun Repository, aucune
connexion base de données.

## Fonctionnalités implémentées

- **Authentification** : connexion par email/mot de passe (haché SHA-256),
  vérification du compte actif, droits selon le rôle (ADMIN/GERANT)
- **Catégories** : CRUD complet, suppression bloquée si des produits l'utilisent
- **Produits** : CRUD complet, recherche, filtrage par catégorie/disponibilité
- **Stock** : approvisionnement, seuil d'alerte, produits en stock faible/rupture
  (un produit à 0 en stock devient automatiquement indisponible)
- **Commandes** *(consultées et gérées côté Java, créées côté client PHP)* :
  liste, filtrage par statut, recherche, détail, changement de statut pas à
  pas (EN_ATTENTE → EN_PREPARATION → PRETE → RETIREE), annulation avec
  restitution automatique du stock
- **Paiements** : commandes impayées/partiellement payées, enregistrement
  d'un paiement (jamais plus que le solde restant), historique par commande
- **Statistiques** : chiffre d'affaires jour/semaine/mois, nombre de
  commandes, commandes en cours, répartition par statut, produit le plus
  vendu, top 3 des produits
- **Utilisateurs** *(espace ADMIN uniquement)* : CRUD, changement de mot de
  passe, activation/désactivation, protection contre l'auto-suppression

