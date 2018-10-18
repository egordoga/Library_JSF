package beans;

import db.Database;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean
@SessionScoped
public class User implements Serializable {

    private String username;
    private String password;

    public User() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String login() throws SQLException {
        ResultSet rs = null;
        Connection conn = null;
        Statement st = null;

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            conn = Database.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("select * from users");
            while (rs.next()) {
                if (rs.getString("userid").equals(username) &&
                        rs.getString("password").equals(password)) {
                    rs.close();
                    st.close();
                    conn.close();
                    return "books";
                }
            }

            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Логин или пароль не верны");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            facesContext.addMessage("login_form", message);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return "index1";
    }

    public String logout() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        try {
            request.logout();
        } catch (ServletException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e);
        }

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        return "/index1.xhtml?faces-redirect=true";
    }
}
