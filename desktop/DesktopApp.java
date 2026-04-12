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
    private int currentUserId = 0;
    private String currentFullName = null;
    
    private static final String API_URL = "https://alfarooj.pythonanywhere.com/api/";
    private static final ZoneId UAE_TIMEZONE = ZoneId.of("Asia/Dubai");
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JToggleButton toggleEyeBtn;
    private boolean passwordVisible = false;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JLabel statusLabel;
    private JComboBox<String> languageCombo;
    
    private final String[] LANGUAGES = {
        "English", "Kiswahili", "Arabic", "French", "Spanish", 
        "German", "Italian", "Portuguese", "Russian", "Chinese",
        "Japanese", "Korean", "Hindi", "Turkish", "Dutch",
        "Greek", "Vietnamese", "Thai", "Polish", "Ukrainian"
    };
    
    private final String[] LANGUAGE_CODES = {
        "en", "sw", "ar", "fr", "es", "de", "it", "pt", "ru", "zh",
        "ja", "ko", "hi", "tr", "nl", "el", "vi", "th", "pl", "uk"
    };
    
    private String currentLanguage = "en";
    private Map<String, String> translationCache = new HashMap<>();
    
    private JLabel titleLabel, subtitleLabel, userLabel, passLabel, langLabel, logoLabel;
    private JButton loginButton;
    private JTabbedPane tabbedPane;
    
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
    
    private String translate(String text) {
        if (text == null || text.isEmpty()) return text;
        if (currentLanguage.equals("en")) return text;
        
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        
        try {
            String translated = translateViaAPI(text);
            translationCache.put(cacheKey, translated);
            return translated;
        } catch (Exception e) {
            return text;
        }
    }
    
    private String translateViaAPI(String text) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String json = String.format("{\"text\":\"%s\",\"target_lang\":\"%s\",\"source_lang\":\"en\"}", 
            escapeJson(text), currentLanguage);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "translate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        
        if (jsonResponse.get("success").getAsBoolean()) {
            return jsonResponse.get("translated").getAsString()
                .replace("&#39;", "'").replace("&quot;", "\"");
        }
        return text;
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                   .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
    
    private void changeLanguage(String langCode) {
        currentLanguage = langCode;
        translationCache.clear();
        updateAllUIText();
        JOptionPane.showMessageDialog(frame, translate("Language changed to ") + langCode, 
            translate("Language"), JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateAllUIText() {
        if (titleLabel != null) titleLabel.setText(translate("AL FAROOJ AL SHAMI"));
        if (subtitleLabel != null) subtitleLabel.setText(translate("Time Table"));
        if (userLabel != null) userLabel.setText(translate("Username:"));
        if (passLabel != null) passLabel.setText(translate("Password:"));
        if (langLabel != null) langLabel.setText(translate("Select Language:"));
        if (loginButton != null) loginButton.setText(translate("LOGIN"));
        if (statusLabel != null) statusLabel.setText(translate("Ready"));
        
        if (usernameField != null) {
            usernameField.putClientProperty("placeholder", translate("Enter your username"));
        }
        if (passwordField != null) {
            passwordField.putClientProperty("placeholder", translate("Enter your password"));
        }
        
        if (tabbedPane != null && tabbedPane.getTabCount() >= 7) {
            tabbedPane.setTitleAt(0, translate("MANAGE USERS"));
            tabbedPane.setTitleAt(1, translate("TODAY ATTENDANCE"));
            tabbedPane.setTitleAt(2, translate("ALL HISTORY"));
            tabbedPane.setTitleAt(3, translate("KITCHEN HISTORY"));
            tabbedPane.setTitleAt(4, translate("WAITER HISTORY"));
            tabbedPane.setTitleAt(5, translate("DELIVERY HISTORY"));
            tabbedPane.setTitleAt(6, translate("MANAGER HISTORY"));
        }
        
        frame.repaint();
    }
    
    private void initialize() {
        frame = new JFrame("AL FAROOJ AL SHAMI - Super Admin Dashboard");
        frame.setBounds(100, 100, 1300, 850);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        
        // Set application icon
        try {
            File iconFile = new File("desktop/icons/hgd.ico");
            if (iconFile.exists()) {
                frame.setIconImage(new ImageIcon(iconFile.getAbsolutePath()).getImage());
            }
        } catch (Exception e) {}
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createSuperAdminPanel(), "super_admin");
        
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        frame.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        
        cardLayout.show(mainPanel, "login");
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(33, 150, 243));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Logo - try to load from file, fallback to text
        logoLabel = new JLabel();
        try {
            File logoFile = new File("desktop/icons/log.png");
            if (!logoFile.exists()) logoFile = new File("desktop/icons/hgd.ico");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon(logoFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(img));
            } else {
                logoLabel.setText("AL FAROOJ");
                logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                logoLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            logoLabel.setText("AL FAROOJ");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            logoLabel.setForeground(Color.WHITE);
        }
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(logoLabel, gbc);
        
        // Title
        titleLabel = new JLabel("AL FAROOJ AL SHAMI");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        panel.add(titleLabel, gbc);
        
        // Subtitle
        subtitleLabel = new JLabel("SUPER ADMIN DASHBOARD");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.WHITE);
        gbc.gridy = 2;
        panel.add(subtitleLabel, gbc);
        
        gbc.gridy = 3;
        panel.add(Box.createVerticalStrut(20), gbc);
        
        // Username label
        userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 4; gbc.gridwidth = 1; gbc.gridx = 0;
        panel.add(userLabel, gbc);
        
        // Username field
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.putClientProperty("placeholder", "Enter your username");
        usernameField.setForeground(Color.BLACK);
        usernameField.setBackground(Color.WHITE);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        // Password label
        passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(passLabel, gbc);
        
        // Password panel with show/hide
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(new Color(33, 150, 243));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.putClientProperty("placeholder", "Enter your password");
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        passwordField.setEchoChar('*');
        
        toggleEyeBtn = new JToggleButton("Show");
        toggleEyeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggleEyeBtn.setPreferredSize(new Dimension(45, 35));
        toggleEyeBtn.setBackground(Color.WHITE);
        toggleEyeBtn.setFocusPainted(false);
        toggleEyeBtn.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            passwordField.setEchoChar(passwordVisible ? (char) 0 : '*');
            toggleEyeBtn.setText(passwordVisible ? "Hide" : "Show");
        });
        
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toggleEyeBtn, BorderLayout.EAST);
        
        gbc.gridx = 1;
        panel.add(passwordPanel, gbc);
        
        // Language panel
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        langPanel.setBackground(new Color(33, 150, 243));
        
        langLabel = new JLabel("Select Language:");
        langLabel.setForeground(Color.WHITE);
        langLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        langPanel.add(langLabel);
        
        languageCombo = new JComboBox<>(LANGUAGES);
        languageCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        languageCombo.setPreferredSize(new Dimension(140, 30));
        languageCombo.setBackground(Color.WHITE);
        languageCombo.addActionListener(e -> {
            int idx = languageCombo.getSelectedIndex();
            if (idx >= 0) {
                changeLanguage(LANGUAGE_CODES[idx]);
            }
        });
        langPanel.add(languageCombo);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(langPanel, gbc);
        
        // Login button
        loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(33, 150, 243));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(250, 45));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        loginButton.addActionListener(e -> login());
        
        gbc.gridy = 7;
        panel.add(loginButton, gbc);
        
        usernameField.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
        
        return panel;
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, translate("Please enter username and password"), 
                translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        statusLabel.setText(translate("Logging in..."));
        loginButton.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = false;
            private String role = "";
            private String fullName = "";
            private int userId = 0;
            private String errorMsg = "";
            
            @Override
            protected Void doInBackground() {
                try {
                    String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
                    String response = sendPostRequest(API_URL + "login", json);
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    if (jsonResponse.get("success").getAsBoolean()) {
                        JsonObject user = jsonResponse.getAsJsonObject("user");
                        role = user.get("role").getAsString();
                        
                        if (!role.equals("super_admin")) {
                            errorMsg = "Access denied. Super Admin only!";
                            return null;
                        }
                        
                        success = true;
                        fullName = user.get("full_name").getAsString();
                        userId = user.get("id").getAsInt();
                        currentUser = username;
                        currentUserId = userId;
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
                loginButton.setEnabled(true);
                statusLabel.setText(translate("Ready"));
                if (success) {
                    JOptionPane.showMessageDialog(frame, translate("Welcome ") + fullName + "!", 
                        translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(mainPanel, "super_admin");
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(frame, translate("Login failed: ") + errorMsg, 
                        translate("Error"), JOptionPane.ERROR_MESSAGE);
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
        
        JLabel dashboardTitle = new JLabel("SUPER ADMIN DASHBOARD", SwingConstants.CENTER);
        dashboardTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dashboardTitle.setForeground(Color.WHITE);
        topPanel.add(dashboardTitle, BorderLayout.CENTER);
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setBackground(new Color(21, 101, 192));
        JLabel userLabel = new JLabel("Logged in as: " + currentFullName);
        userLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userLabel);
        
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.setBackground(new Color(244, 67, 54));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> logout());
        userInfoPanel.add(logoutBtn);
        
        topPanel.add(userInfoPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("MANAGE USERS", createUsersPanel(true));
        tabbedPane.addTab("TODAY ATTENDANCE", createHistoryPanel("today"));
        tabbedPane.addTab("ALL HISTORY", createHistoryPanel("all"));
        tabbedPane.addTab("KITCHEN HISTORY", createHistoryPanel("kitchen"));
        tabbedPane.addTab("WAITER HISTORY", createHistoryPanel("waiter"));
        tabbedPane.addTab("DELIVERY HISTORY", createHistoryPanel("delivery"));
        tabbedPane.addTab("MANAGER HISTORY", createHistoryPanel("manager"));
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
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
        statusLabel.setText(translate("Loading users..."));
        
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
                statusLabel.setText(translate("Ready") + " - " + (userData != null ? userData.length : 0) + " " + translate("users"));
            }
        };
        worker.execute();
    }
    
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, translate("Please select a user to delete"), 
                translate("Warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(frame, translate("Delete user: ") + username + "?", 
            translate("Confirm"), JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            statusLabel.setText(translate("Deleting user..."));
            
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
                    statusLabel.setText(translate("Ready"));
                    JOptionPane.showMessageDialog(frame, translate("User deleted successfully"), 
                        translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                }
            };
            worker.execute();
        }
    }
    
    private JPanel createHistoryPanel(String filterType) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JToolBar toolBar = new JToolBar();
        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.addActionListener(e -> loadHistoryTable(filterType));
        toolBar.add(refreshBtn);
        panel.add(toolBar, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Username", "Full Name", "Department", "Event", "Comment", "Order Type", "Location", "Timestamp"};
        historyTableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(historyTableModel);
        historyTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        loadHistoryTable(filterType);
        
        return panel;
    }
    
    private void loadHistoryTable(String filterType) {
        statusLabel.setText(translate("Loading history..."));
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Object[][] historyData;
            
            @Override
            protected Void doInBackground() {
                try {
                    String url;
                    if (filterType.equals("today")) {
                        url = API_URL + "today_attendance";
                    } else if (filterType.equals("all")) {
                        url = API_URL + "attendance_logs";
                    } else {
                        url = API_URL + "attendance_logs?department=" + filterType;
                    }
                    
                    String response = sendGetRequest(url);
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    if (jsonResponse.get("success").getAsBoolean()) {
                        JsonArray logs = jsonResponse.getAsJsonArray("logs");
                        historyData = new Object[logs.size()][9];
                        for (int i = 0; i < logs.size(); i++) {
                            JsonObject log = logs.get(i).getAsJsonObject();
                            historyData[i][0] = log.get("id").getAsInt();
                            historyData[i][1] = log.get("username").getAsString();
                            historyData[i][2] = log.get("full_name").getAsString();
                            historyData[i][3] = log.has("department") ? log.get("department").getAsString() : "";
                            historyData[i][4] = log.get("event_name").getAsString();
                            historyData[i][5] = log.has("comment") && !log.get("comment").isJsonNull() ? log.get("comment").getAsString() : "";
                            historyData[i][6] = log.has("order_type") && !log.get("order_type").isJsonNull() ? log.get("order_type").getAsString() : "";
                            historyData[i][7] = log.has("location") ? log.get("location").getAsString() : "";
                            historyData[i][8] = log.get("timestamp").getAsString();
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
                statusLabel.setText(translate("Ready") + " - " + (historyData != null ? historyData.length : 0) + " " + translate("records"));
            }
        };
        worker.execute();
    }
    
    private void showCreateUserDialog(String role) {
        JDialog dialog = new JDialog(frame, translate(role.equals("admin") ? "Create Admin" : "Create User"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField fullNameField = new JTextField(20);
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> departmentCombo = new JComboBox<>(new String[]{"kitchen", "waiter", "delivery", "manager"});
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel(translate("Full Name:")), gbc);
        gbc.gridx = 1; dialog.add(fullNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel(translate("Username:")), gbc);
        gbc.gridx = 1; dialog.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel(translate("Password:")), gbc);
        gbc.gridx = 1; dialog.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel(translate("Department:")), gbc);
        gbc.gridx = 1; dialog.add(departmentCombo, gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton createBtn = new JButton(translate("CREATE"));
        JButton cancelBtn = new JButton(translate("CANCEL"));
        
        createBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String department = (String) departmentCombo.getSelectedItem();
            
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, translate("Please fill all fields"), 
                    translate("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            createUser(fullName, username, password, role, department, dialog);
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    
    private void createUser(String fullName, String username, String password, String role, String department, JDialog dialog) {
        statusLabel.setText(translate("Creating user..."));
        
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
                statusLabel.setText(translate("Ready"));
                if (success) {
                    JOptionPane.showMessageDialog(dialog, translate(message), translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(dialog, translate("Failed: ") + message, translate("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(frame, translate("Logout?"), 
            translate("Confirm"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentUser = null;
            currentUserId = 0;
            currentFullName = null;
            usernameField.setText("");
            passwordField.setText("");
            passwordVisible = false;
            passwordField.setEchoChar('*');
            toggleEyeBtn.setText("Show");
            cardLayout.show(mainPanel, "login");
            statusLabel.setText(translate("Ready"));
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
