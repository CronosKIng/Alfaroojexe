import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.http.*;
import java.net.URI;
import com.google.gson.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class DesktopApp {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private String currentUser = null;
    private String currentRole = null;
    private String currentDepartment = null;
    private int currentUserId = 0;
    private String currentFullName = null;
    
    private static final String API_URL = "https://alfarooj.pythonanywhere.com/api/";
    private static final ZoneId UAE_TIMEZONE = ZoneId.of("Asia/Dubai");
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea logArea;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JLabel statusLabel;
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        EventQueue.invokeLater(() -> {
            try {
                DesktopApp window = new DesktopApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    public DesktopApp() {
        initialize();
    }
    
    private void initialize() {
        frame = new JFrame("AL FAROOJ AL SHAMI - Time Table System");
        frame.setBounds(100, 100, 1300, 850);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        
        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        JMenu languageMenu = new JMenu("Language");
        String[] languages = {"English", "Kiswahili", "Arabic", "French", "Spanish"};
        String[] languageCodes = {"en", "sw", "ar", "fr", "es"};
        
        for (int i = 0; i < languages.length; i++) {
            final String code = languageCodes[i];
            JMenuItem langItem = new JMenuItem(languages[i]);
            langItem.addActionListener(e -> changeLanguage(code));
            languageMenu.add(langItem);
        }
        menuBar.add(languageMenu);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);
        
        frame.setJMenuBar(menuBar);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createSuperAdminPanel(), "super_admin");
        mainPanel.add(createAdminPanel(), "admin");
        mainPanel.add(createUserPanel(), "user");
        
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        // Initialize statusLabel (FIXED - was null)
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        frame.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        
        cardLayout.show(mainPanel, "login");
    }
    
    private void changeLanguage(String langCode) {
        JOptionPane.showMessageDialog(frame, "Language changed to " + langCode, "Language", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(frame,
            "AL FAROOJ AL SHAMI Time Table System\nVersion 2.0\n\nFeatures:\n- Super Admin Dashboard\n- Admin Dashboard\n- User Dashboard\n- Location Validation\n- Attendance Tracking\n- User Management\n- Multi-department Support\n- Multi-language Support",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(33, 150, 243));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("AL FAROOJ AL SHAMI");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JLabel subtitleLabel = new JLabel("TIME TABLE SYSTEM");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);
        
        gbc.gridy = 2;
        panel.add(Box.createVerticalStrut(30), gbc);
        
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 3;
        panel.add(userLabel, gbc);
        
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(passLabel, gbc);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        
        JButton loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.addActionListener(e -> login());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);
        
        usernameField.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
        
        return panel;
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (statusLabel != null) statusLabel.setText("Logging in...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = false;
            private String role = "";
            private String fullName = "";
            private int userId = 0;
            private String department = "";
            private String errorMsg = "";
            
            @Override
            protected Void doInBackground() {
                try {
                    String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
                    String response = sendPostRequest(API_URL + "login", json);
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    if (jsonResponse.get("success").getAsBoolean()) {
                        success = true;
                        JsonObject user = jsonResponse.getAsJsonObject("user");
                        role = user.get("role").getAsString();
                        fullName = user.get("full_name").getAsString();
                        userId = user.get("id").getAsInt();
                        department = user.has("department") ? user.get("department").getAsString() : "";
                        currentUser = username;
                        currentRole = role;
                        currentUserId = userId;
                        currentDepartment = department;
                        currentFullName = fullName;
                    } else {
                        errorMsg = jsonResponse.get("message").getAsString();
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (statusLabel != null) statusLabel.setText("Ready");
                if (success) {
                    JOptionPane.showMessageDialog(frame, "Welcome " + fullName + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    if (role.equals("super_admin")) {
                        cardLayout.show(mainPanel, "super_admin");
                        loadUsers();
                    } else if (role.equals("admin")) {
                        cardLayout.show(mainPanel, "admin");
                        loadUsers();
                    } else {
                        cardLayout.show(mainPanel, "user");
                        initUserPanel();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Login failed: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private JPanel createSuperAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(21, 101, 192));
        topPanel.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("SUPER ADMIN DASHBOARD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setBackground(new Color(21, 101, 192));
        JLabel userLabel = new JLabel("Logged in as: " + currentFullName);
        userLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userLabel);
        
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.addActionListener(e -> logout());
        userInfoPanel.add(logoutBtn);
        
        topPanel.add(userInfoPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("MANAGE USERS", createUsersPanel(true));
        tabbedPane.addTab("ALL HISTORY", createHistoryPanel(null));
        tabbedPane.addTab("KITCHEN HISTORY", createHistoryPanel("kitchen"));
        tabbedPane.addTab("WAITER HISTORY", createHistoryPanel("waiter"));
        tabbedPane.addTab("DELIVERY HISTORY", createHistoryPanel("delivery"));
        tabbedPane.addTab("MANAGER HISTORY", createHistoryPanel("manager"));
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(46, 125, 50));
        topPanel.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("ADMIN DASHBOARD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setBackground(new Color(46, 125, 50));
        JLabel userLabel = new JLabel("Logged in as: " + currentFullName);
        userLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userLabel);
        
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.addActionListener(e -> logout());
        userInfoPanel.add(logoutBtn);
        
        topPanel.add(userInfoPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("MANAGE USERS", createUsersPanel(false));
        tabbedPane.addTab("ALL HISTORY", createHistoryPanel(null));
        tabbedPane.addTab("KITCHEN HISTORY", createHistoryPanel("kitchen"));
        tabbedPane.addTab("WAITER HISTORY", createHistoryPanel("waiter"));
        tabbedPane.addTab("DELIVERY HISTORY", createHistoryPanel("delivery"));
        tabbedPane.addTab("MANAGER HISTORY", createHistoryPanel("manager"));
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 81, 0));
        topPanel.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("USER DASHBOARD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setBackground(new Color(230, 81, 0));
        JLabel userLabel = new JLabel("User: " + currentFullName);
        userLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userLabel);
        
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.addActionListener(e -> logout());
        userInfoPanel.add(logoutBtn);
        
        topPanel.add(userInfoPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonsPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        JButton signInBtn = new JButton("SIGN IN");
        signInBtn.setBackground(new Color(76, 175, 80));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFont(new Font("Arial", Font.BOLD, 16));
        signInBtn.setPreferredSize(new Dimension(180, 70));
        signInBtn.addActionListener(e -> recordAttendance("sign_in", "Sign In"));
        buttonsPanel.add(signInBtn);
        
        JButton signOutBtn = new JButton("SIGN OUT");
        signOutBtn.setBackground(new Color(244, 67, 54));
        signOutBtn.setForeground(Color.WHITE);
        signOutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        signOutBtn.setPreferredSize(new Dimension(180, 70));
        signOutBtn.addActionListener(e -> recordAttendance("sign_out", "Sign Out"));
        buttonsPanel.add(signOutBtn);
        
        JButton historyBtn = new JButton("HISTORY");
        historyBtn.setBackground(new Color(156, 39, 176));
        historyBtn.setForeground(Color.WHITE);
        historyBtn.setFont(new Font("Arial", Font.BOLD, 16));
        historyBtn.setPreferredSize(new Dimension(180, 70));
        historyBtn.addActionListener(e -> showUserHistory());
        buttonsPanel.add(historyBtn);
        
        centerPanel.add(buttonsPanel);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Activity Log"));
        scrollPane.setPreferredSize(new Dimension(0, 150));
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void initUserPanel() {
        if (logArea != null) {
            logArea.setText("");
            String time = LocalDateTime.now(UAE_TIMEZONE).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + time + "] Welcome " + currentFullName + "\n");
        }
    }
    
    private JPanel createUsersPanel(boolean canCreateAdmin) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton createUserBtn = new JButton("CREATE USER");
        createUserBtn.addActionListener(e -> showCreateUserDialog("user"));
        toolBar.add(createUserBtn);
        
        if (canCreateAdmin) {
            JButton createAdminBtn = new JButton("CREATE ADMIN");
            createAdminBtn.addActionListener(e -> showCreateUserDialog("admin"));
            toolBar.add(createAdminBtn);
        }
        
        toolBar.addSeparator();
        
        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.addActionListener(e -> loadUsers());
        toolBar.add(refreshBtn);
        
        JButton deleteBtn = new JButton("DELETE SELECTED");
        deleteBtn.setBackground(new Color(244, 67, 54));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSelectedUser());
        toolBar.add(deleteBtn);
        
        panel.add(toolBar, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Full Name", "Username", "Role", "Department", "Created At"};
        userTableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(userTableModel);
        userTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        
        loadUsers();
        
        return panel;
    }
    
    private void loadUsers() {
        if (statusLabel != null) statusLabel.setText("Loading users...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Object[][] userData;
            
            @Override
            protected Void doInBackground() {
                try {
                    String response = sendGetRequest(API_URL + "users");
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    if (jsonResponse.get("success").getAsBoolean()) {
                        JsonArray users = jsonResponse.getAsJsonArray("users");
                        userData = new Object[users.size()][6];
                        for (int i = 0; i < users.size(); i++) {
                            JsonObject user = users.get(i).getAsJsonObject();
                            userData[i][0] = user.get("id").getAsInt();
                            userData[i][1] = user.get("full_name").getAsString();
                            userData[i][2] = user.get("username").getAsString();
                            userData[i][3] = user.get("role").getAsString();
                            userData[i][4] = user.has("department") ? user.get("department").getAsString() : "";
                            userData[i][5] = user.has("created_at") ? user.get("created_at").getAsString() : "";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (userTableModel != null) {
                    userTableModel.setRowCount(0);
                    if (userData != null) {
                        for (Object[] row : userData) {
                            userTableModel.addRow(row);
                        }
                    }
                }
                if (statusLabel != null) statusLabel.setText("Ready");
            }
        };
        worker.execute();
    }
    
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a user to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(frame, "Delete user: " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (statusLabel != null) statusLabel.setText("Deleting user...");
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        sendDeleteRequest(API_URL + "delete_user/" + userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    if (statusLabel != null) statusLabel.setText("Ready");
                    JOptionPane.showMessageDialog(frame, "User deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                }
            };
            worker.execute();
        }
    }
    
    private JPanel createHistoryPanel(String department) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JToolBar toolBar = new JToolBar();
        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.addActionListener(e -> loadHistoryTable(department));
        toolBar.add(refreshBtn);
        panel.add(toolBar, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Username", "Full Name", "Department", "Event", "Location", "Timestamp"};
        historyTableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(historyTableModel);
        historyTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        loadHistoryTable(department);
        
        return panel;
    }
    
    private void loadHistoryTable(String department) {
        if (statusLabel != null) statusLabel.setText("Loading history...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Object[][] historyData;
            
            @Override
            protected Void doInBackground() {
                try {
                    String url = API_URL + "attendance_logs";
                    if (department != null) {
                        url += "?department=" + department;
                    }
                    String response = sendGetRequest(url);
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    if (jsonResponse.get("success").getAsBoolean()) {
                        JsonArray logs = jsonResponse.getAsJsonArray("logs");
                        historyData = new Object[logs.size()][7];
                        for (int i = 0; i < logs.size(); i++) {
                            JsonObject log = logs.get(i).getAsJsonObject();
                            historyData[i][0] = log.get("id").getAsInt();
                            historyData[i][1] = log.get("username").getAsString();
                            historyData[i][2] = log.get("full_name").getAsString();
                            historyData[i][3] = log.get("department").getAsString();
                            historyData[i][4] = log.get("event_name").getAsString();
                            historyData[i][5] = log.get("location").getAsString();
                            historyData[i][6] = log.get("timestamp").getAsString();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (historyTableModel != null) {
                    historyTableModel.setRowCount(0);
                    if (historyData != null) {
                        for (Object[] row : historyData) {
                            historyTableModel.addRow(row);
                        }
                    }
                }
                if (statusLabel != null) statusLabel.setText("Ready");
            }
        };
        worker.execute();
    }
    
    private void showCreateUserDialog(String role) {
        JDialog dialog = new JDialog(frame, "Create " + (role.equals("admin") ? "Admin" : "User"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField fullNameField = new JTextField(20);
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> departmentCombo = new JComboBox<>(new String[]{"kitchen", "waiter", "delivery", "manager"});
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(fullNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        dialog.add(departmentCombo, gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton createBtn = new JButton("CREATE");
        JButton cancelBtn = new JButton("CANCEL");
        
        createBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String department = (String) departmentCombo.getSelectedItem();
            
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            createUser(fullName, username, password, role, department, dialog);
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    
    private void createUser(String fullName, String username, String password, String role, String department, JDialog dialog) {
        if (statusLabel != null) statusLabel.setText("Creating user...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = false;
            private String message = "";
            
            @Override
            protected Void doInBackground() {
                try {
                    String json = String.format("{\"full_name\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"department\":\"%s\",\"created_by\":%d}",
                        fullName, username, password, role, department, currentUserId);
                    String response = sendPostRequest(API_URL + "create_user", json);
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    success = jsonResponse.get("success").getAsBoolean();
                    message = jsonResponse.get("message").getAsString();
                } catch (Exception e) {
                    message = e.getMessage();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (statusLabel != null) statusLabel.setText("Ready");
                if (success) {
                    JOptionPane.showMessageDialog(dialog, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed: " + message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void recordAttendance(String eventType, String eventName) {
        double latitude = 25.3065744;
        double longitude = 55.4573264;
        String location = "Al Farooj Al Shami Restaurant - Sharjah, UAE";
        
        String json = String.format("{\"user_id\":%d,\"username\":\"%s\",\"full_name\":\"%s\",\"department\":\"%s\",\"event_type\":\"%s\",\"event_name\":\"%s\",\"latitude\":%f,\"longitude\":%f,\"location\":\"%s\"}",
            currentUserId, currentUser, currentFullName, currentDepartment, eventType, eventName, latitude, longitude, location);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = false;
            private String message = "";
            
            @Override
            protected Void doInBackground() {
                try {
                    String response = sendPostRequest(API_URL + "attendance", json);
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    success = jsonResponse.get("success").getAsBoolean();
                    message = jsonResponse.get("message").getAsString();
                } catch (Exception e) {
                    message = e.getMessage();
                }
                return null;
            }
            
            @Override
            protected void done() {
                String time = LocalDateTime.now(UAE_TIMEZONE).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                if (success) {
                    if (logArea != null) {
                        logArea.append("[" + time + "] " + eventName + " recorded\n");
                    }
                    JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    if (logArea != null) {
                        logArea.append("[" + time + "] Failed: " + message + "\n");
                    }
                    JOptionPane.showMessageDialog(frame, "Failed: " + message, "Error", JOptionPane.ERROR_MESSAGE);
                }
                if (statusLabel != null) statusLabel.setText("Ready");
            }
        };
        worker.execute();
    }
    
    private void showUserHistory() {
        JDialog historyDialog = new JDialog(frame, "My Attendance History", true);
        historyDialog.setSize(800, 500);
        historyDialog.setLocationRelativeTo(frame);
        
        String[] columns = {"ID", "Event", "Department", "Location", "Timestamp"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    String response = sendGetRequest(API_URL + "attendance_logs");
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    if (jsonResponse.get("success").getAsBoolean()) {
                        JsonArray logs = jsonResponse.getAsJsonArray("logs");
                        for (int i = 0; i < logs.size(); i++) {
                            JsonObject log = logs.get(i).getAsJsonObject();
                            if (log.get("username").getAsString().equals(currentUser)) {
                                model.addRow(new Object[]{
                                    log.get("id").getAsInt(),
                                    log.get("event_name").getAsString(),
                                    log.get("department").getAsString(),
                                    log.get("location").getAsString(),
                                    log.get("timestamp").getAsString()
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                historyDialog.add(new JScrollPane(table));
                historyDialog.setVisible(true);
            }
        };
        worker.execute();
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentUser = null;
            currentRole = null;
            currentUserId = 0;
            currentDepartment = null;
            currentFullName = null;
            usernameField.setText("");
            passwordField.setText("");
            cardLayout.show(mainPanel, "login");
            if (statusLabel != null) statusLabel.setText("Ready");
        }
    }
    
    private String sendPostRequest(String urlString, String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    private String sendGetRequest(String urlString) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    private String sendDeleteRequest(String urlString) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .DELETE()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
