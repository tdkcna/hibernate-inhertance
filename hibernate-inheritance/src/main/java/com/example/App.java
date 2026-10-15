package com.example;
import org.h2.tools.Server;

import com.example.model.joined.Developpeur;
import com.example.model.joined.Manager;
import com.example.model.singletable.Moto;
import com.example.model.singletable.Voiture;
import com.example.model.tableperclass.Electronique;
import com.example.model.tableperclass.Livre;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-inheritance");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        // SINGLE_TABLE
        Voiture voiture = new Voiture("Mervedes", 4);
        Moto moto = new Moto("trois", true);
        em.persist(voiture);
        em.persist(moto);

        // JOINED
        Developpeur dev = new Developpeur("ZAID", "Java");
        Manager manager = new Manager("Youssed", 3);
        em.persist(dev);
        em.persist(manager);

        // TABLE_PER_CLASS
        Livre livre = new Livre("l etranger", "Robert C. Martin");
        Electronique electronique = new Electronique("iphooone", 24);
        em.persist(livre);
        em.persist(electronique);

        em.getTransaction().commit();

        // Lecture des entités
        System.out.println("--- Liste des véhicules ---");
        em.createQuery("SELECT v FROM Vehicule v", com.example.model.singletable.Vehicule.class)
                .getResultList().forEach(v -> System.out.println(v.getClass().getSimpleName() + ": " + v.getMarque()));

        System.out.println("--- Liste des employés ---");
        em.createQuery("SELECT e FROM Employe e", com.example.model.joined.Employe.class)
                .getResultList().forEach(e -> System.out.println(e.getClass().getSimpleName() + ": " + e.getNom()));

        System.out.println("--- Liste des produits ---");
        em.createQuery("SELECT p FROM Produit p", com.example.model.tableperclass.Produit.class)
                .getResultList().forEach(p -> System.out.println(p.getClass().getSimpleName() + ": " + p.getNom()));

        em.close();
        emf.close();



        try {
            Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println("Console H2 disponible sur : http://localhost:8082");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage de la console H2");
            e.printStackTrace();
        }

    }

}
