package com.saveur221.entities;

/**
 * Client du restaurant (s'inscrit et commande via le Module B PHP).
 * Le Module A (Java) a seulement besoin de le consulter (ex: historique),
 * jamais de le créer/modifier — ça, c'est le rôle du site web.
 */
public class Client {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String motDePasse;
    private String photo;

    public Client() {
    }

    public Client(int id, String nom, String prenom, String email,
                  String telephone, String adresse, String motDePasse) {
        this(id, nom, prenom, email, telephone, adresse, motDePasse, null);
    }

    public Client(int id, String nom, String prenom, String email,
                  String telephone, String adresse, String motDePasse, String photo) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.motDePasse = motDePasse;
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return prenom + " " + nom + " <" + email + ">";
    }
}
