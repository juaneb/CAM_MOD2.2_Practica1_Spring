package es.urjc.code.daw.library.unitTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.Arrays;
import java.util.List;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("Test about get all books")
    public void givenSomeBooksInTheBookServiceWhenCallGetServiceBooksThenReturnAllBooks() throws Exception {
        List<Book> books = Arrays.asList(new Book("Clean Code","Este libro mola todo"), new Book("Junit Patterns","Este libro te enseña los principios de los test unitarios"));
        when(bookService.findAll()).thenReturn(books);

        mvc.perform(get("/api/books/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", equalTo("Clean Code")));

    }


    @Test
    @DisplayName("Test about post one book with user authentication")
    @WithMockUser(username = "user", password = "pass", roles = "USER")
    public void givenOneNewBookInTheBookStoreWhenCallPostServiceBooksThenSuccessfulInsertBook() throws Exception {

        Book book = new Book("Clean Code","Este libro mola todo");
        when(bookService.save(Mockito.any())).thenReturn(book);
        mvc.perform(MockMvcRequestBuilders
                .post("/api/books/")
                .content(asJsonString(book))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.description").value("Este libro mola todo"));
    }

    @Test
    @DisplayName("Test about post one book without user authentication")
    public void givenOneNewBookInTheBookStoreWhenCallPostServiceBooksWithoutAuthThenSuccessfulInsertBook() throws Exception {
        //TODO Refactorizar nombre métodos test

        Book book = new Book("Clean Code","Este libro mola todo");
        when(bookService.save(Mockito.any())).thenReturn(book);

        mvc.perform(MockMvcRequestBuilders
                .post("/api/books/")
                .content(asJsonString(book))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test about delete one book with user authentication and correct role")
    @WithMockUser(username = "admin", password = "pass", roles = "ADMIN")
    public void givenOneBookInTheBookStoreWhenCallDeleteServiceBooksWithAuthThenSuccessfulDeleteBook() throws Exception {

        mvc.perform( MockMvcRequestBuilders.delete("/api/books/{id}", 1) )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test about delete one book with user authentication and wrong role")
    @WithMockUser(username = "admin", password = "pass", roles = "USER")
    public void givenOneBookInTheBookStoreWhenCallDeleteServiceBooksWithAuthAndWrongRoleThenAccessForbidden() throws Exception {
        mvc.perform( MockMvcRequestBuilders.delete("/api/books/{id}", 1) )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test about delete one book without user authentication")
    public void givenOneBookInTheBookStoreWhenCallDeleteServiceBooksWithoutAuthThenAccessUnauthorized() throws Exception {
        mvc.perform( MockMvcRequestBuilders.delete("/api/books/{id}", 1) )
                .andExpect(status().isUnauthorized());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
