package org.iut.refactoring;

import java.util.*;
import java.time.*;

public class GestionPersonnel {

    // champs publics pour compatibilité avec les tests
    public ArrayList<Object[]> employes = new ArrayList<>();
    public HashMap<String, Double> salairesEmployes = new HashMap<>();
    public ArrayList<String> logs = new ArrayList<>();



    public void ajouteSalarie(String type, String nom, double salaireDeBase, int experience, String equipe) {
        String id = UUID.randomUUID().toString();
        Object[] emp = new Object[]{id, type, nom, salaireDeBase, experience, equipe};
        employes.add(emp);

        double salaire = calculSalaireInitial(type, salaireDeBase, experience);
        salairesEmployes.put(id, salaire);

        logs.add(LocalDateTime.now() + " - Ajout de l'employé: " + nom);
    }

    public double calculSalaire(String employeId) {
        Object[] emp = trouverEmploye(employeId);
        if (emp == null) {
            System.out.println("ERREUR: impossible de trouver l'employé");
            return 0.0;
        }

        String type = (String) emp[1];
        double salaireDeBase = (double) emp[3];
        int experience = (int) emp[4];

        return calculSalaireComplet(type, salaireDeBase, experience);
    }

    public void avancementEmploye(String employeId, String newType) {
        Object[] emp = trouverEmploye(employeId);
        if (emp == null) {
            System.out.println("ERREUR: impossible de trouver l'employé");
            return;
        }

        emp[1] = newType;
        double nouveauSalaire = calculSalaire(employeId);
        salairesEmployes.put(employeId, nouveauSalaire);
        logs.add(LocalDateTime.now() + " - Employé promu: " + emp[2]);
        System.out.println("Employé promu avec succès!");
    }

    public ArrayList<Object[]> getEmployesParDivision(String division) {
        ArrayList<Object[]> resultat = new ArrayList<>();
        for (Object[] emp : employes) {
            if (emp[5].equals(division)) {
                resultat.add(emp);
            }
        }
        return resultat;
    }

    public void generationRapport(String typeRapport, String filtre) {
        System.out.println("=== RAPPORT: " + typeRapport + " ===");
        switch (typeRapport) {
            case "SALAIRE" -> genererRapportSalaire(filtre);
            case "EXPERIENCE" -> genererRapportExperience(filtre);
            case "DIVISION" -> genererRapportDivision();
            default -> System.out.println("Type de rapport inconnu.");
        }
        logs.add(LocalDateTime.now() + " - Rapport généré: " + typeRapport);
    }

    public void printLogs() {
        System.out.println("=== LOGS ===");
        for (String log : logs) {
            System.out.println(log);
        }
    }

    public double calculBonusAnnuel(String employeId) {
        Object[] emp = trouverEmploye(employeId);
        if (emp == null) return 0.0;

        String type = (String) emp[1];
        double salaireBase = (double) emp[3];
        int experience = (int) emp[4];

        return switch (type) {
            case "DEVELOPPEUR" -> salaireBase * 0.1 * (experience > 5 ? 1.5 : 1.0);
            case "CHEF DE PROJET" -> salaireBase * 0.2 * (experience > 3 ? 1.3 : 1.0);
            case "STAGIAIRE" -> 0.0;
            default -> 0.0;
        };
    }


    private Object[] trouverEmploye(String id) {
        for (Object[] emp : employes) {
            if (emp[0].equals(id)) {
                return emp;
            }
        }
        return null;
    }

    private double calculSalaireInitial(String type, double base, int exp) {
        return switch (type) {
            case "DEVELOPPEUR" -> base * 1.2 * (exp > 5 ? 1.15 : 1.0);
            case "CHEF DE PROJET" -> base * 1.5 * (exp > 3 ? 1.1 : 1.0);
            case "STAGIAIRE" -> base * 0.6;
            default -> base;
        };
    }

    private double calculSalaireComplet(String type, double base, int exp) {
        double salaire = base;
        switch (type) {
            case "DEVELOPPEUR" -> {
                salaire = base * 1.2;
                if (exp > 5) salaire *= 1.15;
                if (exp > 10) salaire *= 1.05;
            }
            case "CHEF DE PROJET" -> {
                salaire = base * 1.5;
                if (exp > 3) salaire *= 1.1;
                salaire += 5000;
            }
            case "STAGIAIRE" -> salaire = base * 0.6;
            default -> {}
        }
        return salaire;
    }

    private void genererRapportSalaire(String filtre) {
        for (Object[] emp : employes) {
            if (filtre == null || filtre.isEmpty() || emp[5].equals(filtre)) {
                String id = (String) emp[0];
                String nom = (String) emp[2];
                double salaire = calculSalaire(id);
                System.out.println(nom + ": " + salaire + " €");
            }
        }
    }

    private void genererRapportExperience(String filtre) {
        for (Object[] emp : employes) {
            if (filtre == null || filtre.isEmpty() || emp[5].equals(filtre)) {
                String nom = (String) emp[2];
                int exp = (int) emp[4];
                System.out.println(nom + ": " + exp + " années");
            }
        }
    }

    private void genererRapportDivision() {
        HashMap<String, Integer> compteur = new HashMap<>();
        for (Object[] emp : employes) {
            String div = (String) emp[5];
            compteur.put(div, compteur.getOrDefault(div, 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : compteur.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " employés");
        }
    }
}
