package ru.projectmanagement;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProjectManagementSystem {

    private static final String DB_URL = "jdbc:sqlite:project_management.db";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            createTables(connection);
            seedDatabase(connection);
            showMenu(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String createProjectsTable = "CREATE TABLE IF NOT EXISTS projects (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL);";

            String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "project_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "responsible_id INTEGER NOT NULL, " +
                    "start_date TEXT NOT NULL, " +
                    "duration INTEGER NOT NULL, " +
                    "completed BOOLEAN NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY (project_id) REFERENCES projects(id), " +
                    "FOREIGN KEY (responsible_id) REFERENCES responsibles(id));";

            String createResponsiblesTable = "CREATE TABLE IF NOT EXISTS responsibles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "contact TEXT NOT NULL);";

            stmt.execute(createProjectsTable);
            stmt.execute(createResponsiblesTable);
            stmt.execute(createTasksTable);
        }
    }

    private static void seedDatabase(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT OR IGNORE INTO projects (id, name) VALUES " +
                    "(1, 'Проект Альфа'), " +
                    "(2, 'Проект Бета'), " +
                    "(3, 'Проект Гамма'), " +
                    "(4, 'Проект Дельта'), " +
                    "(5, 'Проект Эпсилон'), " +
                    "(6, 'Проект Зета'), " +
                    "(7, 'Проект Эта'), " +
                    "(8, 'Проект Тета'), " +
                    "(9, 'Проект Йота'), " +
                    "(10, 'Проект Каппа');");

            stmt.execute("INSERT OR IGNORE INTO responsibles (id, name, contact) VALUES " +
                    "(1, 'Алиса', 'alice@example.com'), " +
                    "(2, 'Боб', 'bob@example.com'), " +
                    "(3, 'Иван', 'ivan@example.com'), " +
                    "(4, 'Мария', 'maria@example.com'), " +
                    "(5, 'Дмитрий', 'dmitry@example.com'), " +
                    "(6, 'Сергей', 'sergey@example.com'), " +
                    "(7, 'Екатерина', 'ekaterina@example.com'), " +
                    "(8, 'Анна', 'anna@example.com'), " +
                    "(9, 'Пётр', 'petr@example.com'), " +
                    "(10, 'Ольга', 'olga@example.com');");

            stmt.execute("INSERT OR IGNORE INTO tasks (id, project_id, name, responsible_id, start_date, duration, completed) VALUES " +
                    "(1, 1, 'Задача 1', 1, '2025-01-01', 10, 0), " +
                    "(2, 2, 'Задача 2', 2, '2025-01-05', 5, 0), " +
                    "(3, 3, 'Задача 3', 3, '2025-01-10', 7, 1), " +
                    "(4, 4, 'Задача 4', 4, '2025-01-12', 15, 0), " +
                    "(5, 5, 'Задача 5', 5, '2025-01-15', 20, 0), " +
                    "(6, 6, 'Задача 6', 6, '2025-01-20', 8, 0), " +
                    "(7, 7, 'Задача 7', 7, '2025-01-22', 10, 0), " +
                    "(8, 8, 'Задача 8', 8, '2025-01-25', 5, 1), " +
                    "(9, 9, 'Задача 9', 9, '2025-01-28', 12, 0), " +
                    "(10, 10, 'Задача 10', 10, '2025-01-30', 9, 0);");
        }
    }

    private static void showMenu(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("\n=== Система управления проектами ===");
        System.out.println("1. Посмотреть проекты в работе");
        System.out.println("2. Количество незавершённых задач по проектам");
        System.out.println("3. Посмотреть незавершённые задачи по ответственному");
        System.out.println("4. Задачи на сегодня");
        System.out.println("5. Просроченные задачи и контакты ответственных");
        System.out.println("6. Выход");

        System.out.print("Выберите опцию: ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> showProjectsInProgress(connection);
            case 2 -> showUnfinishedTaskCount(connection);
            case 3 -> showUnfinishedTasksByResponsible(connection);
            case 4 -> showTodaysTasks(connection);
            case 5 -> showOverdueTasks(connection);
            case 6 -> System.exit(0);
            default -> {
                System.out.println("Неверный выбор.");
                showMenu(connection);
            }
        }
        showMenu(connection);
    }

    private static void showProjectsInProgress(Connection connection) throws SQLException {
        String query = "SELECT DISTINCT p.name FROM projects p JOIN tasks t ON p.id = t.project_id WHERE t.completed = 0;";
        try (PreparedStatement stmt = connection.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            System.out.println("Проекты в работе:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("name"));
            }
        }
    }

    private static void showUnfinishedTaskCount(Connection connection) throws SQLException {
        String query = "SELECT p.name, COUNT(t.id) AS unfinished_tasks FROM projects p JOIN tasks t ON p.id = t.project_id WHERE t.completed = 0 GROUP BY p.name;";
        try (PreparedStatement stmt = connection.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            System.out.println("Количество незавершённых задач по проектам:");
            while (rs.next()) {
                System.out.println(rs.getString("name") + ": " + rs.getInt("unfinished_tasks") + " задач");
            }
        }
    }

    private static void showUnfinishedTasksByResponsible(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.print("Введите имя ответственного: ");
        String responsibleName = scanner.nextLine();
        String query = "SELECT t.name FROM tasks t JOIN responsibles r ON t.responsible_id = r.id WHERE r.name = ? AND t.completed = 0;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, responsibleName);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Незавершённые задачи для " + responsibleName + ":");
                while (rs.next()) {
                    System.out.println("- " + rs.getString("name"));
                }
            }
        }
    }

    private static void showTodaysTasks(Connection connection) throws SQLException {
        String today = LocalDate.now().toString();
        String query = "SELECT t.name, r.name AS responsible_name FROM tasks t JOIN responsibles r ON t.responsible_id = r.id WHERE t.start_date <= ? AND t.completed = 0;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, today);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Задачи на сегодня:");
                while (rs.next()) {
                    System.out.println(rs.getString("name") + " (Ответственный: " + rs.getString("responsible_name") + ")");
                }
            }
        }
    }

    private static void showOverdueTasks(Connection connection) throws SQLException {
        String today = LocalDate.now().toString();
        String query = "SELECT t.name, r.name AS responsible_name, r.contact FROM tasks t JOIN responsibles r ON t.responsible_id = r.id WHERE t.completed = 0 AND DATE(t.start_date, '+' || t.duration || ' days') < ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, today);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Просроченные задачи и контакты ответственных:");
                while (rs.next()) {
                    System.out.println(rs.getString("name") + " (Ответственный: " + rs.getString("responsible_name") + ", Контакт: " + rs.getString("contact") + ")");
                }
            }
        }
    }
}
