package cat.iesesteveterradas;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.util.InputMismatchException;
import java.util.Scanner;

public class PR210Honor {

    public static void main(String[] args) throws SQLException {
        String basePath = System.getProperty("user.dir") + "/data/";
        String filePath = basePath + "honor.db";

        File fDatabase = new File(filePath);
        if (!fDatabase.exists()) {
            initDatabase(filePath);
        }

        Connection conn = UtilsSQLite.connect(filePath);
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.println("\n--- MENÚ ---");
            System.out.println("1. Mostrar una taula");
            System.out.println("2. Mostrar personatges per facció");
            System.out.println("3. Mostrar el millor atacant per facció");
            System.out.println("4. Mostrar el millor defensor per facció");
            System.out.println("5. Sortir");
            System.out.print("Seleccioni una opció: ");

            int option;
            try {
                option = scanner.nextInt();
            }catch(InputMismatchException e) {
                option = 0;
            }

            
            scanner.nextLine();

            switch (option) {
                case 1 -> showTable(conn, scanner);
                case 2 -> showCharactersByFaction(conn, scanner);
                case 3 -> showBestAttackerByFaction(conn, scanner);
                case 4 -> showBestDefenderByFaction(conn, scanner);
                case 5 -> {
                    running = false;
                    System.out.println("Sortint de l'aplicació...");
                }
                default -> System.out.println("Opció no vàlida!");
            }
        }

        UtilsSQLite.disconnect(conn);
        scanner.close();
    }

    static void initDatabase(String filePath) {
        Connection conn = UtilsSQLite.connect(filePath);

        UtilsSQLite.queryUpdate(conn, "DROP TABLE IF EXISTS Personatge;");
        UtilsSQLite.queryUpdate(conn, "DROP TABLE IF EXISTS Faccio;");

        UtilsSQLite.queryUpdate(conn, "CREATE TABLE Faccio (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom VARCHAR(15) NOT NULL," +
                "resum VARCHAR(500) NOT NULL);");

        UtilsSQLite.queryUpdate(conn, "CREATE TABLE Personatge (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom VARCHAR(15) NOT NULL," +
                "atac REAL NOT NULL," +
                "defensa REAL NOT NULL," +
                "idFaccio INTEGER NOT NULL," +
                "FOREIGN KEY(idFaccio) REFERENCES Faccio(id));");

        // Inserció de dades inicials
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Faccio (nom, resum) VALUES (\"Cavallers\", \"Though seen as a single group, the Knights are hardly unified. There are many Legions in Ashfeld, the most prominent being The Iron Legion.\");");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Faccio (nom, resum) VALUES (\"Vikings\",   \"The Vikings are a loose coalition of hundreds of clans and tribes, the most powerful being The Warborn.\");");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Faccio (nom, resum) VALUES (\"Samurais\",  \"The Samurai are the most unified of the three factions, though this does not say much as the Daimyos were often battling each other for dominance.\");");

        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Warden\",      1, 3, 1);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Conqueror\",   2, 2, 1);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Peacekeep\",   2, 3, 1);");

        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Raider\",    3, 3, 2);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Warlord\",   2, 2, 2);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Berserker\", 1, 1, 2);");

        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Kensei\",  3, 2, 3);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Shugoki\", 2, 1, 3);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (\"Orochi\",  3, 2, 3);");

        UtilsSQLite.disconnect(conn);
    }

    static void showTable(Connection conn, Scanner scanner) {
        System.out.print("Introdueixi el nom de la taula a mostrar (Faccio o Personatge): ");
        String tableName = scanner.nextLine();
        if(tableName == "") {
            return;
        }

        try {
            ResultSet rs = UtilsSQLite.querySelect(conn, "SELECT * FROM " + tableName + ";");
            if(rs == null) {
                return;
            }
            ResultSetMetaData rsmd = rs.getMetaData();
            int columns = rsmd.getColumnCount();

            System.out.println("\n--- Contingut de la taula " + tableName + " ---");
            for (int i = 1; i <= columns; i++) {
                System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();

            while (rs.next()) {
                for (int i = 1; i <= columns; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar la taula: " + e.getMessage());
        }
    }

    static void showCharactersByFaction(Connection conn, Scanner scanner) {
        System.out.print("Introdueixi el nom de la facció: ");

        String factionName = scanner.nextLine();
        factionName = factionName.substring(0,1).toUpperCase() + factionName.substring(1).toLowerCase();

        try {
            String sql = "SELECT p.nom, p.atac, p.defensa FROM Personatge p " +
                    "JOIN Faccio f ON p.idFaccio = f.id WHERE f.nom = '" + factionName + "';";
            ResultSet rs = UtilsSQLite.querySelect(conn, sql);

            System.out.println("\n--- Personatges de la facció " + factionName + " ---");
            while (rs.next()) {
                System.out.println("Nom: " + rs.getString("nom") + ", Atac: " + rs.getDouble("atac") + ", Defensa: " + rs.getDouble("defensa"));
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar personatges: " + e.getMessage());
        }
    }

    static void showBestAttackerByFaction(Connection conn, Scanner scanner) {
        System.out.print("Introdueixi el nom de la facció: ");
        String factionName = scanner.nextLine();
        factionName = factionName.substring(0,1).toUpperCase() + factionName.substring(1).toLowerCase();

        try {
            String sql = "SELECT p.nom, MAX(p.atac) as atac FROM Personatge p " +
                    "JOIN Faccio f ON p.idFaccio = f.id WHERE f.nom = '" + factionName + "';";
            ResultSet rs = UtilsSQLite.querySelect(conn, sql);

            if (rs.next()) {
                System.out.println("\nEl millor atacant de la facció " + factionName + " és: " + rs.getString("nom") + ", Atac: " + rs.getDouble("atac"));
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar el millor atacant: " + e.getMessage());
        }
    }

    static void showBestDefenderByFaction(Connection conn, Scanner scanner) {
        System.out.print("Introdueixi el nom de la facció: ");
        String factionName = scanner.nextLine();
        factionName = factionName.substring(0,1).toUpperCase() + factionName.substring(1).toLowerCase();

        try {
            String sql = "SELECT p.nom, MAX(p.defensa) as defensa FROM Personatge p " +
                    "JOIN Faccio f ON p.idFaccio = f.id WHERE f.nom = '" + factionName + "';";
            ResultSet rs = UtilsSQLite.querySelect(conn, sql);

            if (rs.next()) {
                System.out.println("\nEl millor defensor de la facció " + factionName + " és: " + rs.getString("nom") + ", Defensa: " + rs.getDouble("defensa"));
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar el millor defensor: " + e.getMessage());
        }
    }
}
