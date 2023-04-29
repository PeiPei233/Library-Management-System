import utils.DatabaseConnector;
import utils.RandomData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.RandomUtils;

import entities.*;
import entities.Card.CardType;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.BorrowHistories;
import queries.CardList;
import queries.BorrowHistories.Item;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;

public class LMSUI {
    
    private final LibraryManagementSystem library;
    private static final Logger log = Logger.getLogger(Main.class.getName());
    private final JPanel homePanel = new JPanel();
    private final JPanel bookPanel = new JPanel();
    private final JPanel cardPanel = new JPanel();
    private final JPanel borrowPanel = new JPanel();
    private final JFrame frame = new JFrame("Library Management System");

    LMSUI(DatabaseConnector connector) {
        this.library = new LibraryManagementSystemImpl(connector);
        
        // TEST CODE
        library.resetDatabase();
        createLibrary(library, 200, 20, 100);

        // main frame
        frame.setSize(1200, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (connector.release()) {
                    log.info("Success to release connection.");
                } else {
                    log.warning("Failed to release connection.");
                }
            }
        });

        // menu bar
        JMenuItem homeMenu = new JMenuItem("Home");
        homeMenu.setFont(new Font(null, Font.PLAIN, 20));
        homeMenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JMenuItem bookMenu = new JMenuItem("Book Management");
        bookMenu.setFont(new Font(null, Font.PLAIN, 20));
        bookMenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JMenuItem cardMenu = new JMenuItem("Card Management");
        cardMenu.setFont(new Font(null, Font.PLAIN, 20));
        cardMenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JMenuItem borrowMenu = new JMenuItem("Borrow Management");
        borrowMenu.setFont(new Font(null, Font.PLAIN, 20));
        borrowMenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(homeMenu);
        menuBar.add(bookMenu);
        menuBar.add(cardMenu);
        menuBar.add(borrowMenu);

        // menu actions
        homeMenu.addActionListener(e -> {
            showHomePanel();
        });
        bookMenu.addActionListener(e -> {
            showBookPanel();
        });
        cardMenu.addActionListener(e -> {
            showCardPanel();
        });
        borrowMenu.addActionListener(e -> {
            showBorrowPanel();
        });

        frame.setJMenuBar(menuBar);
        
        createHomePanel();
        createBookPanel();
        createCardPanel();
        createBorrowPanel();

        showHomePanel();

    }

    private void createLibrary(LibraryManagementSystem library, int nBooks, int nCards, int nBorrows) {
        /* create books */
        Set<Book> bookSet = new HashSet<>();
        while (bookSet.size() < nBooks) {
            bookSet.add(RandomData.randomBook());
        }
        List<Book> bookList = new ArrayList<>(bookSet);
        library.storeBook(bookList);
        /* create cards */
        List<Card> cardList = new ArrayList<>();
        for (int i = 0; i < nCards; i++) {
            Card c = new Card();
            c.setName(String.format("User%05d", i));
            c.setDepartment(RandomData.randomDepartment());
            c.setType(Card.CardType.random());
            cardList.add(c);
            library.registerCard(c);
        }
        /* create histories */
        PriorityQueue<Long> mills = new PriorityQueue<>();
        for (int i = 0; i < nBorrows * 2; i++) {
            mills.add(RandomData.randomTime());
        }
        for (int i = 0; i < nBorrows;) {
            Book b = bookList.get(RandomUtils.nextInt(0, nBooks));
            if (b.getStock() == 0) {
                continue;
            }
            i++;
            Card c = cardList.get(RandomUtils.nextInt(0, nCards));
            Borrow r = new Borrow();
            r.setCardId(c.getCardId());
            r.setBookId(b.getBookId());
            r.setBorrowTime(mills.poll());
            r.setReturnTime(mills.poll());
            library.borrowBook(r);
            library.returnBook(r);
        }
    }

    private void showHomePanel() {
        log.info("Show home panel.");
        frame.setContentPane(homePanel);
        frame.revalidate();
    }

    private void showBookPanel() {
        log.info("Show book panel.");
        frame.setContentPane(bookPanel);
        frame.revalidate();
    }

    private void showCardPanel() {
        log.info("Show card panel.");
        frame.setContentPane(cardPanel);
        frame.revalidate();
    }

    private void showBorrowPanel() {
        log.info("Show borrow panel.");
        frame.setContentPane(borrowPanel);
        frame.revalidate();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(null, Font.PLAIN, 16));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2),
            BorderFactory.createEmptyBorder(10, 20, 20, 20)));
        return label;
    }

    private void createHomePanel() {

        homePanel.setBackground(Color.WHITE);
        homePanel.setLayout(new BorderLayout());
        homePanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel homeLabel = new JLabel("Home");
        homeLabel.setFont(new Font(null, Font.BOLD, 36));
        homePanel.add(homeLabel, BorderLayout.NORTH);

        JLabel toHome = createLabel("<html> <h1>Home</h1> <br> <p>The home page of the Library Management System. </p></html>");
        toHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showHomePanel();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                toHome.setBackground(Color.LIGHT_GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                toHome.setBackground(Color.WHITE);
            }
        });
        JLabel toBook = createLabel("<html><h1>Book Management</h1><br> <p>Add, Modify or Query the Book Infomation.</p> <p>Update the Stock of the Book.</p> </html>");
        toBook.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showBookPanel();
                toBook.setBackground(Color.WHITE);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                toBook.setBackground(Color.LIGHT_GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                toBook.setBackground(Color.WHITE);
            }
        });
        JLabel toCard = createLabel("<html><h1>Card Management</h1><br> <p>Add or Query the Card Infomation.</p> </html>");
        toCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCardPanel();
                toCard.setBackground(Color.WHITE);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                toCard.setBackground(Color.LIGHT_GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                toCard.setBackground(Color.WHITE);
            }
        });
        JLabel toBorrow = createLabel("<html><h1>Borrow Management</h1><br> <p>Borrow or Return a book.</p> <p>Query the records of borrows.</p> </html>");
        toBorrow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showBorrowPanel();
                toBorrow.setBackground(Color.WHITE);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                toBorrow.setBackground(Color.LIGHT_GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                toBorrow.setBackground(Color.WHITE);
            }
        });

        JPanel homeCenterPanel = new JPanel(new GridLayout(2, 2, 60, 60));
        homeCenterPanel.add(toHome);
        homeCenterPanel.add(toBook);
        homeCenterPanel.add(toCard);
        homeCenterPanel.add(toBorrow);
        homeCenterPanel.setBackground(Color.WHITE);
        homeCenterPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));        

        homePanel.add(homeCenterPanel, BorderLayout.CENTER);
    }

    private void createBookPanel() {
        bookPanel.setBackground(Color.WHITE);
        bookPanel.setLayout(new BorderLayout());
        bookPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel bookLabel = new JLabel("Book Management");
        bookLabel.setFont(new Font(null, Font.BOLD, 36));
        bookPanel.add(bookLabel, BorderLayout.NORTH);

        JPanel bookInfoPanel = new JPanel();
        bookInfoPanel.setLayout(new BoxLayout(bookInfoPanel, BoxLayout.Y_AXIS));
        bookInfoPanel.setBackground(Color.WHITE);
        bookInfoPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        bookPanel.add(bookInfoPanel, BorderLayout.CENTER);
        JPanel bookOperationPanel = new JPanel();
        JMenuBar bookOperationBar = new JMenuBar();
        bookOperationBar.setBackground(Color.WHITE);
        bookOperationBar.setMinimumSize(new Dimension(100, 50));

        JMenuItem addBook = new JMenuItem("Add Book");
        addBook.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem modifyBook = new JMenuItem("Modify Book");
        modifyBook.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem queryBook = new JMenuItem("Query Book");
        queryBook.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem deleteBook = new JMenuItem("Delete Book");
        deleteBook.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem updateStock = new JMenuItem("Update Stock");
        updateStock.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem batchImport = new JMenuItem("Batch Import");
        batchImport.setFont(new Font(null, Font.PLAIN, 16));

        bookOperationBar.add(addBook);
        bookOperationBar.add(modifyBook);
        bookOperationBar.add(queryBook);
        bookOperationBar.add(deleteBook);
        bookOperationBar.add(updateStock);
        bookOperationBar.add(batchImport);
        bookOperationPanel.add(bookOperationBar);
        bookOperationPanel.setBackground(Color.WHITE);
        bookInfoPanel.add(bookOperationPanel, BorderLayout.NORTH);

        JPanel bookInfoEnterPanel = new JPanel(new GridLayout(4, 4, 40, 10));
        bookInfoEnterPanel.setBackground(Color.WHITE);
        bookInfoEnterPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        bookInfoPanel.add(bookInfoEnterPanel, BorderLayout.CENTER);

        JLabel bookIDLabel = new JLabel("Book ID: ");
        bookIDLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(bookIDLabel);
        JTextField bookIDField = new JTextField(15);
        bookIDField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(bookIDField);

        JLabel titleLabel = new JLabel("Title: ");
        titleLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(titleLabel);
        JTextField titleField = new JTextField(15);
        titleField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(titleField);

        JLabel authorLabel = new JLabel("Author: ");
        authorLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(authorLabel);
        JTextField authorField = new JTextField(15);
        authorField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(authorField);

        JLabel pressLabel = new JLabel("Press: ");
        pressLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(pressLabel);
        JTextField pressField = new JTextField(15);
        pressField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(pressField);

        JLabel categoryLabel = new JLabel("Category: ");
        categoryLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(categoryLabel);
        JTextField categoryField = new JTextField(15);
        categoryField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(categoryField);

        JLabel priceLabel = new JLabel("Price: ");
        priceLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(priceLabel);
        JTextField priceField = new JTextField(15);
        priceField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(priceField);

        JTextField minPriceField = new JTextField(15);
        minPriceField.setFont(new Font(null, Font.PLAIN, 16));
        JTextField maxPriceField = new JTextField(15);
        maxPriceField.setFont(new Font(null, Font.PLAIN, 16));

        JLabel priceToLabel = new JLabel("to");
        priceToLabel.setFont(new Font(null, Font.PLAIN, 16));
        priceToLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.X_AXIS));
        pricePanel.setBackground(Color.WHITE);
        pricePanel.add(minPriceField);
        pricePanel.add(priceToLabel);
        pricePanel.add(maxPriceField);

        JLabel publishYearLabel = new JLabel("Publish Year: ");
        publishYearLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(publishYearLabel);
        JTextField publishYearField = new JTextField(15);
        publishYearField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(publishYearField);

        JTextField minPublishYearField = new JTextField(15);
        minPublishYearField.setFont(new Font(null, Font.PLAIN, 16));
        JTextField maxPublishYearField = new JTextField(15);
        maxPublishYearField.setFont(new Font(null, Font.PLAIN, 16));

        JLabel publishYearToLabel = new JLabel("to");
        publishYearToLabel.setFont(new Font(null, Font.PLAIN, 16));
        publishYearToLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel publishYearPanel = new JPanel();
        publishYearPanel.setLayout(new BoxLayout(publishYearPanel, BoxLayout.X_AXIS));
        publishYearPanel.setBackground(Color.WHITE);
        publishYearPanel.add(minPublishYearField);
        publishYearPanel.add(publishYearToLabel);
        publishYearPanel.add(maxPublishYearField);

        JLabel stockLabel = new JLabel("Stock: ");
        stockLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(stockLabel);
        JTextField stockField = new JTextField(15);
        stockField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(stockField);

        JLabel stockIncLabel = new JLabel("Stock Increment: ");
        stockIncLabel.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(stockIncLabel);
        JTextField stockIncField = new JTextField(15);
        stockIncField.setFont(new Font(null, Font.PLAIN, 16));
        bookInfoEnterPanel.add(stockIncField);

        DefaultTableModel bookTableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return Integer.class;
                    case 5:
                        return String.class;
                    case 6:
                        return Double.class;
                    case 7:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookTable = new JTable(bookTableModel);
        String[] bookTableColumnNames = {"Book ID", "Category", "Title", "Press", "Publish Year", "Author", "Price", "Stock"};
        bookTableModel.setColumnIdentifiers(bookTableColumnNames);
        bookTable.setFont(new Font(null, Font.PLAIN, 16));
        bookTable.getTableHeader().setFont(new Font(null, Font.BOLD, 16));
        bookTable.setCellSelectionEnabled(true);
        bookTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        bookTable.setRowHeight(25);
        bookTable.setAutoCreateRowSorter(true);
        JPanel bookTablePanel = new JPanel(new BorderLayout());
        bookTablePanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        bookTablePanel.setBackground(Color.WHITE);
        bookTablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        
        
        JButton addBookButton = new JButton("Add Book");
        addBookButton.setFont(new Font(null, Font.PLAIN, 16));
        addBookButton.addActionListener(e -> {
            String title = titleField.getText();
            String author = authorField.getText();
            String press = pressField.getText();
            String category = categoryField.getText();
            String price = priceField.getText();
            String publishYear = publishYearField.getText();
            String stock = stockField.getText();
            if (title.equals("") || author.equals("") || press.equals("") || category.equals("") || price.equals("") || publishYear.equals("") || stock.equals("")) {
                JOptionPane.showMessageDialog(null, "Please fill in all the blanks!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    double priceDouble = Double.parseDouble(price);
                    int publishYearInt = Integer.parseInt(publishYear);
                    int stockInt = Integer.parseInt(stock);
                    if (priceDouble <= 0 || publishYearInt <= 0 || stockInt <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        log.info("Add book: " + category + " " + title + " " + press + " " + publishYearInt + " " + author + " " + priceDouble + " " + stockInt);
                        Book book = new Book(category, title, press, publishYearInt, author, priceDouble, stockInt);
                        ApiResult res = library.storeBook(book);
                        if (res.ok) {
                            JOptionPane.showMessageDialog(null,
                                "<html>" +
                                    "<p>Add book successfully!</p> <br>" +
                                    "<p>Book ID:" + book.getBookId() + " </p>" +
                                    "<p>Category: " + book.getCategory() + "</p>" +
                                    "<p>Title: " + book.getTitle() + "</p>" +
                                    "<p>Press: " + book.getPress() + "</p>" +
                                    "<p>Publish Year: " + book.getPublishYear() + "</p>" +
                                    "<p>Author: " + book.getAuthor() + "</p>" +
                                    "<p>Price: " + book.getPrice() + "</p>" +
                                    "<p>Stock: " + book.getStock() + "</p>" +
                                "</html>", "Success", JOptionPane.INFORMATION_MESSAGE);
                            bookIDField.setText("" + book.getBookId());
                        } else {
                            JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        
        JButton modifyBookButton = new JButton("Modify Book");
        modifyBookButton.setFont(new Font(null, Font.PLAIN, 16));
        modifyBookButton.addActionListener(e -> {
            String bookID = bookIDField.getText();
            String title = titleField.getText();
            String author = authorField.getText();
            String press = pressField.getText();
            String category = categoryField.getText();
            String price = priceField.getText();
            String publishYear = publishYearField.getText();
            if (bookID.equals("") || title.equals("") || author.equals("") || press.equals("") || category.equals("") || price.equals("") || publishYear.equals("")) {
                JOptionPane.showMessageDialog(null, "Please fill in all the blanks!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    int bookIDInt = Integer.parseInt(bookID);
                    double priceDouble = Double.parseDouble(price);
                    int publishYearInt = Integer.parseInt(publishYear);
                    if (bookIDInt <= 0 || priceDouble <= 0 || publishYearInt <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        log.info("Modify book: " + category + " " + title + " " + press + " " + publishYearInt + " " + author + " " + priceDouble);
                        Book book = new Book(category, title, press, publishYearInt, author, priceDouble, 0);
                        book.setBookId(bookIDInt);
                        ApiResult res = library.modifyBookInfo(book);
                        if (res.ok) {
                            JOptionPane.showMessageDialog(null,
                                "<html>" +
                                    "<p>Modify book successfully!</p> <br>" +
                                    "<p>Book ID:" + book.getBookId() + " </p>" +
                                    "<p>Category: " + book.getCategory() + "</p>" +
                                    "<p>Title: " + book.getTitle() + "</p>" +
                                    "<p>Press: " + book.getPress() + "</p>" +
                                    "<p>Publish Year: " + book.getPublishYear() + "</p>" +
                                    "<p>Author: " + book.getAuthor() + "</p>" +
                                    "<p>Price: " + book.getPrice() + "</p>" +
                                    "<p>Stock: " + book.getStock() + "</p>" +
                                "</html>", "Success", JOptionPane.INFORMATION_MESSAGE);
                            stockField.setText("" + book.getStock());
                        } else {
                            JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton queryBookButton = new JButton("Query Book");
        queryBookButton.setFont(new Font(null, Font.PLAIN, 16));
        queryBookButton.addActionListener(e -> {
            String category = categoryField.getText();
            String title = titleField.getText();
            String press = pressField.getText();
            String minPulishYear = minPublishYearField.getText();
            String maxPulishYear = maxPublishYearField.getText();
            String author = authorField.getText();
            String minPrice = minPriceField.getText();
            String maxPrice = maxPriceField.getText();
            try {
                BookQueryConditions conditions = new BookQueryConditions();
                if (!category.equals("")) {
                    conditions.setCategory(category);
                }
                if (!title.equals("")) {
                    conditions.setTitle(title);
                }
                if (!press.equals("")) {
                    conditions.setPress(press);
                }
                if (!minPulishYear.equals("")) {
                    int minPulishYearInt;
                    try {
                        minPulishYearInt = Integer.parseInt(minPulishYear);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (minPulishYearInt <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    conditions.setMinPublishYear(minPulishYearInt);
                }
                if (!maxPulishYear.equals("")) {
                    int maxPulishYearInt;
                    try {
                        maxPulishYearInt = Integer.parseInt(maxPulishYear);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (maxPulishYearInt <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    conditions.setMaxPublishYear(maxPulishYearInt);
                }
                if (!author.equals("")) {
                    conditions.setAuthor(author);
                }
                if (!minPrice.equals("")) {
                    double minPriceDouble;
                    try {
                        minPriceDouble = Double.parseDouble(minPrice);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (minPriceDouble <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    conditions.setMinPrice(minPriceDouble);
                }
                if (!maxPrice.equals("")) {
                    double maxPriceDouble;
                    try {
                        maxPriceDouble = Double.parseDouble(maxPrice);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (maxPriceDouble <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    conditions.setMaxPrice(maxPriceDouble);
                }
                log.info("Query book: " + category + " " + title + " " + press + " " + minPulishYear + " " + maxPulishYear + " " + author + " " + minPrice + " " + maxPrice);
                ApiResult res = library.queryBook(conditions);
                if (res.ok) {
                    BookQueryResults results = (BookQueryResults) res.payload;
                    if (results.getCount() == 0) {
                        JOptionPane.showMessageDialog(null, "No books found!", "Error", JOptionPane.ERROR_MESSAGE);
                        bookTableModel.setDataVector(null, bookTableColumnNames);
                        return;
                    } else {
                        List<Book> books = results.getResults();
                        Object[][] data = new Object[results.getCount()][8];
                        for (int i = 0; i < results.getCount(); i++) {
                            Book book = books.get(i);
                            data[i][0] = book.getBookId();
                            data[i][1] = book.getCategory();
                            data[i][2] = book.getTitle();
                            data[i][3] = book.getPress();
                            data[i][4] = book.getPublishYear();
                            data[i][5] = book.getAuthor();
                            data[i][6] = book.getPrice();
                            data[i][7] = book.getStock();
                        }
                        bookTableModel.setDataVector(data, bookTableColumnNames);
                        JOptionPane.showMessageDialog(null, "Query book successfully, See the results in the table!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton deleteBookButton = new JButton("Delete Book");
        deleteBookButton.setFont(new Font(null, Font.PLAIN, 16));
        deleteBookButton.addActionListener(e -> {
            String bookID = bookIDField.getText();
            if (bookID.equals("")) {
                JOptionPane.showMessageDialog(null, "Please fill in the blanks!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    int bookIDInt = Integer.parseInt(bookID);
                    if (bookIDInt <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        log.info("Delete book: " + bookIDInt);
                        ApiResult res = library.removeBook(bookIDInt);
                        if (res.ok) {
                            JOptionPane.showMessageDialog(null, "Delete book successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton updateStockButton = new JButton("Update Stock");
        updateStockButton.setFont(new Font(null, Font.PLAIN, 16));
        updateStockButton.addActionListener(e -> {
            String bookID = bookIDField.getText();
            String stock = stockIncField.getText();
            if (bookID.equals("") || stock.equals("")) {
                JOptionPane.showMessageDialog(null, "Please fill in all the blanks!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    int bookIDInt = Integer.parseInt(bookID);
                    int stockInt = Integer.parseInt(stock);
                    if (bookIDInt <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter positive numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        log.info("Increment stock: " + bookIDInt + " " + stockInt);
                        ApiResult res = library.incBookStock(bookIDInt, stockInt);
                        if (res.ok) {
                            JOptionPane.showMessageDialog(null, "Update stock successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter numbers!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        List<JComponent> addBookComponents = Arrays.asList(
            bookIDLabel, bookIDField,
            categoryLabel, categoryField,
            titleLabel, titleField,
            pressLabel, pressField,
            publishYearLabel, publishYearField,
            authorLabel, authorField,
            priceLabel, priceField,
            stockLabel, stockField
        );

        List<JComponent> modifyBookComponents = Arrays.asList(
            bookIDLabel, bookIDField,
            categoryLabel, categoryField,
            titleLabel, titleField,
            pressLabel, pressField,
            publishYearLabel, publishYearField,
            authorLabel, authorField,
            priceLabel, priceField,
            stockLabel, stockField
        );
        
        List<JComponent> deleteBookComponents = Arrays.asList(
            bookIDLabel, bookIDField
        );

        List<JComponent> updateStockComponents = Arrays.asList(
            bookIDLabel, bookIDField,
            stockIncLabel, stockIncField
        );

        List<JComponent> queryBookComponents = Arrays.asList(
            categoryLabel, categoryField,
            titleLabel, titleField,
            pressLabel, pressField,
            publishYearLabel, publishYearPanel,
            authorLabel, authorField,
            priceLabel, pricePanel
        );

        bookInfoEnterPanel.removeAll();
        for (JComponent component : addBookComponents) {
            bookInfoEnterPanel.add(component);
        }
        bookInfoPanel.add(addBookButton);
        bookInfoPanel.add(modifyBookButton);
        bookInfoPanel.add(queryBookButton);
        bookInfoPanel.add(deleteBookButton);
        bookInfoPanel.add(updateStockButton);

        addBookButton.setVisible(true);
        modifyBookButton.setVisible(false);
        queryBookButton.setVisible(false);
        deleteBookButton.setVisible(false);
        updateStockButton.setVisible(false);
        bookIDField.setEditable(false);

        addBook.addActionListener(e -> {
            bookInfoEnterPanel.removeAll();
            bookInfoEnterPanel.setLayout(new GridLayout(4, 4, 40, 10));
            for (JComponent component : addBookComponents) {
                bookInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            addBookButton.setVisible(true);
            modifyBookButton.setVisible(false);
            queryBookButton.setVisible(false);
            deleteBookButton.setVisible(false);
            updateStockButton.setVisible(false);
            bookIDField.setText("");
            bookIDField.setEditable(false);
            bookInfoPanel.revalidate();
        });

        modifyBook.addActionListener(e -> {
            bookInfoEnterPanel.removeAll();
            bookInfoEnterPanel.setLayout(new GridLayout(4, 4, 40, 10));
            for (JComponent component : modifyBookComponents) {
                bookInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            addBookButton.setVisible(false);
            modifyBookButton.setVisible(true);
            queryBookButton.setVisible(false);
            deleteBookButton.setVisible(false);
            updateStockButton.setVisible(false);
            stockField.setText("");
            stockField.setEditable(false);
            bookInfoPanel.revalidate();
        });

        queryBook.addActionListener(e -> {
            bookInfoEnterPanel.removeAll();
            bookInfoEnterPanel.setLayout(new GridLayout(3, 4, 40, 10));
            
            for (JComponent component : queryBookComponents) {
                bookInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            
            addBookButton.setVisible(false);
            modifyBookButton.setVisible(false);
            queryBookButton.setVisible(true);
            deleteBookButton.setVisible(false);
            updateStockButton.setVisible(false);
            bookInfoPanel.revalidate();
        });

        deleteBook.addActionListener(e -> {
            bookInfoEnterPanel.removeAll();
            bookInfoEnterPanel.setLayout(new GridLayout(1, 2, 40, 10));
            for (JComponent component : deleteBookComponents) {
                bookInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            addBookButton.setVisible(false);
            modifyBookButton.setVisible(false);
            queryBookButton.setVisible(false);
            deleteBookButton.setVisible(true);
            updateStockButton.setVisible(false);
            bookInfoPanel.revalidate();
        });

        updateStock.addActionListener(e -> {
            bookInfoEnterPanel.removeAll();
            bookInfoEnterPanel.setLayout(new GridLayout(1, 4, 40, 10));
            for (JComponent component : updateStockComponents) {
                bookInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            addBookButton.setVisible(false);
            modifyBookButton.setVisible(false);
            queryBookButton.setVisible(false);
            deleteBookButton.setVisible(false);
            updateStockButton.setVisible(true);
            bookInfoPanel.revalidate();
        });

        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        batchImport.addActionListener(e -> {
            
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    List<Book> books = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        String[] bookInfo = line.split(",");
                        if (bookInfo.length != 7) {
                            JOptionPane.showMessageDialog(null, "Invalid book info", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        for (String info : bookInfo) {
                            if (info == null || info.trim().isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Invalid book info", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        Book book = new Book();
                        book.setCategory(bookInfo[0].trim());
                        book.setTitle(bookInfo[1].trim());
                        book.setPress(bookInfo[2].trim());
                        try {
                            int publishYearInt = Integer.parseInt(bookInfo[3].trim());
                            if (publishYearInt < 0) {
                                JOptionPane.showMessageDialog(null, "Publish year must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            book.setPublishYear(publishYearInt);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Publish year must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        book.setAuthor(bookInfo[4].trim());
                        try {
                            double priceDouble = Double.parseDouble(bookInfo[5].trim());
                            if (priceDouble < 0) {
                                JOptionPane.showMessageDialog(null, "Price must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            book.setPrice(priceDouble);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Price must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        try {
                            int stockInt = Integer.parseInt(bookInfo[6].trim());
                            if (stockInt < 0) {
                                JOptionPane.showMessageDialog(null, "Stock must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            book.setStock(stockInt);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Stock must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        books.add(book);
                    }
                    ApiResult res = library.storeBook(books);
                    if (res.ok) {
                        Object[][] data = new Object[books.size()][8];
                        for (int i = 0; i < books.size(); i++) {
                            Book book = books.get(i);
                            data[i][0] = book.getBookId();
                            data[i][1] = book.getCategory();
                            data[i][2] = book.getTitle();
                            data[i][3] = book.getPress();
                            data[i][4] = book.getPublishYear();
                            data[i][5] = book.getAuthor();
                            data[i][6] = book.getPrice();
                            data[i][7] = book.getStock();
                        }
                        bookTableModel.setDataVector(data, bookTableColumnNames);
                        JOptionPane.showMessageDialog(null, "Import successfully, The imported books information will show in the table.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException e1) {
                    JOptionPane.showMessageDialog(null, "File not found", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "File not found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        

        bookInfoPanel.add(bookTablePanel);
    }

    private void createCardPanel() {
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel cardLabel = new JLabel("Card Management");
        cardLabel.setFont(new Font(null, Font.BOLD, 36));
        cardPanel.add(cardLabel, BorderLayout.NORTH);

        JPanel cardInfoPanel = new JPanel();
        cardInfoPanel.setLayout(new BoxLayout(cardInfoPanel, BoxLayout.Y_AXIS));
        cardInfoPanel.setBackground(Color.WHITE);
        cardInfoPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        cardPanel.add(cardInfoPanel, BorderLayout.CENTER);
        JPanel cardOperationPanel = new JPanel();
        JMenuBar cardOperationBar = new JMenuBar();
        cardOperationBar.setBackground(Color.WHITE);
        cardOperationBar.setMinimumSize(new Dimension(100, 50));

        JMenuItem registerCard = new JMenuItem("Register Card");
        registerCard.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem removeCard = new JMenuItem("Remove Card");
        removeCard.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem listAllCards = new JMenuItem("List All Cards");
        listAllCards.setFont(new Font(null, Font.PLAIN, 16));

        cardOperationBar.add(registerCard);
        cardOperationBar.add(removeCard);
        cardOperationBar.add(listAllCards);
        cardOperationPanel.add(cardOperationBar);
        cardOperationPanel.setBackground(Color.WHITE);
        cardInfoPanel.add(cardOperationPanel, BorderLayout.NORTH);

        JPanel cardInfoEnterPanel = new JPanel(new GridLayout(2, 4, 40, 10));
        cardInfoEnterPanel.setBackground(Color.WHITE);
        cardInfoEnterPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        cardInfoPanel.add(cardInfoEnterPanel, BorderLayout.CENTER);

        JLabel cardIdLabel = new JLabel("Card ID: ");
        cardIdLabel.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(cardIdLabel);
        JTextField cardIdField = new JTextField(15);
        cardIdField.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(cardIdField);

        JLabel userNameLabel = new JLabel("Name: ");
        userNameLabel.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(userNameLabel);
        JTextField userNameField = new JTextField(15);
        userNameField.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(userNameField);

        JLabel departmentLabel = new JLabel("Department: ");
        departmentLabel.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(departmentLabel);
        JTextField departmentField = new JTextField(15);
        departmentField.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(departmentField);

        JLabel typeLabel = new JLabel("Type: ");
        typeLabel.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(typeLabel);
        JComboBox<String> typeBox = new JComboBox<String>();
        typeBox.addItem("Student");
        typeBox.addItem("Teacher");
        typeBox.setFont(new Font(null, Font.PLAIN, 16));
        cardInfoEnterPanel.add(typeBox);

        DefaultTableModel cardTableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    default:
                        return String.class;
                }
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable cardTable = new JTable(cardTableModel);
        String[] cardTableColumnNames = {"Card ID", "Name", "Department", "Type"};
        cardTableModel.setColumnIdentifiers(cardTableColumnNames);
        cardTable.setFont(new Font(null, Font.PLAIN, 16));
        cardTable.getTableHeader().setFont(new Font(null, Font.BOLD, 16));
        cardTable.setCellSelectionEnabled(true);
        cardTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        cardTable.setRowHeight(25);
        cardTable.setAutoCreateRowSorter(true);
        JPanel cardTablePanel = new JPanel(new BorderLayout()); 
        cardTablePanel.add(new JScrollPane(cardTable), BorderLayout.CENTER);
        cardTablePanel.setBackground(Color.WHITE);
        cardTablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton registerCardButton = new JButton("Register Card");
        registerCardButton.setFont(new Font(null, Font.PLAIN, 16));
        registerCardButton.addActionListener(e -> {
            String name = userNameField.getText();
            String department = departmentField.getText();
            String type = (String) typeBox.getSelectedItem();
            if (name.isEmpty() || department.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill in all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                CardType cardType = CardType.values(type.toUpperCase().substring(0, 1));
                Card card = new Card(0, name, department, cardType);
                ApiResult res = library.registerCard(card);
                if (res.ok) {
                    JOptionPane.showMessageDialog(null, 
                        "<html>" +
                            "<p>Register card successfully!</p> <br>" +
                            "<p>Card ID:" + card.getCardId() + " </p>" +
                            "<p>Name: " + card.getName() + "</p>" +
                            "<p>Department: " + card.getDepartment() + "</p>" +
                            "<p>Type: " + (card.getType() == CardType.Student ? "Student" : "Teacher") + "</p>" +
                        "</html>", "Success", JOptionPane.INFORMATION_MESSAGE);
                    cardIdField.setText("" + card.getCardId());
                } else {
                    JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton removeCardButton = new JButton("Remove Card");
        removeCardButton.setFont(new Font(null, Font.PLAIN, 16));
        removeCardButton.addActionListener(e -> {
            String cardId = cardIdField.getText();
            if (cardId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill in all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    ApiResult res = library.removeCard(Integer.parseInt(cardId));
                    if (res.ok) {
                        JOptionPane.showMessageDialog(null, "Remove card successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        cardIdField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Card ID must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        List<JComponent> registerCardComponents = Arrays.asList(
            cardIdLabel, cardIdField,
            userNameLabel, userNameField,
            departmentLabel, departmentField,
            typeLabel, typeBox
        );

        List<JComponent> removeCardComponents = Arrays.asList(
            cardIdLabel, cardIdField
        );

        cardInfoEnterPanel.removeAll();
        for (JComponent component : registerCardComponents) {
            cardInfoEnterPanel.add(component);
            if (component instanceof JTextField) {
                ((JTextField) component).setEditable(true);
            }
        }
        cardInfoPanel.add(registerCardButton);
        cardInfoPanel.add(removeCardButton);

        registerCardButton.setVisible(true);
        removeCardButton.setVisible(false);
        cardIdField.setEditable(false);

        registerCard.addActionListener(e -> {
            cardInfoEnterPanel.removeAll();
            cardInfoEnterPanel.setLayout(new GridLayout(2, 4, 40, 10));
            for (JComponent component : registerCardComponents) {
                cardInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            registerCardButton.setVisible(true);
            removeCardButton.setVisible(false);
            cardIdField.setText("");
            cardIdField.setEditable(false);
            cardInfoPanel.revalidate();
        });

        removeCard.addActionListener(e -> {
            cardInfoEnterPanel.removeAll();
            cardInfoEnterPanel.setLayout(new GridLayout(1, 2, 40, 10));
            for (JComponent component : removeCardComponents) {
                cardInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            registerCardButton.setVisible(false);
            removeCardButton.setVisible(true);
            cardInfoPanel.revalidate();
        });
        
        listAllCards.addActionListener(e -> {
            ApiResult res = library.showCards();
            if (res.ok) {
                CardList cardList = (CardList) res.payload;
                if (cardList.getCount() == 0) {
                    JOptionPane.showMessageDialog(null, "No card found", "Error", JOptionPane.ERROR_MESSAGE);
                    cardTableModel.setDataVector(null, cardTableColumnNames);
                    return;
                } else {
                    List<Card> cards = cardList.getCards();
                    Object[][] cardTableData = new Object[cardList.getCount()][4];
                    for (int i = 0; i < cardList.getCount(); i++) {
                        Card card = cards.get(i);
                        cardTableData[i][0] = card.getCardId();
                        cardTableData[i][1] = card.getName();
                        cardTableData[i][2] = card.getDepartment();
                        cardTableData[i][3] = card.getType() == CardType.Student ? "Student" : "Teacher";
                    }
                    cardTableModel.setDataVector(cardTableData, cardTableColumnNames);
                    JOptionPane.showMessageDialog(null, "Cards are all shown in the table!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cardInfoPanel.add(cardTablePanel);

    }

    private void createBorrowPanel() {
        borrowPanel.setBackground(Color.WHITE);
        borrowPanel.setLayout(new BorderLayout());
        borrowPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel borrowLabel = new JLabel("Borrow Management");
        borrowLabel.setFont(new Font(null, Font.BOLD, 36));
        borrowPanel.add(borrowLabel, BorderLayout.NORTH);

        JPanel borrowInfoPanel = new JPanel();
        borrowInfoPanel.setLayout(new BoxLayout(borrowInfoPanel, BoxLayout.Y_AXIS));
        borrowInfoPanel.setBackground(Color.WHITE);
        borrowInfoPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        borrowPanel.add(borrowInfoPanel, BorderLayout.CENTER);
        JPanel borrowOperationPanel = new JPanel();
        JMenuBar borrowOperationBar = new JMenuBar();
        borrowOperationBar.setBackground(Color.WHITE);
        borrowOperationBar.setMinimumSize(new Dimension(100, 50));

        JMenuItem borrowBook = new JMenuItem("Borrow Book");
        borrowBook.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem returnBook = new JMenuItem("Return Book");
        returnBook.setFont(new Font(null, Font.PLAIN, 16));

        JMenuItem showBorrowHistory = new JMenuItem("Show Borrow History");
        showBorrowHistory.setFont(new Font(null, Font.PLAIN, 16));

        borrowOperationBar.add(borrowBook);
        borrowOperationBar.add(returnBook);
        borrowOperationBar.add(showBorrowHistory);
        borrowOperationPanel.add(borrowOperationBar);
        borrowOperationPanel.setBackground(Color.WHITE);
        borrowInfoPanel.add(borrowOperationPanel);

        JPanel borrowInfoEnterPanel = new JPanel(new GridLayout(2, 4, 40, 10));
        borrowInfoEnterPanel.setBackground(Color.WHITE);
        borrowInfoEnterPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        borrowInfoPanel.add(borrowInfoEnterPanel, BorderLayout.CENTER);

        JLabel cardIdLabel = new JLabel("Card ID");
        cardIdLabel.setFont(new Font(null, Font.PLAIN, 16));
        JTextField cardIdField = new JTextField(15);
        cardIdField.setFont(new Font(null, Font.PLAIN, 16));

        JLabel bookIdLabel = new JLabel("Book ID");
        bookIdLabel.setFont(new Font(null, Font.PLAIN, 16));
        JTextField bookIdField = new JTextField(15);
        bookIdField.setFont(new Font(null, Font.PLAIN, 16));

        JLabel borrowTimeLabel = new JLabel("Borrow Time");
        borrowTimeLabel.setFont(new Font(null, Font.PLAIN, 16));
        JTextField borrowTimeField = new JTextField(15);
        borrowTimeField.setFont(new Font(null, Font.PLAIN, 16));

        JLabel returnTimeLabel = new JLabel("Return Time");
        returnTimeLabel.setFont(new Font(null, Font.PLAIN, 16));
        JTextField returnTimeField = new JTextField(15);
        returnTimeField.setFont(new Font(null, Font.PLAIN, 16));

        DefaultTableModel borrowTableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return Integer.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return String.class;
                    case 5:
                        return Integer.class;
                    case 6:
                        return String.class;
                    case 7:
                        return Double.class;
                    case 8:
                        return String.class;
                    case 9:
                        return String.class;
                    default:
                        return String.class;
                }
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable borrowTable = new JTable(borrowTableModel);
        String[] borrowTableColumnNames = {"Card ID", "Book ID", "Category", "Title", "Press", "Publish Year", "Author", "Price", "Borrow Time", "Return Time"};
        borrowTableModel.setColumnIdentifiers(borrowTableColumnNames);
        borrowTable.setFont(new Font(null, Font.PLAIN, 16));
        borrowTable.getTableHeader().setFont(new Font(null, Font.BOLD, 16));
        borrowTable.setCellSelectionEnabled(true);
        borrowTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        borrowTable.setRowHeight(25);
        borrowTable.setAutoCreateRowSorter(true);
        JPanel borrowTablePanel = new JPanel(new BorderLayout());
        borrowTablePanel.add(new JScrollPane(borrowTable), BorderLayout.CENTER);
        borrowTablePanel.setBackground(Color.WHITE);
        borrowTablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton borrowButton = new JButton("Borrow Book");
        borrowButton.setFont(new Font(null, Font.PLAIN, 16));
        borrowButton.addActionListener(e -> {
            try {
                int cardId = Integer.parseInt(cardIdField.getText());
                int bookId = Integer.parseInt(bookIdField.getText());
                long borrowTime = System.currentTimeMillis();
                Borrow borrow = new Borrow(cardId, bookId);
                borrow.setBorrowTime(borrowTime);
                ApiResult res = library.borrowBook(borrow);
                if (res.ok) {
                    Instant borrowInstant = Instant.ofEpochMilli(borrowTime);
                    LocalDateTime borrowLocalDateTime = LocalDateTime.ofInstant(borrowInstant, ZoneId.systemDefault());
                    String returnTimeStr = borrowLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    borrowTimeField.setText(returnTimeStr);
                    JOptionPane.showMessageDialog(null, 
                        "<html>" +
                            "<p>Borrow book successfully!</p> <br>" +
                            "<p>Card ID:" + borrow.getCardId() + " </p>" +
                            "<p>Book ID: " + borrow.getBookId() + "</p>" +
                            "<p>Borrow Time: " + returnTimeStr + "</p>" +
                        "</html>", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Card ID and Book ID must be integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton returnButton = new JButton("Return Book");
        returnButton.setFont(new Font(null, Font.PLAIN, 16));
        returnButton.addActionListener(e -> {
            try {
                int cardId = Integer.parseInt(cardIdField.getText());
                int bookId = Integer.parseInt(bookIdField.getText());
                long returnTime = System.currentTimeMillis();
                Borrow borrow = new Borrow(cardId, bookId);
                borrow.setReturnTime(returnTime);
                ApiResult res = library.returnBook(borrow);
                if (res.ok) {
                    long borrowTime = borrow.getBorrowTime();
                    Instant borrowInstant = Instant.ofEpochMilli(borrowTime);
                    LocalDateTime borrowLocalDateTime = LocalDateTime.ofInstant(borrowInstant, ZoneId.systemDefault());
                    String borrowTimeStr = borrowLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    borrowTimeField.setText(borrowTimeStr);
                    Instant returnInstant = Instant.ofEpochMilli(returnTime);
                    LocalDateTime returnLocalDateTime = LocalDateTime.ofInstant(returnInstant, ZoneId.systemDefault());
                    String returnTimeStr = returnLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    returnTimeField.setText(returnTimeStr);
                    JOptionPane.showMessageDialog(null, 
                        "<html>" +
                            "<p>Return book successfully!</p> <br>" +
                            "<p>Card ID:" + borrow.getCardId() + " </p>" +
                            "<p>Book ID: " + borrow.getBookId() + "</p>" +
                            "<p>Borrow Time: " + borrowTimeStr + "</p>" +
                            "<p>Return Time: " + returnTimeStr + "</p>" +
                        "</html>", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Card ID and Book ID must be integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton borrowHistoryButton = new JButton("Show Borrow History");
        borrowHistoryButton.setFont(new Font(null, Font.PLAIN, 16));
        borrowHistoryButton.addActionListener(e -> {
            try {
                int cardId = Integer.parseInt(cardIdField.getText());
                ApiResult res = library.showBorrowHistory(cardId);
                if (res.ok) {
                    BorrowHistories borrowHistories = (BorrowHistories) res.payload;
                    if (borrowHistories.getCount() == 0) {
                        JOptionPane.showMessageDialog(null, "No borrow history of Card ID " + cardId + " found.", "Error", JOptionPane.ERROR_MESSAGE);
                        borrowTableModel.setDataVector(null, borrowTableColumnNames);
                        return;
                    } else {
                        List<Item> borrowHistoryList = borrowHistories.getItems();
                        Object[][] borrowHistoryData = new Object[borrowHistoryList.size()][10];
                        for (int i = 0; i < borrowHistoryList.size(); i++) {
                            Item item = borrowHistoryList.get(i);
                            borrowHistoryData[i][0] = item.getCardId();
                            borrowHistoryData[i][1] = item.getBookId();
                            borrowHistoryData[i][2] = item.getCategory();
                            borrowHistoryData[i][3] = item.getTitle();
                            borrowHistoryData[i][4] = item.getPress();
                            borrowHistoryData[i][5] = item.getPublishYear();
                            borrowHistoryData[i][6] = item.getAuthor();
                            borrowHistoryData[i][7] = item.getPrice();
                            long borrowTime = item.getBorrowTime();
                            Instant borrowInstant = Instant.ofEpochMilli(borrowTime);
                            LocalDateTime borrowLocalDateTime = LocalDateTime.ofInstant(borrowInstant, ZoneId.systemDefault());
                            borrowHistoryData[i][8] = borrowLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            long returnTime = item.getReturnTime();
                            if (returnTime == 0) {
                                borrowHistoryData[i][9] = "Not Returned";
                            } else {
                                Instant returnInstant = Instant.ofEpochMilli(returnTime);
                                LocalDateTime returnLocalDateTime = LocalDateTime.ofInstant(returnInstant, ZoneId.systemDefault());
                                borrowHistoryData[i][9] = returnLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            }
                        }
                        borrowTableModel.setDataVector(borrowHistoryData, borrowTableColumnNames);
                        JOptionPane.showMessageDialog(null, "Borrow History Query Success!", "Success", JOptionPane.PLAIN_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, res.message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(null, "Card ID must be an integer", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        List<JComponent> borrowComponents = Arrays.asList(
            cardIdLabel, cardIdField,
            bookIdLabel, bookIdField,
            borrowTimeLabel, borrowTimeField,
            returnTimeLabel, returnTimeField
        );

        List<JComponent> returnComponents = Arrays.asList(
            cardIdLabel, cardIdField,
            bookIdLabel, bookIdField,
            borrowTimeLabel, borrowTimeField,
            returnTimeLabel, returnTimeField
        );

        List<JComponent> borrowHistoryComponents = Arrays.asList(
            cardIdLabel, cardIdField
        );

        
        borrowInfoEnterPanel.removeAll();
        borrowInfoEnterPanel.setLayout(new GridLayout(2, 4, 40, 10));
        for (JComponent component : borrowComponents) {
            borrowInfoEnterPanel.add(component);
            if (component instanceof JTextField) {
                ((JTextField) component).setEditable(true);
            }
        }

        borrowInfoPanel.add(borrowButton);
        borrowInfoPanel.add(returnButton);
        borrowInfoPanel.add(borrowHistoryButton);

        borrowButton.setVisible(true);
        returnButton.setVisible(false);
        borrowHistoryButton.setVisible(false);
        borrowTimeField.setEditable(false);
        returnTimeField.setEditable(false);
        borrowInfoPanel.revalidate();

        borrowBook.addActionListener(e -> {
            borrowInfoEnterPanel.removeAll();
            borrowInfoEnterPanel.setLayout(new GridLayout(2, 4, 40, 10));
            for (JComponent component : borrowComponents) {
                borrowInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            borrowButton.setVisible(true);
            returnButton.setVisible(false);
            borrowHistoryButton.setVisible(false);
            borrowTimeField.setEditable(false);
            returnTimeField.setEditable(false);
            borrowTimeField.setText("");
            returnTimeField.setText("");
            borrowInfoPanel.revalidate();
        });

        returnBook.addActionListener(e -> {
            borrowInfoEnterPanel.removeAll();
            borrowInfoEnterPanel.setLayout(new GridLayout(2, 4, 40, 10));
            for (JComponent component : returnComponents) {
                borrowInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            borrowButton.setVisible(false);
            returnButton.setVisible(true);
            borrowHistoryButton.setVisible(false);
            borrowTimeField.setEditable(false);
            returnTimeField.setEditable(false);
            borrowTimeField.setText("");
            returnTimeField.setText("");
            borrowInfoPanel.revalidate();
        });

        showBorrowHistory.addActionListener(e -> {
            borrowInfoEnterPanel.removeAll();
            borrowInfoEnterPanel.setLayout(new GridLayout(1, 2, 40, 10));
            for (JComponent component : borrowHistoryComponents) {
                borrowInfoEnterPanel.add(component);
                if (component instanceof JTextField) {
                    ((JTextField) component).setEditable(true);
                }
            }
            borrowButton.setVisible(false);
            returnButton.setVisible(false);
            borrowHistoryButton.setVisible(true);
            borrowInfoPanel.revalidate();
        });

        borrowInfoPanel.add(borrowTablePanel);

    }

}
