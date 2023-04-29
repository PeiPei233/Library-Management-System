import entities.Book;
import entities.Borrow;
import entities.Card;
import entities.Book.SortColumn;
import entities.Card.CardType;
import queries.*;
import utils.DBInitializer;
import utils.DatabaseConnector;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO book (category, title, press, publish_year, author, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        // set the book id back
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT book_id FROM book WHERE category = ? AND title = ? AND press = ? AND publish_year = ? AND author = ? AND price = ? AND stock = ?");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            ResultSet rs = stmt.executeQuery();
            rs.next();
            book.setBookId(rs.getInt("book_id"));
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Store book successfully.");
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        // Make sure the book exists
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM book WHERE book_id = ?");
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Book not found.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Make sure the new stock is non-negative
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT stock FROM book WHERE book_id = ?");
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int stock = rs.getInt("stock");
            if (stock + deltaStock < 0) {
                return new ApiResult(false, "Stock cannot be negative.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Update the stock
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE book SET stock = stock + ? WHERE book_id = ?");
            stmt.setInt(1, deltaStock);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }

        return new ApiResult(true, "Update stock successfully.");
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO book VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            for (Book book : books) {
                stmt.setInt(1, book.getBookId());
                stmt.setString(2, book.getCategory());
                stmt.setString(3, book.getTitle());
                stmt.setString(4, book.getPress());
                stmt.setInt(5, book.getPublishYear());
                stmt.setString(6, book.getAuthor());
                stmt.setDouble(7, book.getPrice());
                stmt.setInt(8, book.getStock());
                stmt.addBatch();
            }
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }

        // set the book id back
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT book_id FROM book WHERE category = ? AND title = ? AND press = ? AND publish_year = ? AND author = ? AND price = ? AND stock = ?");
            for (Book book : books) {
                stmt.setString(1, book.getCategory());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getPress());
                stmt.setInt(4, book.getPublishYear());
                stmt.setString(5, book.getAuthor());
                stmt.setDouble(6, book.getPrice());
                stmt.setInt(7, book.getStock());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                book.setBookId(rs.getInt("book_id"));
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        return new ApiResult(true, "Store books successfully.");
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = connector.getConn();
        // Check if the book exists
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM book WHERE book_id = ?");
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Book not found.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Check if the book is borrowed but not returned
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM borrow WHERE book_id = ? AND return_time = 0");
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ApiResult(false, "Book is borrowed but not returned.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Remove the book
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM book WHERE book_id = ?");
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }

        return new ApiResult(true, "Remove book successfully.");
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        // Check if the book exists
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT stock FROM book WHERE book_id = ?");
            stmt.setInt(1, book.getBookId());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Book not found.");
            }
            book.setStock(rs.getInt("stock"));
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Update the book info
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE book SET category = ?, title = ?, press = ?, publish_year = ?, author = ?, price = ? WHERE book_id = ?");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getBookId());
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Modify book info successfully.");
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        Connection conn = connector.getConn();
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM book WHERE 1 = 1";
        List<Object> conditionsList = new ArrayList<>();

        if (conditions.getCategory() != null) {
            sql += " AND category = ?";
            conditionsList.add(conditions.getCategory());
        }
        if (conditions.getTitle() != null) {
            sql += " AND title LIKE ?";
            conditionsList.add("%" + conditions.getTitle() + "%");
        }
        if (conditions.getPress() != null) {
            sql += " AND press LIKE ?";
            conditionsList.add("%" + conditions.getPress() + "%");
        }
        if (conditions.getMinPublishYear() != null) {
            sql += " AND publish_year >= ?";
            conditionsList.add(conditions.getMinPublishYear());
        }
        if (conditions.getMaxPublishYear() != null) {
            sql += " AND publish_year <= ?";
            conditionsList.add(conditions.getMaxPublishYear());
        }
        if (conditions.getAuthor() != null) {
            sql += " AND author LIKE ?";
            conditionsList.add("%" + conditions.getAuthor() + "%");
        }
        if (conditions.getMinPrice() != null) {
            sql += " AND price >= ?";
            conditionsList.add(conditions.getMinPrice());
        }
        if (conditions.getMaxPrice() != null) {
            sql += " AND price <= ?";
            conditionsList.add(conditions.getMaxPrice());
        }
        if (conditions.getSortBy() != null) {
            sql += " ORDER BY " + conditions.getSortBy().getValue();
        }
        if (conditions.getSortOrder() != null) {
            sql += " " + conditions.getSortOrder().getValue();
        }
        if (conditions.getSortBy() != SortColumn.BOOK_ID) {
            sql += ", book_id";
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int i = 0; i < conditionsList.size(); i++) {
                stmt.setObject(i + 1, conditionsList.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setCategory(rs.getString("category"));
                book.setTitle(rs.getString("title"));
                book.setPress(rs.getString("press"));
                book.setPublishYear(rs.getInt("publish_year"));
                book.setAuthor(rs.getString("author"));
                book.setPrice(rs.getDouble("price"));
                book.setStock(rs.getInt("stock"));
                books.add(book);
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        return new ApiResult(true, "Query books successfully.", new BookQueryResults(books));
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        // Check if the user has borrowed the book and not returned
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM borrow WHERE book_id = ? AND card_id = ? AND return_time = 0");
            stmt.setInt(1, borrow.getBookId());
            stmt.setInt(2, borrow.getCardId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ApiResult(false, "User has borrowed the book and not returned.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Check if the book exists and has stock
        try {
            // add FOR UPDATE to lock the row
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM book WHERE book_id = ? AND stock > 0 FOR UPDATE");
            stmt.setInt(1, borrow.getBookId());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Book does not exist or has no stock.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Add borrow record and update book stock
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO borrow (book_id, card_id, borrow_time) VALUES (?, ?, ?)");
            stmt.setInt(1, borrow.getBookId());
            stmt.setInt(2, borrow.getCardId());
            stmt.setLong(3, borrow.getBorrowTime());
            stmt.executeUpdate();
            stmt = conn.prepareStatement("UPDATE book SET stock = stock - 1 WHERE book_id = ?");
            stmt.setInt(1, borrow.getBookId());
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Borrow book successfully.");
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        // Check if the user has borrowed the book and not returned
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT borrow_time FROM borrow WHERE book_id = ? AND card_id = ? AND return_time = 0");
            stmt.setInt(1, borrow.getBookId());
            stmt.setInt(2, borrow.getCardId());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "User has not borrowed the book or has returned.");
            }
            if (borrow.getReturnTime() <= rs.getLong("borrow_time")) {
                return new ApiResult(false, "Return time is earlier than borrow time.");
            }
            borrow.setBorrowTime(rs.getLong("borrow_time"));
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Update borrow record and update book stock
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE borrow SET return_time = ? WHERE book_id = ? AND card_id = ? AND return_time = 0");
            stmt.setLong(1, borrow.getReturnTime());
            stmt.setInt(2, borrow.getBookId());
            stmt.setInt(3, borrow.getCardId());
            stmt.executeUpdate();
            stmt = conn.prepareStatement("UPDATE book SET stock = stock + 1 WHERE book_id = ?");
            stmt.setInt(1, borrow.getBookId());
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Return book successfully.");
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        List<BorrowHistories.Item> borrows = new ArrayList<>();
        // inquire borrow history, borrow time desc, book id asc
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT borrow.card_id, borrow.book_id, borrow.borrow_time, borrow.return_time, " +
                "book.category, book.title, book.press, book.publish_year, book.author, book.price " +
                "FROM borrow, book " +
                "WHERE borrow.book_id = book.book_id AND borrow.card_id = ? " +
                "ORDER BY borrow.borrow_time DESC, borrow.book_id ASC"
            );
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BorrowHistories.Item item = new BorrowHistories.Item();
                item.setCardId(rs.getInt("card_id"));
                item.setBookId(rs.getInt("book_id"));
                item.setBorrowTime(rs.getLong("borrow_time"));
                item.setReturnTime(rs.getLong("return_time"));
                item.setCategory(rs.getString("category"));
                item.setTitle(rs.getString("title"));
                item.setPress(rs.getString("press"));
                item.setPublishYear(rs.getInt("publish_year"));
                item.setAuthor(rs.getString("author"));
                item.setPrice(rs.getDouble("price"));
                borrows.add(item);
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Query borrow history successfully.", new BorrowHistories(borrows));
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        // Check if the card exists, when <name, department, type> are same, the card exists
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM card WHERE name = ? AND department = ? AND type = ?");
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ApiResult(false, "Card already exists.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        // Register card
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO card (name, department, type) VALUES (?, ?, ?)");
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }

        // set card id back
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT card_id FROM card WHERE name = ? AND department = ? AND type = ?");
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                card.setCardId(rs.getInt("card_id"));
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Register card successfully.");
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        // Check if the card exists
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM card WHERE card_id = ?");
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Card does not exist.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }

        // Check if the card have borrowed books and not returned
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM borrow WHERE card_id = ? AND return_time = 0");
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ApiResult(false, "Card has borrowed books and not returned.");
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        // Remove card
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM card WHERE card_id = ?");
            stmt.setInt(1, cardId);
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Remove card successfully.");
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        List<Card> cards = new ArrayList<>();
        // show all cards
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM card ORDER BY card_id ASC");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Card card = new Card();
                card.setCardId(rs.getInt("card_id"));
                card.setName(rs.getString("name"));
                card.setDepartment(rs.getString("department"));
                card.setType(CardType.values(rs.getString("type")));
                cards.add(card);
            }
        } catch (Exception e) {
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Show cards successfully.", new CardList(cards));
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
