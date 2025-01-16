package QuanLy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.sql.*;

/**
 * Lớp LoginFrame đại diện cho giao diện đăng nhập và đăng ký của ứng dụng.
 * Kế thừa từ JFrame để tạo cửa sổ giao diện người dùng.
 */
public class LoginFrame extends JFrame {

    /**
     * Constructor để khởi tạo giao diện đăng nhập/đăng ký.
     */
    public LoginFrame() {
        setTitle("Đăng Nhập/Đăng Ký"); 
        setSize(400, 300); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        setLocationRelativeTo(null); 
        
        // Thiết lập icon cho cửa sổ
        URL urlIconchinh = LoginFrame.class.getResource("Icon_Chinh.png"); 
        Image img = Toolkit.getDefaultToolkit().createImage(urlIconchinh); 
        this.setIconImage(img); 

        // Tạo bảng điều khiển (panel) chứa các thành phần giao diện
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10)); 
        JLabel userLabel = new JLabel("Tên đăng nhập:"); 
        JLabel passLabel = new JLabel("Mật khẩu:"); 

        
        Font labelFont = new Font(userLabel.getFont().getName(), Font.BOLD, 21);
        userLabel.setFont(labelFont);
        passLabel.setFont(labelFont);

       
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

      
        JButton loginButton = new JButton("Đăng Nhập");
        JButton registerButton = new JButton("Đăng Ký");

       
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.add(userLabel); 
        panel.add(usernameField); 
        panel.add(passLabel); 
        panel.add(passwordField); 
        panel.add(loginButton); 
        panel.add(registerButton); 

        add(panel); 

        
        loginButton.addActionListener(e -> handleLogin(usernameField.getText(), new String(passwordField.getPassword())));

       
        registerButton.addActionListener(e -> handleRegister(usernameField.getText(), new String(passwordField.getPassword())));
    }

   
    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username); 
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) { 
                if (rs.next()) { 
                    JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                    this.dispose(); 
                    SwingUtilities.invokeLater(() -> new QuanLyDoanVien().setVisible(true)); 
                } else {
                    JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void handleRegister(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            stmt.setString(1, username); 
            stmt.setString(2, password);
            stmt.executeUpdate(); 

            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("23000")) { 
                JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); // Tạo và hiển thị giao diện
    }
}
