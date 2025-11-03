package org.iut.refactoring;

class GestionApp {
    public static void main(String[] args) {
        GestionPersonnel app = new GestionPersonnel();
        
        app.ajouteSalarie("DEVELOPPEUR", "Alice", 50000, 6, "IT");
        app.ajouteSalarie("CHEF DE PROJET", "Bob", 60000, 4, "RH");
        app.ajouteSalarie("STAGIAIRE", "Charlie", 20000, 0, "IT");
        app.ajouteSalarie("DEVELOPPEUR", "Dan", 55000, 12, "IT");
        
        String aliceId = (String) app.employes.get(0)[0];
        
        System.out.println("Salaire de Alice: " + app.calculSalaire(aliceId) + " €");
        System.out.println("Bonus de Alice: " + app.calculBonusAnnuel(aliceId) + " €");
        
        app.generationRapport("SALAIRE", "IT");
        app.generationRapport("EQUIPE", null);
        
        app.avancementEmploye(aliceId, "CHEF DE PROJET");
        
        app.printLogs();
    }
}
