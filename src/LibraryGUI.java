import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class LibraryGUI extends JFrame {

    private Library library;
    private JTable table;
    private DefaultTableModel tableModel;
    private static final String DATA_FILE = "library_data.ser";

    public LibraryGUI() {
        super("Library Management System");

        library = new Library();

        // If you have a load method, call it first
        try {
            library = Library.loadFromFile(DATA_FILE);
        } catch (Exception e) {
            library = new Library();
        }

        // 👉 Add this line AFTER loading, BEFORE building the table
        addSampleBooksIfEmpty();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        createMenuBar();

        String[] columnNames = {"Title", "Author", "ISBN", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);


        refreshTable();

        setVisible(true);
    }


    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem loadItem = new JMenuItem("Load");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(e -> saveLibrary());
        loadItem.addActionListener(e -> loadLibrary());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem addBookItem = new JMenuItem("Add Book");
        JMenuItem borrowItem = new JMenuItem("Borrow");
        JMenuItem returnItem = new JMenuItem("Return");
        JMenuItem removeItem = new JMenuItem("Remove Book");


        addBookItem.addActionListener(e -> addBookDialog());
        borrowItem.addActionListener(e -> borrowBookDialog());
        returnItem.addActionListener(e -> returnBookDialog());
        removeItem.addActionListener(e -> removeBookDialog());

        editMenu.add(addBookItem);
        editMenu.add(borrowItem);
        editMenu.add(returnItem);
        editMenu.add(removeItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
    }

    private void refreshTable() {
        tableModel.setRowCount(0); // clear table
        for (Book book : library.getBookList()) {
            String status = book.isAvailable() ? "Available" : "Borrowed";
            Object[] row = {book.getTitle(), book.getAuthor(), book.getIsbn(), status};
            tableModel.addRow(row);
        }
    }

    private void addBookDialog() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();

        Object[] message = {
                "Title:", titleField,
                "Author:", authorField,
                "ISBN:", isbnField
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Add Book",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String isbn = isbnField.getText().trim();

            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "All fields are required.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book book = new Book(title, author, isbn);
            library.addBook(book);
            refreshTable();
        }
    }

    private void borrowBookDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to borrow.",
                    "No book selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get book from library list using row index
        Book selectedBook = library.getBookList().get(selectedRow);

        if (!selectedBook.isAvailable()) {
            JOptionPane.showMessageDialog(this,
                    "This book is already borrowed.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField nameField = new JTextField();
        JTextField idField = new JTextField();

        Object[] message = {
                "Member Name:", nameField,
                "Member ID:", idField
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Borrow Book",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String id = idField.getText().trim();

            if (name.isEmpty() || id.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Name and ID are required.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Member member = new Member(name, id);

            boolean success = library.borrowBook(selectedBook, member);
            if (!success) {
                JOptionPane.showMessageDialog(this,
                        "Could not borrow book.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                refreshTable();
            }
        }
    }

    private void returnBookDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to return.",
                    "No book selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Book selectedBook = library.getBookList().get(selectedRow);

        if (selectedBook.isAvailable()) {
            JOptionPane.showMessageDialog(this,
                    "This book is not borrowed.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ask for member ID to confirm who returns the book
        String memberId = JOptionPane.showInputDialog(
                this,
                "Enter Member ID who returns the book:",
                "Return Book",
                JOptionPane.QUESTION_MESSAGE
        );

        if (memberId == null || memberId.trim().isEmpty()) {
            return;
        }

        // Find the member in the map (simple linear search)
        Member foundMember = null;
        for (Member m : library.getBorrowedBooks().keySet()) {
            if (m.getId().equals(memberId.trim())) {
                foundMember = m;
                break;
            }
        }

        if (foundMember == null) {
            JOptionPane.showMessageDialog(this,
                    "No member with this ID has borrowed this book.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = library.returnBook(selectedBook, foundMember);
        if (!success) {
            JOptionPane.showMessageDialog(this,
                    "Could not return book.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            refreshTable();
        }
    }
    private void removeBookDialog() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to remove.",
                    "No book selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Book selectedBook = library.getBookList().get(selectedRow);

        int response = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove this book?\n" + selectedBook.getTitle(),
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            library.removeBook(selectedBook);
            refreshTable();
        }
    }


    private void saveLibrary() {
        try {
            library.saveToFile(DATA_FILE);
            JOptionPane.showMessageDialog(this,
                    "Library saved successfully.",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving library: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLibrary() {
        try {
            library = Library.loadFromFile(DATA_FILE);
            refreshTable();
            JOptionPane.showMessageDialog(this,
                    "Library loaded successfully.",
                    "Loaded",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading library: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }
    // Add some sample books when the library is empty
    private void addSampleBooksIfEmpty() {
        if (library.getBookList().isEmpty()) {
            library.addBook(new Book("The Hobbit", "J.R.R. Tolkien", "ISBN001"));
            library.addBook(new Book("1984", "George Orwell", "ISBN002"));
            library.addBook(new Book("To Kill a Mockingbird", "Harper Lee", "ISBN003"));
            library.addBook(new Book("The Great Gatsby", "F. Scott Fitzgerald", "ISBN004"));
            library.addBook(new Book("Moby Dick", "Herman Melville", "ISBN005"));
            library.addBook(new Book("Pride and Prejudice", "Jane Austen", "ISBN006"));
            library.addBook(new Book("The Catcher in the Rye", "J.D. Salinger", "ISBN007"));
            library.addBook(new Book("Harry Potter 1", "J.K. Rowling", "ISBN008"));
            library.addBook(new Book("The Lord of the Rings", "J.R.R. Tolkien", "ISBN009"));
            library.addBook(new Book("The Alchemist", "Paulo Coelho", "ISBN010"));
        }
    }

    public static void main(String[] args) {
        // Use Swing's event dispatch thread
        SwingUtilities.invokeLater(() -> new LibraryGUI());
    }

}
