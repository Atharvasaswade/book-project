/*
    The book project lets a user keep track of different books they would like to read, are currently
    reading, have read or did not finish.
    Copyright (C) 2020  Karan Kumar

    This program is free software: you can redistribute it and/or modify it under the terms of the
    GNU General Public License as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT ANY
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
    PURPOSE.  See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with this program.
    If not, see <https://www.gnu.org/licenses/>.
 */

package com.karankumar.bookproject.backend.entity;

import com.karankumar.bookproject.annotations.IntegrationTest;
import com.karankumar.bookproject.backend.service.AuthorService;
import com.karankumar.bookproject.backend.service.BookService;
import com.karankumar.bookproject.backend.service.PredefinedShelfService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@IntegrationTest
class AuthorTest {
    private BookService bookService;
    private AuthorService authorService;

    private PredefinedShelf toRead;

    @Autowired
    void AuthorServiceTest(BookService bookService, AuthorService authorService) {
        this.bookService = bookService;
        this.authorService = authorService;
        resetBookService();
    }

    @BeforeEach
    public void reset(@Autowired PredefinedShelfService predefinedShelfService) {
        resetBookService();
        toRead = predefinedShelfService.findToReadShelf();
    }

    private void resetBookService() {
        bookService.deleteAll();
    }

    private static Book createBook(String title, PredefinedShelf shelf) {
        Author author = new Author("Steven", "Pinker");
        return new Book(title, author, shelf);
    }

    /**
     * Updating the author of a book should only affect that book, not any other book that
     * originally had the same author name
     */
    @Test
    void updateAuthorAffectsOneRow() {
        // given
        Book testBook1 = createBook("How the mind works", toRead);
        Book testBook2 = createBook("The better angels of our nature", toRead);
        bookService.save(testBook1);
        bookService.save(testBook2);

        // when
        Author newAuthor = new Author("Matthew", "Walker");
        testBook1.setAuthor(newAuthor);
        bookService.save(testBook1);

        // then
        Assertions.assertNotEquals(testBook1.getAuthor(), testBook2.getAuthor());
    }

    @Test
    void removeOrphanAuthors() {
        assumeThat(authorService.findAll()).isEmpty();

        // given
        Author orphan = new Author("Jostein", "Gaarder");
        Book book = new Book("Sophie's World", orphan, toRead);
        bookService.save(book);

        // when
        bookService.delete(book);

        // then
        assertThat(authorService.findAll()).isEmpty();
    }
    @Test
    @DisplayName("Non-orphan authors shouldn't be removed when one of their books is deleted")
    void notRemoveNonOrphans() {
        // given
        Author nonOrphan = new Author("Jostein", "Gaarder");

        Book book = new Book("Sophie's World", nonOrphan, toRead);
        bookService.save(book);

        Book book2 = new Book("The Other World", nonOrphan, toRead);
        bookService.save(book2);

        // when
        bookService.delete(book);
        assumeThat(bookService.count()).isOne();

        // then
        assertThat(authorService.count()).isOne();
    }
}
