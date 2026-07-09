import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

public class LibraryTest {

    @Test
    public void testAddBook() {
        Library library = new Library();
        int initialSize = library.getBookList().size();

        Book book = new Book("Test Title", "Test Author", "123");
        library.addBook(book);

        assertEquals(initialSize + 1, library.getBookList().size());
        assertTrue(library.getBookList().contains(book));
    }

    @Test
    public void testRemoveBook() {
        Library library = new Library();
        Book book = new Book("To Remove", "Author", "999");
        library.addBook(book);

        assertTrue(library.getBookList().contains(book));

        library.removeBook(book);

        assertFalse(library.getBookList().contains(book));
    }

    @Test
    public void testBorrowBook() {
        Library library = new Library();
        Book book = new Book("Title", "Author", "111");
        library.addBook(book);

        Member member = new Member("John", "M1");
        boolean result = library.borrowBook(book, member);

        assertTrue(result);
        assertFalse(book.isAvailable());

        List<Book> borrowed = library.getBorrowedBooks().get(member);
        assertNotNull(borrowed);
        assertTrue(borrowed.contains(book));
    }

    @Test
    public void testBorrowBookAlreadyBorrowed() {
        Library library = new Library();
        Book book = new Book("Title", "Author", "222");
        library.addBook(book);

        Member member1 = new Member("John", "M1");
        Member member2 = new Member("Mary", "M2");

        // First borrow should work
        assertTrue(library.borrowBook(book, member1));
        assertFalse(book.isAvailable());

        // Second borrow should fail
        boolean result = library.borrowBook(book, member2);
        assertFalse(result);

        // Book still linked to first member
        List<Book> borrowedBy1 = library.getBorrowedBooks().get(member1);
        assertTrue(borrowedBy1.contains(book));
    }

    @Test
    public void testBorrowBookNullBook() {
        Library library = new Library();
        Member member = new Member("John", "M1");

        boolean result = library.borrowBook(null, member);
        assertFalse(result);
    }

    @Test
    public void testBorrowBookNullMember() {
        Library library = new Library();
        Book book = new Book("Title", "Author", "333");
        library.addBook(book);

        boolean result = library.borrowBook(book, null);
        assertFalse(result);
        assertTrue(book.isAvailable());
    }

    @Test
    public void testReturnBook() {
        Library library = new Library();
        Book book = new Book("Title", "Author", "111");
        library.addBook(book);

        Member member = new Member("John", "M1");
        library.borrowBook(book, member);

        boolean result = library.returnBook(book, member);

        assertTrue(result);
        assertTrue(book.isAvailable());

        List<Book> borrowed = library.getBorrowedBooks().get(member);
        if (borrowed != null) {
            assertFalse(borrowed.contains(book));
        }
    }

    @Test
    public void testReturnBookNotBorrowed() {
        Library library = new Library();
        Book book = new Book("Title", "Author", "444");
        library.addBook(book);

        Member member = new Member("John", "M1");

        // Book is available, not borrowed
        assertTrue(book.isAvailable());

        boolean result = library.returnBook(book, member);
        assertFalse(result); // cannot return if not borrowed
    }

    @Test
    public void testReturnBookWrongMember() {
        Library library = new Library();
        Book book = new Book("Title", "Author", "555");
        library.addBook(book);

        Member correctMember = new Member("John", "M1");
        Member wrongMember = new Member("Mary", "M2");

        library.borrowBook(book, correctMember);

        boolean result = library.returnBook(book, wrongMember);
        assertFalse(result); // wrong member cannot return it

        // Still borrowed by the correct member
        assertFalse(book.isAvailable());
        List<Book> list = library.getBorrowedBooks().get(correctMember);
        assertNotNull(list);
        assertTrue(list.contains(book));
    }

    @Test
    public void testReturnBookNullBook() {
        Library library = new Library();
        Member member = new Member("John", "M1");

        boolean result = library.returnBook(null, member);
        assertFalse(result);
    }

    @Test
    public void testReturnBookNullMember() {
        Library library = new Library();
        Book book = new Book("Title", "Author", "666");
        library.addBook(book);

        boolean result = library.returnBook(book, null);
        assertFalse(result);
    }

    @Test
    public void testMemberRemovedFromMapWhenNoMoreBooks() {
        Library library = new Library();
        Book book1 = new Book("Book1", "A", "1");
        Book book2 = new Book("Book2", "B", "2");
        library.addBook(book1);
        library.addBook(book2);

        Member member = new Member("John", "M1");
        library.borrowBook(book1, member);
        library.borrowBook(book2, member);

        // Return both books
        library.returnBook(book1, member);
        library.returnBook(book2, member);

        // After last book is returned, member should be removed from map
        assertFalse(library.getBorrowedBooks().containsKey(member));
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        Library library = new Library();
        library.addBook(new Book("A", "B", "1"));
        library.addBook(new Book("C", "D", "2"));

        String fileName = "test_library.ser";
        library.saveToFile(fileName);

        Library loaded = Library.loadFromFile(fileName);

        assertEquals(library.getBookList().size(), loaded.getBookList().size());
        assertEquals(library.getBookList().get(0).getTitle(),
                loaded.getBookList().get(0).getTitle());
        assertEquals(library.getBookList().get(1).getIsbn(),
                loaded.getBookList().get(1).getIsbn());

        // clean up
        new File(fileName).delete();
    }
}
