import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Library implements Serializable {

    private ArrayList<Book> bookList;
    private HashMap<Member, List<Book>> borrowedBooks;

    public Library() {
        bookList = new ArrayList<>();
        borrowedBooks = new HashMap<>();
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public HashMap<Member, List<Book>> getBorrowedBooks() {
        return borrowedBooks;
    }

    // Add a new book
    public void addBook(Book book) {
        if (book != null) {
            bookList.add(book);
        }
    }

    // Remove a book (not strictly required but nice to have)
    public void removeBook(Book book) {
        if (book != null) {
            bookList.remove(book);
        }
    }

    // Borrow a book
    public boolean borrowBook(Book book, Member member) {
        if (book == null || member == null) {
            return false;
        }

        if (!book.isAvailable()) {
            return false; // already borrowed
        }

        book.setBorrowed();

        List<Book> list = borrowedBooks.get(member);
        if (list == null) {
            list = new ArrayList<>();
            borrowedBooks.put(member, list);
        }
        list.add(book);

        return true;
    }

    // Return a book
    public boolean returnBook(Book book, Member member) {
        if (book == null || member == null) {
            return false;
        }

        if (book.isAvailable()) {
            return false; // already available
        }

        List<Book> list = borrowedBooks.get(member);
        if (list == null || !list.contains(book)) {
            // Member did not borrow this book
            return false;
        }

        book.setReturned();
        list.remove(book);

        // If member has no more books, remove them from map
        if (list.isEmpty()) {
            borrowedBooks.remove(member);
        }

        return true;
    }

    // Save the whole Library object to a file
    public void saveToFile(String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(fileName))) {
            oos.writeObject(this);
        }
    }

    // Static method to load Library from file
    public static Library loadFromFile(String fileName)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName))) {
            return (Library) ois.readObject();
        }
    }
}
