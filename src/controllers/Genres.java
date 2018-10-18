package controllers;

import beans.Genre;
import db.Database;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@ManagedBean
@ApplicationScoped
public class Genres implements Serializable {

    public ArrayList<Genre> getGenreList() {

        Connection conn = null;
        Statement st = null;
        ResultSet res = null;

        ArrayList<Genre> genreList = new ArrayList<>();


        try {

            conn = Database.getConnection();
            assert conn != null;
            st = conn.createStatement();
            res = st.executeQuery("SELECT * FROM library.genre");

            while (res.next()) {
                Genre genre = new Genre();
                genre.setId(res.getInt("id"));
                genre.setName(res.getString("name"));
                genreList.add(genre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SearchController.closeConnection(conn, st, res);
        }

        return genreList;
    }

}
