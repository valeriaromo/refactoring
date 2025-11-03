package org.iut.refactoring;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class GestionPersonnelTest {

    private GestionPersonnel gp;

    // Pour capturer les prints de System.out
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        gp = new GestionPersonnel();
        // ajouter quelques employés utilisés dans plusieurs tests
        gp.ajouteSalarie("DEVELOPPEUR", "Alice", 50000.0, 6, "IT");
        gp.ajouteSalarie("CHEF DE PROJET", "Bob", 60000.0, 4, "RH");
        gp.ajouteSalarie("STAGIAIRE", "Charlie", 20000.0, 0, "IT");
        gp.ajouteSalarie("DEVELOPPEUR", "Dan", 55000.0, 12, "IT");
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // Aide : trouver l'id d'un employé par nom
    private String findIdByName(String name) {
        for (Object[] emp : gp.employes) {
            if (name.equals(emp[2])) {
                return (String) emp[0];
            }
        }
        return null;
    }

    @Test
    void testAjouteSalarie_populatesEmployesAndSalairesAndLogs() {
        int initialCount = gp.employes.size();
        gp.ajouteSalarie("DEVELOPPEUR", "Eve", 45000.0, 2, "FIN");
        assertEquals(initialCount + 1, gp.employes.size(), "Un employé supplémentaire doit être ajouté");

        String eveId = findIdByName("Eve");
        assertNotNull(eveId, "Eve doit exister");

        assertTrue(gp.salairesEmployes.containsKey(eveId), "salairesEmployes doit contenir la clé");

        assertFalse(gp.logs.isEmpty(), "Les logs ne doivent pas être vides");
        boolean containsEve = gp.logs.stream().anyMatch(s -> s.contains("Eve") && s.contains("Ajout"));
        assertTrue(containsEve, "Un log de création pour Eve doit exister");
    }

    @Test
    void testCalculSalaire_developpeur_experienceBetween6And10() {
        String aliceId = findIdByName("Alice");
        assertNotNull(aliceId);

        double expected = 50000.0 * 1.2 * 1.15;
        double actual = gp.calculSalaire(aliceId);
        assertEquals(expected, actual, 0.0001, "Le salaire d'Alice doit correspondre");
    }

    @Test
    void testCalculSalaire_developpeur_experienceGreaterThan10() {
        String danId = findIdByName("Dan");
        assertNotNull(danId);

        double expected = 55000.0 * 1.2 * 1.15 * 1.05;
        double actual = gp.calculSalaire(danId);
        assertEquals(expected, actual, 0.0001, "Le salaire de Dan (exp >10) doit inclure le bonus supplémentaire");
    }

    @Test
    void testCalculSalaire_chefDeProjet_bonus5000() {
        String bobId = findIdByName("Bob");
        assertNotNull(bobId);

        double expected = 60000.0 * 1.5 * 1.1 + 5000.0;
        double actual = gp.calculSalaire(bobId);
        assertEquals(expected, actual, 0.0001, "Le salaire de Bob doit inclure le bonus et les multiplicateurs");
    }

    @Test
    void testCalculSalaire_stagiaire() {
        String charlieId = findIdByName("Charlie");
        assertNotNull(charlieId);

        double expected = 20000.0 * 0.6;
        double actual = gp.calculSalaire(charlieId);
        assertEquals(expected, actual, 0.0001, "Le salaire de Charlie doit être 60% du salaire de base");
    }

    @Test
    void testSalairesEmployesInitial_vs_calculSalaire_diferenciasSegunMetodoDeAñadir() {
        String bobId = findIdByName("Bob");
        String danId = findIdByName("Dan");

        assertNotNull(bobId);
        assertNotNull(danId);

        double storedBob = gp.salairesEmployes.get(bobId);
        double recomputedBob = gp.calculSalaire(bobId);
        assertEquals(60000.0 * 1.5 * 1.1, storedBob, 0.0001);
        assertEquals(104000.0, recomputedBob, 0.0001);

        double storedDan = gp.salairesEmployes.get(danId);
        double recomputedDan = gp.calculSalaire(danId);
        assertEquals(55000.0 * 1.2 * 1.15, storedDan, 0.0001);
        assertEquals(55000.0 * 1.2 * 1.15 * 1.05, recomputedDan, 0.0001);
    }

    @Test
    void testCalculSalaire_nonexistentId_printsErrorAndReturnsZero() {
        System.setOut(new PrintStream(outContent));
        double val = gp.calculSalaire("id-que-no-existe");
        String printed = outContent.toString();
        assertEquals(0.0, val, 0.0001, "Doit retourner 0 pour un id inexistant");
        assertTrue(printed.contains("ERREUR: impossible de trouver l'employé"), "Doit afficher un message d'erreur");
    }

    @Test
    void testCalculBonusAnnuel_variosCasos() {
        String aliceId = findIdByName("Alice");
        String bobId = findIdByName("Bob");
        String charlieId = findIdByName("Charlie");
        String danId = findIdByName("Dan");

        assertEquals(50000.0 * 0.1 * 1.5, gp.calculBonusAnnuel(aliceId), 0.0001, "Bonus Alice");
        assertEquals(60000.0 * 0.2 * 1.3, gp.calculBonusAnnuel(bobId), 0.0001, "Bonus Bob");
        assertEquals(0.0, gp.calculBonusAnnuel(charlieId), 0.0001, "Bonus Charlie doit être 0");
        assertEquals(55000.0 * 0.1 * 1.5, gp.calculBonusAnnuel(danId), 0.0001, "Bonus Dan");
    }

    @Test
    void testAvancementEmploye_updatesTypeAndSalairesEmployesAndLogs() {
        String aliceId = findIdByName("Alice");
        assertNotNull(aliceId);

        double before = gp.calculSalaire(aliceId);
        assertEquals(50000.0 * 1.2 * 1.15, before, 0.0001);

        gp.avancementEmploye(aliceId, "CHEF DE PROJET");

        double expectedAfter = 50000.0 * 1.5 * 1.1 + 5000.0;
        double after = gp.calculSalaire(aliceId);
        assertEquals(expectedAfter, after, 0.0001, "Le salaire après promotion doit correspondre au nouveau type");

        assertTrue(gp.salairesEmployes.containsKey(aliceId));
        assertEquals(after, gp.salairesEmployes.get(aliceId), 0.0001, "La map des salaires doit être mise à jour après promotion");

        boolean hasPromotionLog = gp.logs.stream().anyMatch(s -> s.contains("Employé promu") && s.contains("Alice"));
        assertTrue(hasPromotionLog, "Un log de promotion pour Alice doit exister");
    }

    @Test
    void testAvancementEmploye_nonexistent_printsError() {
        System.setOut(new PrintStream(outContent));
        gp.avancementEmploye("id-no-existe-xyz", "DEVELOPPEUR");
        String printed = outContent.toString();
        assertTrue(printed.contains("ERREUR: impossible de trouver l'employé"), "Doit afficher une erreur pour un id inexistant");
    }

    @Test
    void testGetEmployesParDivision_returnsCorrectList() {
        ArrayList<Object[]> itEmps = gp.getEmployesParDivision("IT");
        assertEquals(3, itEmps.size(), "Doit retourner 3 employés pour la division IT");
        boolean containsAlice = itEmps.stream().anyMatch(e -> "Alice".equals(e[2]));
        boolean containsDan = itEmps.stream().anyMatch(e -> "Dan".equals(e[2]));
        boolean containsCharlie = itEmps.stream().anyMatch(e -> "Charlie".equals(e[2]));
        assertTrue(containsAlice && containsDan && containsCharlie, "La liste IT doit contenir Alice, Dan et Charlie");
    }

    @Test
    void testGetEmployesParDivision_emptyWhenNone() {
        ArrayList<Object[]> ventes = gp.getEmployesParDivision("VENTES");
        assertNotNull(ventes);
        assertTrue(ventes.isEmpty(), "Si aucun employé, doit retourner une liste vide");
    }

    @Test
    void testGenerationRapport_salaire_filterIT_printsOnlyIT() {
        System.setOut(new PrintStream(outContent));
        gp.generationRapport("SALAIRE", "IT");
        String output = outContent.toString();

        assertTrue(output.contains("Alice:"), "Doit contenir Alice");
        assertTrue(output.contains("Charlie:"), "Doit contenir Charlie");
        assertTrue(output.contains("Dan:"), "Doit contenir Dan");
        assertFalse(output.contains("Bob:"), "Ne doit pas contenir Bob avec filtre IT");
    }

    @Test
    void testGenerationRapport_experience_noFilter_printsAll() {
        System.setOut(new PrintStream(outContent));
        gp.generationRapport("EXPERIENCE", null);
        String output = outContent.toString();

        assertTrue(output.contains("Alice:"), "Doit contenir Alice");
        assertTrue(output.contains("Bob:"), "Doit contenir Bob");
        assertTrue(output.contains("Charlie:"), "Doit contenir Charlie");
        assertTrue(output.contains("Dan:"), "Doit contenir Dan");
    }

    @Test
    void testGenerationRapport_division_counts() {
        System.setOut(new PrintStream(outContent));
        gp.generationRapport("DIVISION", null);
        String output = outContent.toString();

        assertTrue(output.contains("IT: 3"), "Doit indiquer que IT a 3 employés");
        assertTrue(output.contains("RH: 1"), "Doit indiquer que RH a 1 employé");
    }

    @Test
    void testGenerationRapport_unknownType_stillAddsLog() {
        int logsBefore = gp.logs.size();
        System.setOut(new PrintStream(outContent));
        gp.generationRapport("EQUIPE", null);
        int logsAfter = gp.logs.size();
        assertEquals(logsBefore + 1, logsAfter, "Même pour un type inconnu, un log doit être ajouté");

        boolean hasReportLog = gp.logs.stream().anyMatch(s -> s.contains("Rapport généré") && s.contains("EQUIPE"));
        assertTrue(hasReportLog, "Doit exister un log pour le rapport EQUIPE");
    }

    @Test
    void testPrintLogs_outputsAllLogs() {
        gp.logs.add("TESTLOG - exemple");
        System.setOut(new PrintStream(outContent));
        gp.printLogs();
        String output = outContent.toString();
        assertTrue(output.contains("=== LOGS ==="), "Doit afficher l'entête des logs");
        assertTrue(output.contains("TESTLOG - exemple"), "Doit afficher le log ajouté");
    }

    @Test
    void testMultipleAddAndIntegrity() {
        for (int i = 0; i < 20; i++) {
            gp.ajouteSalarie("DEVELOPPEUR", "Dev" + i, 30000 + i * 1000, i % 7, "Team" + (i % 4));
        }
        long uniqueIds = gp.employes.stream().map(e -> (String) e[0]).distinct().count();
        assertEquals(gp.employes.size(), uniqueIds, "Tous les ids doivent être uniques");

        assertEquals(gp.employes.size(), gp.salairesEmployes.size(), "La map des salaires doit avoir le même nombre que d'employés");
    }

    @Test
    void testEdgeCase_zeroExperience_and_zeroSalary() {
        gp.ajouteSalarie("DEVELOPPEUR", "Zero", 0.0, 0, "Z");
        String id = findIdByName("Zero");
        assertNotNull(id);
        double salary = gp.calculSalaire(id);
        assertEquals(0.0, salary, 0.0001, "Salaire doit etre 0 si base est 0");

        double bonus = gp.calculBonusAnnuel(id);
        assertEquals(0.0, bonus, 0.0001, "Bonus doit etre 0 si base est 0");
    }
}
