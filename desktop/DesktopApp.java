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
    
    private JComboBox<String> languageCombo;
    private JLabel languageLabel;
    private JToggleButton toggleEyeBtn;
    private JLabel logoLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton loginButton;
    
    private JLabel superAdminTitleLabel;
    private JLabel adminTitleLabel;
    private JLabel userTitleLabel;
    private JTabbedPane superAdminTabbedPane;
    private JTabbedPane adminTabbedPane;
    private JButton signInBtn, signOutBtn, historyBtn;
    private JButton createUserBtn, createAdminBtn, refreshBtn, deleteBtn;
    
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

    public static void main(String[] args) {
        TranslationHelper.loadLanguage();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }
        EventQueue.invokeLater(() -> {
            try {
                new DesktopApp().frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public DesktopApp() {
        initialize();
        translateAllUI();
    }
    
    private void translateAllUI() {
        SwingUtilities.invokeLater(() -> {
            JMenuBar menuBar = frame.getJMenuBar();
            if (menuBar != null) {
                JMenu fileMenu = menuBar.getMenu(0);
                JMenu languageMenu = menuBar.getMenu(1);
                JMenu helpMenu = menuBar.getMenu(2);
                if (fileMenu != null) fileMenu.setText(TranslationHelper.translateText("File"));
                if (languageMenu != null) languageMenu.setText(TranslationHelper.translateText("Language"));
                if (helpMenu != null) helpMenu.setText(TranslationHelper.translateText("Help"));
            }
            if (titleLabel != null) titleLabel.setText(TranslationHelper.translateText("AL FAROOJ AL SHAMI"));
            if (subtitleLabel != null) subtitleLabel.setText(TranslationHelper.translateText("TIME TABLE SYSTEM"));
            if (languageLabel != null) languageLabel.setText(TranslationHelper.translateText("Select Language:"));
            if (loginButton != null) {
                loginButton.setText(TranslationHelper.translateText("LOGIN"));
                loginButton.setBackground(new Color(0, 120, 0));
                loginButton.setForeground(Color.WHITE);
                loginButton.setFont(new Font("Arial", Font.BOLD, 16));
            }
            if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready"));
            if (usernameField != null && usernameField.getText().equals("Enter username"))
                usernameField.setText(TranslationHelper.translateText("Enter username"));
            if (passwordField != null) {
                String pwdText = new String(passwordField.getPassword());
                if (pwdText.equals("Enter password"))
                    passwordField.setText(TranslationHelper.translateText("Enter password"));
            }
            if (currentRole != null) {
                if (currentRole.equals("super_admin")) {
                    if (superAdminTitleLabel != null) superAdminTitleLabel.setText(TranslationHelper.translateText("SUPER ADMIN DASHBOARD"));
                    if (superAdminTabbedPane != null) {
                        superAdminTabbedPane.setTitleAt(0, TranslationHelper.translateText("MANAGE USERS"));
                        superAdminTabbedPane.setTitleAt(1, TranslationHelper.translateText("TODAY ATTENDANCE"));
                        superAdminTabbedPane.setTitleAt(2, TranslationHelper.translateText("ALL HISTORY"));
                        superAdminTabbedPane.setTitleAt(3, TranslationHelper.translateText("KITCHEN HISTORY"));
                        superAdminTabbedPane.setTitleAt(4, TranslationHelper.translateText("WAITER HISTORY"));
                        superAdminTabbedPane.setTitleAt(5, TranslationHelper.translateText("DELIVERY HISTORY"));
                        superAdminTabbedPane.setTitleAt(6, TranslationHelper.translateText("MANAGER HISTORY"));
                    }
                } else if (currentRole.equals("admin")) {
                    if (adminTitleLabel != null) adminTitleLabel.setText(TranslationHelper.translateText("ADMIN DASHBOARD"));
                    if (adminTabbedPane != null) {
                        adminTabbedPane.setTitleAt(0, TranslationHelper.translateText("MANAGE USERS"));
                        adminTabbedPane.setTitleAt(1, TranslationHelper.translateText("TODAY ATTENDANCE"));
                        adminTabbedPane.setTitleAt(2, TranslationHelper.translateText("ALL HISTORY"));
                        adminTabbedPane.setTitleAt(3, TranslationHelper.translateText("KITCHEN HISTORY"));
                        adminTabbedPane.setTitleAt(4, TranslationHelper.translateText("WAITER HISTORY"));
                        adminTabbedPane.setTitleAt(5, TranslationHelper.translateText("DELIVERY HISTORY"));
                        adminTabbedPane.setTitleAt(6, TranslationHelper.translateText("MANAGER HISTORY"));
                    }
                } else if (currentRole.equals("user")) {
                    if (userTitleLabel != null) userTitleLabel.setText(TranslationHelper.translateText("USER DASHBOARD"));
                    if (signInBtn != null) signInBtn.setText(TranslationHelper.translateText("SIGN IN"));
                    if (signOutBtn != null) signOutBtn.setText(TranslationHelper.translateText("SIGN OUT"));
                    if (historyBtn != null) historyBtn.setText(TranslationHelper.translateText("MY HISTORY"));
                }
            }
            if (createUserBtn != null) createUserBtn.setText(TranslationHelper.translateText("CREATE USER"));
            if (createAdminBtn != null) createAdminBtn.setText(TranslationHelper.translateText("CREATE ADMIN"));
            if (refreshBtn != null) refreshBtn.setText(TranslationHelper.translateText("REFRESH"));
            if (deleteBtn != null) deleteBtn.setText(TranslationHelper.translateText("DELETE SELECTED"));
            refreshTableHeaders();
        });
    }
    
    private void refreshTableHeaders() {
        SwingUtilities.invokeLater(() -> {
            if (userTableModel != null) {
                userTableModel.setColumnIdentifiers(new Object[]{
                    TranslationHelper.translateText("ID"),
                    TranslationHelper.translateText("Full Name"),
                    TranslationHelper.translateText("Username"),
                    TranslationHelper.translateText("Role"),
                    TranslationHelper.translateText("Department"),
                    TranslationHelper.translateText("Created At")
                });
            }
            if (historyTableModel != null) {
                historyTableModel.setColumnIdentifiers(new Object[]{
                    TranslationHelper.translateText("ID"),
                    TranslationHelper.translateText("Username"),
                    TranslationHelper.translateText("Full Name"),
                    TranslationHelper.translateText("Department"),
                    TranslationHelper.translateText("Event"),
                    TranslationHelper.translateText("Location"),
                    TranslationHelper.translateText("Timestamp")
                });
            }
        });
    }
    
    private void initialize() {
        frame = new JFrame("AL FAROOJ AL SHAMI - Time Table System");
        frame.setBounds(100, 100, 1400, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        try {
            File iconFile = new File("desktop/icons/hgd.ico");
            if (iconFile.exists()) frame.setIconImage(new ImageIcon(iconFile.getAbsolutePath()).getImage());
        } catch (Exception e) {}
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        JMenu languageMenu = new JMenu("Language");
        for (int i = 0; i < LANGUAGES.length; i++) {
            final String code = LANGUAGE_CODES[i];
            final String name = LANGUAGES[i];
            JMenuItem langItem = new JMenuItem(name);
            langItem.addActionListener(e -> {
                TranslationHelper.setLanguage(code);
                translateAllUI();
                JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("Language changed to ") + name, TranslationHelper.translateText("Language"), JOptionPane.INFORMATION_MESSAGE);
            });
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
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        try {
            File logoFile = new File("desktop/icons/log.png");
            if (!logoFile.exists()) logoFile = new File("desktop/icons/hgd.ico");
            if (logoFile.exists()) {
                ImageIcon logoIcon = new ImageIcon(logoFile.getAbsolutePath());
                Image img = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(img));
            } else {
                logoLabel = new JLabel("🏪");
                logoLabel.setFont(new Font("Arial", Font.PLAIN, 50));
                logoLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            logoLabel = new JLabel("🏪");
            logoLabel.setFont(new Font("Arial", Font.PLAIN, 50));
            logoLabel.setForeground(Color.WHITE);
        }
        panel.add(logoLabel, gbc);
        
        gbc.gridy = 1;
        titleLabel = new JLabel("AL FAROOJ AL SHAMI");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, gbc);
        
        gbc.gridy = 2;
        subtitleLabel = new JLabel("TIME TABLE SYSTEM");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.WHITE);
        panel.add(subtitleLabel, gbc);
        
        gbc.gridy = 3;
        panel.add(Box.createVerticalStrut(20), gbc);
        
        gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setForeground(Color.GRAY);
        usernameField.setText("Enter username");
        usernameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (usernameField.getText().equals("Enter username")) { 
                    usernameField.setText(""); 
                    usernameField.setForeground(Color.BLACK); 
                }
            }
            public void focusLost(FocusEvent e) {
                if (usernameField.getText().isEmpty()) { 
                    usernameField.setForeground(Color.GRAY); 
                    usernameField.setText("Enter username"); 
                }
            }
        });
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(new Color(33, 150, 243));
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setForeground(Color.GRAY);
        passwordField.setText("Enter password");
        passwordField.setEchoChar((char)0);
        passwordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                String text = new String(passwordField.getPassword());
                if (text.equals("Enter password")) { 
                    passwordField.setText(""); 
                    passwordField.setForeground(Color.BLACK); 
                    passwordField.setEchoChar('*'); 
                }
            }
            public void focusLost(FocusEvent e) {
                if (passwordField.getPassword().length == 0) { 
                    passwordField.setForeground(Color.GRAY); 
                    passwordField.setText("Enter password"); 
                    passwordField.setEchoChar((char)0); 
                }
            }
        });
        toggleEyeBtn = new JToggleButton("👁");
        toggleEyeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toggleEyeBtn.setPreferredSize(new Dimension(40, 25));
        toggleEyeBtn.setBackground(new Color(240, 240, 240));
        toggleEyeBtn.addActionListener(e -> {
            if (toggleEyeBtn.isSelected()) { 
                passwordField.setEchoChar((char)0); 
                toggleEyeBtn.setText("👁");
            } else { 
                passwordField.setEchoChar('*'); 
                toggleEyeBtn.setText("🔒");
            }
        });
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toggleEyeBtn, BorderLayout.EAST);
        panel.add(passwordPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel langPanel = new JPanel(new FlowLayout());
        langPanel.setBackground(new Color(33, 150, 243));
        languageLabel = new JLabel("Select Language:");
        languageLabel.setForeground(Color.WHITE);
        languageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        langPanel.add(languageLabel);
        languageCombo = new JComboBox<>(LANGUAGES);
        languageCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        languageCombo.setPreferredSize(new Dimension(150, 25));
        String currentLang = TranslationHelper.getCurrentLanguage();
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            if (LANGUAGE_CODES[i].equals(currentLang)) { 
                languageCombo.setSelectedIndex(i); 
                break; 
            }
        }
        languageCombo.addActionListener(e -> {
            int idx = languageCombo.getSelectedIndex();
            if (idx >= 0) { 
                TranslationHelper.setLanguage(LANGUAGE_CODES[idx]); 
                translateAllUI(); 
            }
        });
        langPanel.add(languageCombo);
        panel.add(langPanel, gbc);
        
        gbc.gridy = 7;
        loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(0, 120, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(200, 45));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        loginButton.addActionListener(e -> login());
        panel.add(loginButton, gbc);
        
        usernameField.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
        return panel;
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.equals("Enter username") || username.isEmpty()) {
            JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("Please enter username"), TranslationHelper.translateText("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.equals("Enter password") || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("Please enter password"), TranslationHelper.translateText("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Logging in..."));
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = false;
            private String role = "", fullName = "", department = "", errorMsg = "";
            private int userId = 0;
            
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
                SwingUtilities.invokeLater(() -> {
                    if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready"));
                    if (success) {
                        JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("Welcome ") + fullName + "!", TranslationHelper.translateText("Success"), JOptionPane.INFORMATION_MESSAGE);
                        if (role.equals("super_admin")) {
                            cardLayout.show(mainPanel, "super_admin");
                            loadUsers();
                            loadAllHistoryTables();
                        } else if (role.equals("admin")) {
                            cardLayout.show(mainPanel, "admin");
                            loadUsers();
                            loadAllHistoryTables();
                        } else {
                            cardLayout.show(mainPanel, "user");
                            initUserPanel();
                        }
                        translateAllUI();
                    } else {
                        JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("Login failed: ") + errorMsg, TranslationHelper.translateText("Error"), JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        };
        worker.execute();
    }
    
    private JPanel createSuperAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(21, 101, 192));
        topPanel.setPreferredSize(new Dimension(0, 60));
        superAdminTitleLabel = new JLabel("SUPER ADMIN DASHBOARD", SwingConstants.CENTER);
        superAdminTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        superAdminTitleLabel.setForeground(Color.WHITE);
        topPanel.add(superAdminTitleLabel, BorderLayout.CENTER);
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setBackground(new Color(21, 101, 192));
        JLabel userLabel = new JLabel("Logged in as: " + (currentFullName != null ? currentFullName : ""));
        userLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userLabel);
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.addActionListener(e -> logout());
        userInfoPanel.add(logoutBtn);
        topPanel.add(userInfoPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        superAdminTabbedPane = new JTabbedPane();
        superAdminTabbedPane.addTab("MANAGE USERS", createUsersPanel(true));
        superAdminTabbedPane.addTab("TODAY ATTENDANCE", createHistoryPanel(null, true));
        superAdminTabbedPane.addTab("ALL HISTORY", createHistoryPanel(null, false));
        superAdminTabbedPane.addTab("KITCHEN HISTORY", createHistoryPanel("kitchen", false));
        superAdminTabbedPane.addTab("WAITER HISTORY", createHistoryPanel("waiter", false));
        superAdminTabbedPane.addTab("DELIVERY HISTORY", createHistoryPanel("delivery", false));
        superAdminTabbedPane.addTab("MANAGER HISTORY", createHistoryPanel("manager", false));
        panel.add(superAdminTabbedPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(46, 125, 50));
        topPanel.setPreferredSize(new Dimension(0, 60));
        adminTitleLabel = new JLabel("ADMIN DASHBOARD", SwingConstants.CENTER);
        adminTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        adminTitleLabel.setForeground(Color.WHITE);
        topPanel.add(adminTitleLabel, BorderLayout.CENTER);
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setBackground(new Color(46, 125, 50));
        JLabel userLabel = new JLabel("Logged in as: " + (currentFullName != null ? currentFullName : ""));
        userLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userLabel);
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.addActionListener(e -> logout());
        userInfoPanel.add(logoutBtn);
        topPanel.add(userInfoPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        adminTabbedPane = new JTabbedPane();
        adminTabbedPane.addTab("MANAGE USERS", createUsersPanel(false));
        adminTabbedPane.addTab("TODAY ATTENDANCE", createHistoryPanel(null, true));
        adminTabbedPane.addTab("ALL HISTORY", createHistoryPanel(null, false));
        adminTabbedPane.addTab("KITCHEN HISTORY", createHistoryPanel("kitchen", false));
        adminTabbedPane.addTab("WAITER HISTORY", createHistoryPanel("waiter", false));
        adminTabbedPane.addTab("DELIVERY HISTORY", createHistoryPanel("delivery", false));
        adminTabbedPane.addTab("MANAGER HISTORY", createHistoryPanel("manager", false));
        panel.add(adminTabbedPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createHistoryPanel(String department, boolean isTodayOnly) {
        JPanel panel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.addActionListener(e -> loadHistoryTable(department, isTodayOnly));
        toolBar.add(refreshBtn);
        panel.add(toolBar, BorderLayout.NORTH);
        
        String[] columns = {
            TranslationHelper.translateText("ID"), 
            TranslationHelper.translateText("Username"), 
            TranslationHelper.translateText("Full Name"), 
            TranslationHelper.translateText("Department"), 
            TranslationHelper.translateText("Event"), 
            TranslationHelper.translateText("Location"), 
            TranslationHelper.translateText("Timestamp")
        };
        historyTableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(historyTableModel);
        historyTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        loadHistoryTable(department, isTodayOnly);
        return panel;
    }
    
    private void loadHistoryTable(String department, boolean isTodayOnly) {
        if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Loading history..."));
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Object[][] historyData;
            private String deptName = department != null ? department : (isTodayOnly ? "today" : "all");
            
            @Override 
            protected Void doInBackground() {
                try {
                    String url = API_URL + "attendance_logs";
                    boolean hasParam = false;
                    if (department != null && !department.isEmpty()) {
                        url += "?department=" + department;
                        hasParam = true;
                    }
                    if (isTodayOnly) {
                        url += (hasParam ? "&" : "?") + "today=true";
                    }
                    System.out.println("Fetching: " + url);
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
                        System.out.println("Loaded " + historyData.length + " records for " + deptName);
                    }
                } catch (Exception e) { 
                    e.printStackTrace(); 
                }
                return null;
            }
            
            @Override 
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    if (historyTableModel != null) {
                        historyTableModel.setRowCount(0);
                        if (historyData != null) {
                            for (Object[] row : historyData) {
                                historyTableModel.addRow(row);
                            }
                            if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready") + " - " + historyData.length + " " + TranslationHelper.translateText("records loaded for ") + deptName);
                        } else {
                            if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready") + " - " + TranslationHelper.translateText("No records found for ") + deptName);
                        }
                        refreshTableHeaders();
                    }
                });
            }
        };
        worker.execute();
    }
    
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 81, 0));
        topPanel.setPreferredSize(new Dimension(0, 60));
        userTitleLabel = new JLabel("USER DASHBOARD", SwingConstants.CENTER);
        userTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        userTitleLabel.setForeground(Color.WHITE);
        topPanel.add(userTitleLabel, BorderLayout.CENTER);
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setBackground(new Color(230, 81, 0));
        JLabel userLabel = new JLabel("User: " + (currentFullName != null ? currentFullName : ""));
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
        
        signInBtn = new JButton("SIGN IN");
        signInBtn.setBackground(new Color(76, 175, 80));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFont(new Font("Arial", Font.BOLD, 16));
        signInBtn.setPreferredSize(new Dimension(180, 70));
        signInBtn.addActionListener(e -> recordAttendance("sign_in", "Sign In"));
        buttonsPanel.add(signInBtn);
        
        signOutBtn = new JButton("SIGN OUT");
        signOutBtn.setBackground(new Color(244, 67, 54));
        signOutBtn.setForeground(Color.WHITE);
        signOutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        signOutBtn.setPreferredSize(new Dimension(180, 70));
        signOutBtn.addActionListener(e -> recordAttendance("sign_out", "Sign Out"));
        buttonsPanel.add(signOutBtn);
        
        historyBtn = new JButton("MY HISTORY");
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
        scrollPane.setBorder(new TitledBorder(TranslationHelper.translateText("Activity Log")));
        scrollPane.setPreferredSize(new Dimension(0, 150));
        panel.add(scrollPane, BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel createUsersPanel(boolean canCreateAdmin) {
        JPanel panel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        createUserBtn = new JButton("CREATE USER");
        createUserBtn.addActionListener(e -> showCreateUserDialog("user"));
        toolBar.add(createUserBtn);
        if (canCreateAdmin) {
            createAdminBtn = new JButton("CREATE ADMIN");
            createAdminBtn.addActionListener(e -> showCreateUserDialog("admin"));
            toolBar.add(createAdminBtn);
        }
        toolBar.addSeparator();
        refreshBtn = new JButton("REFRESH");
        refreshBtn.addActionListener(e -> { loadUsers(); loadAllHistoryTables(); });
        toolBar.add(refreshBtn);
        deleteBtn = new JButton("DELETE SELECTED");
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
    
    private void loadAllHistoryTables() {
        // Refresh all history tabs
        if (superAdminTabbedPane != null) {
            for (int i = 2; i < superAdminTabbedPane.getTabCount(); i++) {
                Component comp = superAdminTabbedPane.getComponentAt(i);
                if (comp instanceof JPanel) {
                    // Will be refreshed when tab is selected
                }
            }
        }
        if (adminTabbedPane != null) {
            for (int i = 2; i < adminTabbedPane.getTabCount(); i++) {
                Component comp = adminTabbedPane.getComponentAt(i);
                if (comp instanceof JPanel) {
                    // Will be refreshed when tab is selected
                }
            }
        }
    }
    
    private void loadUsers() {
        if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Loading users..."));
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Object[][] userData;
            
            @Override 
            protected Void doInBackground() {
                try {
                    String response = sendGetRequest(API_URL + "users");
                    System.out.println("Users response: " + response);
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
                        System.out.println("Loaded " + userData.length + " users");
                    }
                } catch (Exception e) { 
                    e.printStackTrace(); 
                }
                return null;
            }
            
            @Override 
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    if (userTableModel != null) {
                        userTableModel.setRowCount(0);
                        if (userData != null) {
                            for (Object[] row : userData) {
                                userTableModel.addRow(row);
                            }
                            if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready") + " - " + userData.length + " " + TranslationHelper.translateText("users loaded"));
                        } else {
                            if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready") + " - " + TranslationHelper.translateText("No users found"));
                        }
                        refreshTableHeaders();
                    }
                });
            }
        };
        worker.execute();
    }
    
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("Please select a user to delete"), TranslationHelper.translateText("Warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 2);
        int confirm = JOptionPane.showConfirmDialog(frame, TranslationHelper.translateText("Delete user: ") + username + "?", TranslationHelper.translateText("Confirm"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Deleting user..."));
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override 
                protected Void doInBackground() {
                    try { 
                        sendDeleteRequest(API_URL + "delete_user/" + userId); 
                    } catch (Exception e) { e.printStackTrace(); }
                    return null;
                }
                @Override 
                protected void done() {
                    SwingUtilities.invokeLater(() -> {
                        if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready"));
                        JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("User deleted successfully"), TranslationHelper.translateText("Success"), JOptionPane.INFORMATION_MESSAGE);
                        loadUsers();
                    });
                }
            };
            worker.execute();
        }
    }
    
    private void showCreateUserDialog(String role) {
        JDialog dialog = new JDialog(frame, TranslationHelper.translateText(role.equals("admin") ? "Create Admin" : "Create User"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        JTextField fullNameField = new JTextField(20);
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> departmentCombo = new JComboBox<>(new String[]{"kitchen", "waiter", "delivery", "manager"});
        
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel(TranslationHelper.translateText("Full Name:")), gbc);
        gbc.gridx = 1; dialog.add(fullNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel(TranslationHelper.translateText("Username:")), gbc);
        gbc.gridx = 1; dialog.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel(TranslationHelper.translateText("Password:")), gbc);
        gbc.gridx = 1; dialog.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel(TranslationHelper.translateText("Department:")), gbc);
        gbc.gridx = 1; dialog.add(departmentCombo, gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton createBtn = new JButton(TranslationHelper.translateText("CREATE"));
        JButton cancelBtn = new JButton(TranslationHelper.translateText("CANCEL"));
        createBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String department = (String) departmentCombo.getSelectedItem();
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, TranslationHelper.translateText("Please fill all fields"), TranslationHelper.translateText("Error"), JOptionPane.ERROR_MESSAGE);
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
        if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Creating user..."));
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
                } catch (Exception e) { message = e.getMessage(); }
                return null;
            }
            @Override 
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready"));
                    if (success) {
                        JOptionPane.showMessageDialog(dialog, message, TranslationHelper.translateText("Success"), JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadUsers();
                    } else {
                        JOptionPane.showMessageDialog(dialog, TranslationHelper.translateText("Failed: ") + message, TranslationHelper.translateText("Error"), JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        };
        worker.execute();
    }
    
    private void initUserPanel() {
        if (logArea != null) {
            logArea.setText("");
            String time = LocalDateTime.now(UAE_TIMEZONE).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + time + "] " + TranslationHelper.translateText("Welcome ") + currentFullName + "\n");
        }
        translateAllUI();
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
                } catch (Exception e) { message = e.getMessage(); }
                return null;
            }
            @Override 
            protected void done() {
                String time = LocalDateTime.now(UAE_TIMEZONE).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String translatedEvent = TranslationHelper.translateText(eventName);
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        if (logArea != null) logArea.append("[" + time + "] " + translatedEvent + " " + TranslationHelper.translateText("recorded") + "\n");
                        JOptionPane.showMessageDialog(frame, message, TranslationHelper.translateText("Success"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        if (logArea != null) logArea.append("[" + time + "] " + TranslationHelper.translateText("Failed") + ": " + message + "\n");
                        JOptionPane.showMessageDialog(frame, TranslationHelper.translateText("Failed: ") + message, TranslationHelper.translateText("Error"), JOptionPane.ERROR_MESSAGE);
                    }
                    if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready"));
                });
            }
        };
        worker.execute();
    }
    
    private void showUserHistory() {
        JDialog historyDialog = new JDialog(frame, TranslationHelper.translateText("My Attendance History"), true);
        historyDialog.setSize(800, 500);
        historyDialog.setLocationRelativeTo(frame);
        String[] columns = { 
            TranslationHelper.translateText("ID"), 
            TranslationHelper.translateText("Event"), 
            TranslationHelper.translateText("Department"), 
            TranslationHelper.translateText("Location"), 
            TranslationHelper.translateText("Timestamp") 
        };
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
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }
            @Override 
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    historyDialog.add(new JScrollPane(table));
                    historyDialog.setVisible(true);
                });
            }
        };
        worker.execute();
    }
    
    private void showAboutDialog() {
        String aboutText = TranslationHelper.translateText("AL FAROOJ AL SHAMI Time Table System\nVersion 2.0\n\nFeatures:\n- Super Admin Dashboard\n- Admin Dashboard\n- User Dashboard\n- Location Validation\n- Attendance Tracking\n- User Management\n- Multi-department Support\n- Multi-language Support (20 Languages)");
        JOptionPane.showMessageDialog(frame, aboutText, TranslationHelper.translateText("About"), JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(frame, TranslationHelper.translateText("Logout?"), TranslationHelper.translateText("Confirm"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentUser = null; currentRole = null; currentUserId = 0; currentDepartment = null; currentFullName = null;
            usernameField.setText(""); passwordField.setText("");
            cardLayout.show(mainPanel, "login");
            if (statusLabel != null) statusLabel.setText(TranslationHelper.translateText("Ready"));
        }
    }
    
    private String sendPostRequest(String urlString, String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
    
    private String sendGetRequest(String urlString) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
    
    private String sendDeleteRequest(String urlString) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).DELETE().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
