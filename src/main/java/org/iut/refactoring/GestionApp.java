package org.iut.refactoring;

public class GestionApp {
    public static void main(String[] args) {
        GestionPersonnel gp = new GestionPersonnel();

        gp.ajouteSalarie("DEVELOPPEUR", "Alice", 50000, 6, "IT");
        gp.ajouteSalarie("CHEF DE PROJET", "Bob", 60000, 4, "RH");
        gp.ajouteSalarie("STAGIAIRE", "Charlie", 20000, 0, "IT");
        gp.ajouteSalarie("DEVELOPPEUR", "Dan", 55000, 12, "IT");

        String aliceId = (String) gp.employes.get(0)[0];

        System.out.println("Salaire de Alice: " + gp.calculSalaire(aliceId) + " €");
        System.out.println("Bonus de Alice: " + gp.calculBonusAnnuel(aliceId) + " €");

        gp.generationRapport("SALAIRE", "IT");
        gp.generationRapport("EQUIPE", null);

        gp.avancementEmploye(aliceId, "CHEF DE PROJET");
        gp.printLogs();
    }
}
