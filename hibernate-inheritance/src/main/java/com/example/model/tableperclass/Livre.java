package com.example.model.tableperclass;

import javax.persistence.Entity;

@Entity
public class Livre extends Produit {

    private String auteur;

    public Livre() {}

    public Livre(String nom, String auteur) {
        super(nom);
        this.auteur = auteur;
    }

    // getters et setters
}