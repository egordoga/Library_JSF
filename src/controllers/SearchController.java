package controllers;

import beans.Book;
import db.Database;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;


@ManagedBean(eager = true)
@SessionScoped
public class SearchController implements Serializable {

    private static final String TEMP_SQL =
            "select b.id,  b.name, b.page_count, b.isbn, b.publish_year, " +
                    "g.name as genre, a.fio as author, p.name as publisher from book b " +
                    "inner join genre g on b.genre_id=g.id " +
                    "inner join author a on b.author_id=a.id " +
                    "inner join publisher p on b.publisher_id=p.id ";

    private Character[] russianLetters = {'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О',
            'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я'};

    private SearchType searchType;
    private String currentSql;
    private long totalBooksCount;
    private int booksOnPage = 2;
    private long selectedPageNumber = 1;
    private ArrayList<Book> currentBookList;
    private int selectedGenreId;
    private String searchString;
    private char selectedLetter;
    private ArrayList<Integer> pageNumbers = new ArrayList<>();
    private boolean editMode;
    private String currentSqlWithoutLimit;

    public SearchController() {
        fillBooksAll();
    }

    public static void closeConnection(Connection conn, Statement st, ResultSet res) {
        try {
            if (res != null) {
                res.close();
            }
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void fillBooksBySQL(String sql) {
        StringBuilder sqlBuilder = new StringBuilder(sql);
        currentSql = sqlBuilder.toString();
        currentSqlWithoutLimit = sql;

        Connection conn = null;
        Statement st = null;
        ResultSet res = null;

        try {
            conn = Database.getConnection();
            assert conn != null;
            st = conn.createStatement();
            res = st.executeQuery(sql);

            res.last();
            totalBooksCount = res.getRow();

            if (totalBooksCount > booksOnPage) {
                fillPageNumbers(totalBooksCount, booksOnPage);
                sqlBuilder.append(" limit ").append(selectedPageNumber * booksOnPage - booksOnPage).append(",").append(booksOnPage);
            }

            res = st.executeQuery(sqlBuilder.toString());

            currentBookList = new ArrayList<>();

            while (res.next()) {
                Book book = new Book();
                book.setName(res.getString("name"));
                book.setAuthor(res.getString("author"));
                book.setId(res.getInt("id"));
                book.setGenre(res.getString("genre"));
                book.setIsbn(res.getString("isbn"));
                book.setPageCount(res.getInt("page_count"));
                book.setPublisher(res.getString("publisher"));
                book.setPublishYear(res.getInt("publish_year"));
                currentBookList.add(book);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, st, res);
        }
    }

    private void fillPageNumbers(long totalBooksCount, int booksOnPage) {

        int pageCount = totalBooksCount / booksOnPage != 0 ? (int) (totalBooksCount / booksOnPage + 1) : (int) (totalBooksCount / booksOnPage);
        pageNumbers.clear();
        for (int i = 1; i <= pageCount; i++) {
            pageNumbers.add(i);
        }
    }

    public String fillBooksByGenre(int i) {

        selectedGenreId = i;
        selectedPageNumber = 1;

        fillBooksBySQL(TEMP_SQL + " where b.genre_id=" + selectedGenreId + " order by b.name");

        selectedLetter = ' ';
        searchString = "";

        return "books";
    }

    public void fillBooksByLetter() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedLetter = params.get("letter").charAt(0);

        fillBooksBySQL(TEMP_SQL + "where substr(b.name,1,1)='" + selectedLetter + "' order by b.name");
        selectedGenreId = -1;
        selectedPageNumber = 1;
    }

    public void fillBookBySearch() {
        if (searchString.trim().length() == 0) {
            fillBooksAll();
            return;
        }
        String end = "%' order by b.name";

        if (searchType == SearchType.AUTHOR) {
            fillBooksBySQL(TEMP_SQL + "where lower(a.fio) like '%" + searchString.toLowerCase() + end);
        } else if (searchType == SearchType.NAME) {
            fillBooksBySQL(TEMP_SQL + "where lower(b.name) like '%" + searchString.toLowerCase() + end);
        }

        selectedLetter = ' ';
        selectedGenreId = -1;
        selectedPageNumber = 1;
    }

    private void fillBooksAll() {
        fillBooksBySQL(TEMP_SQL + "order by b.name");
    }

    public String selectedPage() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedPageNumber = Integer.valueOf(params.get("page_number"));
        fillBooksBySQL(currentSql);
        return "library_template.xhtml";
    }

    public byte[] getContent(int id) {
        Statement st = null;
        ResultSet res = null;
        Connection conn = null;

        byte[] content = null;

        try {
            conn = Database.getConnection();
            st = conn.createStatement();
            res = st.executeQuery("SELECT content FROM book WHERE id=" + id);
            while (res.next()) {
                content = res.getBytes("content");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, st, res);
        }
        return content;
    }

    public String updateBooks() {

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("UPDATE book SET name=?, isbn=?, page_count=?, publish_year=? WHERE id=?");

            for (Book book : currentBookList) {
                prepStmt.setString(1, book.getName());
                prepStmt.setString(2, book.getIsbn());
                prepStmt.setInt(3, book.getPageCount());
                prepStmt.setInt(4, book.getPublishYear());
                prepStmt.setInt(5, book.getId());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }

                assert conn != null;
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        switchEditMode();
        return "books";
    }

    public void changeBooksOnPage(ValueChangeEvent event) {
        booksOnPage = Integer.valueOf(event.getNewValue().toString());
        selectedPageNumber = 1;
        fillBooksBySQL(currentSqlWithoutLimit);
    }

    public void switchEditMode() {
        editMode = !editMode;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public ArrayList<Integer> getPageNumbers() {
        return pageNumbers;
    }

    public int getSelectedGenreId() {
        return selectedGenreId;
    }

    public long getSelectedPageNumber() {
        return selectedPageNumber;
    }

    public int getBooksOnPage() {
        return booksOnPage;
    }

    public void setBooksOnPage(int booksOnPage) {
        this.booksOnPage = booksOnPage;
    }

    public char getSelectedLetter() {
        return selectedLetter;
    }

    public Character[] getRussianLetters() {
        return russianLetters;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public void setSelectedGenreId(int selectedGenreId) {
        this.selectedGenreId = selectedGenreId;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public byte[] getImage(int id) {

        Connection conn = null;
        Statement st = null;
        ResultSet res = null;

        byte[] image = null;

        try {
            conn = Database.getConnection();
            st = conn.createStatement();
            res = st.executeQuery("SELECT image FROM book WHERE id=" + id);

            while (res.next()) {
                image = res.getBytes("image");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, st, res);
        }
        return image;
    }

    public ArrayList<Book> getCurrentBookList() {
        return currentBookList;
    }

    public long getTotalBooksCount() {
        return totalBooksCount;
    }
}
