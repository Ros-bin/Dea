package QuanLy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class QuanLyDoanVien extends JFrame {

    private DefaultTableModel tableModel;
    private JTable memberTable;
    private JTextField searchField;
    private static final Logger LOGGER = Logger.getLogger(QuanLyDoanVien.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String[] GENDER_OPTIONS = {"Nam", "Nữ", "Khác"};

    public QuanLyDoanVien() {
        setTitle("Quản Lý Đoàn Viên");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupLogger();
        
     // Set Icon 
        URL urlIconchinh = LoginFrame.class.getResource("Icon_Chinh.png");
        Image img = Toolkit.getDefaultToolkit().createImage(urlIconchinh);
        this.setIconImage(img);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Danh Sách Thành Viên", createMemberListPanel());
        add(tabbedPane);
    }

    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("member_app.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.INFO);
        } catch (Exception e) {
            System.err.println("Error setting up logger: " + e.getMessage());
        }
    }

    private JPanel createMemberListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Họ và Tên", "Email", "Ngày vào đoàn", "Xếp loại", "Khoa học", "Giới tính", "Số điện thoại"};
        tableModel = new DefaultTableModel(columnNames, 0);
        memberTable = new JTable(tableModel);

        memberTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        memberTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        memberTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        memberTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        memberTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        memberTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        memberTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(memberTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Thêm thành viên");
        JButton deleteButton = new JButton("Xóa");
        JButton editButton = new JButton("Sửa");
        JButton statisticalButton = new JButton("Thống kê");
        JButton searchButton = new JButton("Tìm kiếm");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        loadMembersToTable();

        addButton.addActionListener(e -> showAddMemberDialog());
        editButton.addActionListener(e -> showEditMemberDialog());
        deleteButton.addActionListener(e -> deleteMember());
        statisticalButton.addActionListener(e -> showStatistics());
        searchButton.addActionListener(e -> searchMember());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(statisticalButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showAddMemberDialog() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField statusField = new JTextField();
        JTextField courseField = new JTextField();
        JComboBox<String> genderField = new JComboBox<>(GENDER_OPTIONS);
        JTextField phoneField = new JTextField();
        
         // (10 số)
        limitTextFieldInput(phoneField, 10);

        JPanel inputPanel = createInputPanel(nameField, emailField, dateField, statusField, courseField, genderField, phoneField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Thêm Thành Viên", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String date = dateField.getText().trim();
            String status = statusField.getText().trim();
            String course = courseField.getText().trim();
             String gender = (String) genderField.getSelectedItem();
             String phone = phoneField.getText().trim();

            if (validateMemberInput(name, email, date, status, course, gender, phone)) {
                addMemberToDatabase(name, email, date, status, course, gender, phone);
            }
        }
    }

      private void limitTextFieldInput(JTextField textField, int maxLength) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if ((fb.getDocument().getLength() + string.length()) <= maxLength && string.matches("\\d+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if ((fb.getDocument().getLength() - length + text.length()) <= maxLength && text.matches("\\d+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }
    private void showEditMemberDialog() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow >= 0) {
            Member currentMember = getMemberFromTable(selectedRow);

            JTextField nameField = new JTextField(currentMember.getName());
            JTextField emailField = new JTextField(currentMember.getEmail());
            JTextField dateField = new JTextField(currentMember.getJoinDate().format(DATE_FORMATTER));
            JTextField statusField = new JTextField(currentMember.getRating());
            JTextField courseField = new JTextField(currentMember.getCourse());
             JComboBox<String> genderField = new JComboBox<>(GENDER_OPTIONS);
             genderField.setSelectedItem(currentMember.getGender());
             JTextField phoneField = new JTextField(currentMember.getPhoneNumber());
             
          
              limitTextFieldInput(phoneField, 10);

            JPanel inputPanel = createInputPanel(nameField, emailField, dateField, statusField, courseField, genderField, phoneField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Sửa Thành Viên", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String date = dateField.getText().trim();
                String status = statusField.getText().trim();
                String course = courseField.getText().trim();
                 String gender = (String) genderField.getSelectedItem();
                String phone = phoneField.getText().trim();
                if (validateMemberInput(name, email, date, status, course, gender, phone)) {
                    updateMemberInDatabase(name, email, date, status, course, gender, phone, currentMember.getEmail());
                }
            }
        } else {
            showMessageDialog("Vui lòng chọn thành viên để sửa!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel createInputPanel(JTextField nameField, JTextField emailField, JTextField dateField, JTextField statusField, JTextField courseField, JComboBox<String> genderField, JTextField phoneField) {
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        inputPanel.add(new JLabel("Họ và Tên:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Ngày vào đoàn (dd/MM/yyyy):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Xếp loại:"));
        inputPanel.add(statusField);
        inputPanel.add(new JLabel("Khoa học:"));
        inputPanel.add(courseField);
        inputPanel.add(new JLabel("Giới tính:"));
        inputPanel.add(genderField);
        inputPanel.add(new JLabel("Số điện thoại:"));
        inputPanel.add(phoneField);
        return inputPanel;
    }

    private Member getMemberFromTable(int selectedRow) {
        String name = (String) tableModel.getValueAt(selectedRow, 0);
        String email = (String) tableModel.getValueAt(selectedRow, 1);
        String dateString = (String) tableModel.getValueAt(selectedRow, 2);
        LocalDate joinDate = LocalDate.parse(dateString, DATE_FORMATTER);
        String rating = (String) tableModel.getValueAt(selectedRow, 3);
        String course = (String) tableModel.getValueAt(selectedRow, 4);
        String gender = (String) tableModel.getValueAt(selectedRow, 5);
         String phone = (String) tableModel.getValueAt(selectedRow, 6);
        return new Member(name, email, joinDate, rating, course, gender, phone);
    }

    private void deleteMember() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow >= 0) {
            String email = (String) tableModel.getValueAt(selectedRow, 1);
            if (showConfirmationDialog("Bạn có chắc chắn muốn xóa thành viên này?")) {
                deleteMemberFromDatabase(email);
            }
        } else {
            showMessageDialog("Vui lòng chọn thành viên để xóa!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void searchMember() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            searchMembersFromDatabase(searchText);
        } else {
            loadMembersToTable();
        }
    }

    private void loadMembersToTable() {
        tableModel.setRowCount(0); // clear the old data before loading
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM members")) {
            while (rs.next()) {
                Member member = new Member(rs.getString("name"), rs.getString("email"), rs.getDate("join_date").toLocalDate(), rs.getString("rating"), rs.getString("course"), rs.getString("gender"), rs.getString("phone_number"));
                addRowToTable(member);
            }
        } catch (SQLException ex) {
             showErrorDialog("Lỗi khi tải danh sách thành viên: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, "Error loading members", ex);
        }
    }
    private void addRowToTable(Member member){
        tableModel.addRow(new Object[]{
                member.getName(),
                member.getEmail(),
                member.getJoinDate().format(DATE_FORMATTER),
                member.getRating(),
                member.getCourse(),
                 member.getGender(),
                 member.getPhoneNumber()
        });
    }
    private void searchMembersFromDatabase(String searchText) {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM members WHERE LOWER(name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)")) {
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                   Member member = new Member(rs.getString("name"), rs.getString("email"), rs.getDate("join_date").toLocalDate(), rs.getString("rating"), rs.getString("course"), rs.getString("gender"), rs.getString("phone_number"));
                    addRowToTable(member);
                }
            }
        } catch (SQLException ex) {
             showErrorDialog("Lỗi khi tìm kiếm thành viên: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, "Error searching members", ex);
        }
    }


    private void addMemberToDatabase(String name, String email, String date, String status, String course, String gender, String phone) {
        try {
            LocalDate parsedDate = parseDate(date);
            java.sql.Date sqlDate = java.sql.Date.valueOf(parsedDate);

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO members (name, email, join_date, rating, course, gender, phone_number) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setDate(3, sqlDate);
                stmt.setString(4, status);
                stmt.setString(5, course);
                stmt.setString(6, gender);
                stmt.setString(7, phone);
                stmt.executeUpdate();

                 Member member = new Member(name, email, parsedDate, status, course, gender, phone);
                addRowToTable(member);

                showMessageDialog("Thêm thành viên thành công!", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                showErrorDialog("Lỗi khi thêm thành viên: " + ex.getMessage());
                 LOGGER.log(Level.SEVERE, "Error adding member", ex);
            }
        } catch (DateTimeParseException e) {
           showErrorDialog("Lỗi khi thêm thành viên: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error parsing date", e);
        }
    }

    private void updateMemberInDatabase(String name, String email, String date, String status, String course, String gender, String phone, String currentEmail) {
        try {
            LocalDate parsedDate = parseDate(date);
            java.sql.Date sqlDate = java.sql.Date.valueOf(parsedDate);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE members SET name = ?, email = ?, join_date = ?, rating = ?, course = ?, gender = ?, phone_number =? WHERE email = ?")) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setDate(3, sqlDate);
                stmt.setString(4, status);
                stmt.setString(5, course);
                 stmt.setString(6, gender);
                 stmt.setString(7, phone);
                stmt.setString(8, currentEmail);
                stmt.executeUpdate();

                int selectedRow = memberTable.getSelectedRow();
                  Member member = new Member(name, email, parsedDate, status, course, gender, phone);
                tableModel.removeRow(selectedRow);
                addRowToTable(member);

                showMessageDialog("Sửa thành viên thành công!", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                 showErrorDialog("Lỗi khi sửa thành viên: " + ex.getMessage());
                LOGGER.log(Level.SEVERE, "Error updating member", ex);
            }
        } catch (DateTimeParseException e) {
             showErrorDialog("Lỗi khi sửa thành viên: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error parsing date", e);
        }
    }

    private void deleteMemberFromDatabase(String email) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM members WHERE email = ?")) {
            stmt.setString(1, email);
            stmt.executeUpdate();
            int selectedRow = memberTable.getSelectedRow();
            tableModel.removeRow(selectedRow);
            showMessageDialog("Xóa thành viên thành công!", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
              showErrorDialog("Lỗi khi xóa thành viên: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, "Error deleting member", ex);
        }
    }

    private LocalDate parseDate(String dateString) throws DateTimeParseException {
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }


    private boolean validateMemberInput(String name, String email, String date, String rating, String course, String gender, String phone) {
        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty() || date == null || date.trim().isEmpty() || rating == null || rating.trim().isEmpty() || course == null || course.trim().isEmpty() || gender == null || gender.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
                showErrorDialog("Vui lòng nhập đầy đủ thông tin!");
                return false;
            }

        // Email validation
        if (!isValidEmail(email)) {
            showErrorDialog("Định dạng email không hợp lệ!");
            return false;
        }
        // Date validation
        if (!isValidDate(date)) {
            showErrorDialog("Định dạng ngày không hợp lệ! (dd/MM/yyyy)");
            return false;
        }
         if (!isValidPhoneNumber(phone)) {
             showErrorDialog("Số điện thoại không hợp lệ");
             return false;
         }

        return true;
    }
    private void showStatistics() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT " +
                             "   (SELECT COUNT(*) FROM members) as totalMembers, " +
                             "   (SELECT COUNT(*) FROM members WHERE rating = 'Tốt') as goodMembers, " +
                             "   (SELECT COUNT(*) FROM members WHERE rating = 'Khá') as averageMembers, " +
                             "   (SELECT COUNT(*) FROM members WHERE rating = 'Xuất sắc') as excellentMembers, " +
                             "   (SELECT COUNT(*) FROM members WHERE rating = 'Yếu') as weakMembers, " +
                             "    COUNT(DISTINCT course) as uniqueCourses FROM members"
             ))
        {

            if (rs.next()) {
                int totalMembers = rs.getInt("totalMembers");
                int goodMembers = rs.getInt("goodMembers");
                int averageMembers = rs.getInt("averageMembers");
                int excellentMembers = rs.getInt("excellentMembers");
                int weakMembers = rs.getInt("weakMembers");
                int uniqueCourses = rs.getInt("uniqueCourses");


                String message = "Tổng số thành viên: " + totalMembers + "\n" +
                        "Số thành viên xếp loại Tốt: " + goodMembers + "\n" +
                        "Số thành viên xếp loại Khá: " + averageMembers + "\n" +
                        "Số thành viên xếp loại Xuất sắc: " + excellentMembers + "\n" +
                        "Số thành viên xếp loại Yếu: " + weakMembers + "\n" +
                        "Số khoa: " + uniqueCourses;
                showMessageDialog(message, JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
             showErrorDialog("Lỗi khi thống kê: " + ex.getMessage());
             LOGGER.log(Level.SEVERE, "Error showing statistics", ex);
        }
    }

     private boolean isValidPhoneNumber(String phoneNumber){
         String phoneRegex = "^[0-9]{10}$";
        return phoneNumber.matches(phoneRegex);
     }


    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private boolean isValidDate(String date) {
        try {
            parseDate(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void showMessageDialog(String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", messageType);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private boolean showConfirmationDialog(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Xác nhận", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QuanLyDoanVien().setVisible(true);
        });
    }
}
class Member {
    private String name;
    private String email;
    private LocalDate joinDate;
    private String rating;
    private String course;
    private String gender;
    private String phoneNumber;

    public Member(String name, String email, LocalDate joinDate, String rating, String course, String gender, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.joinDate = joinDate;
        this.rating = rating;
        this.course = course;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }
    public String getRating() {
        return rating;
    }
    public String getCourse() {
        return course;
    }
    public String getGender() {
        return gender;
    }
     public String getPhoneNumber(){
        return phoneNumber;
     }

    @Override
    public String toString() {
        return "Member{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", joinDate=" + joinDate +
                ", rating='" + rating + '\'' +
                ", course='" + course + '\'' +
                 ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}

class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/quanlydoanvien";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database. " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if(connection == null || connection.isClosed()){
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    @Override
    protected void finalize() {
        closeConnection();
    }
}