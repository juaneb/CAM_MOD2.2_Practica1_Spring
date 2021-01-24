package es.urjc.code.daw.library.integrationTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import io.restassured.RestAssured;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookRestE2EControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation ();
        RestAssured.baseURI = "https://localhost:" + port;
    }

    @Test
    @DisplayName("Test about get all books")
    public void givenSomeBooksInTheBookServiceWhenCallGetServiceBooksThenReturnAllBooks() {
        when().
                get("/api/books/").
                then().
                statusCode(200).
                body("title", hasItems("SUEÑOS DE ACERO Y NEON","LA VIDA SECRETA DE LA MENTE","CASI SIN QUERER","TERMINAMOS Y OTROS POEMAS SIN TERMINAR","LA LEGIÓN PERDIDA"));

    }

    @Test
    @DisplayName("Test about post one book with user authentication")
    public void givenOneNewBookInTheBookStoreWhenCallPostServiceBooksThenSuccessfulInsertBook() throws JsonProcessingException {

        Book book = new Book("Clean Code","Libro para los que se aburren");
        Response response = given().auth().basic("user","pass").contentType(ContentType.JSON).body(new ObjectMapper().writeValueAsString(book)).
        when().
                post("/api/books/").andReturn();
        response.then().
                statusCode(201).
                body("title", equalTo("Clean Code")).
                body("description", equalTo("Libro para los que se aburren"));

        int id = from(response.getBody().asString()).get("id");

        when().
                get("/api/books/{id}",id).
        then().
                statusCode(200).
                body("title", equalTo("Clean Code")).
                body("description", equalTo("Libro para los que se aburren"));
    }

    @Test
    @DisplayName("Test about delete one book with user authentication and correct role")
    public void givenOneBookInTheBookStoreWhenCallDeleteServiceBooksWithAuthThenSuccessfulDeleteBook() throws JsonProcessingException {

        Book book = new Book("Clean Code","Libro para los que se aburren");
        Response response = given().auth().basic("admin","pass").contentType(ContentType.JSON).body(new ObjectMapper().writeValueAsString(book)).
                when().
                post("/api/books/").andReturn();
        response.then().
                statusCode(201).
                body("title", equalTo("Clean Code")).
                body("description", equalTo("Libro para los que se aburren"));

        int id = from(response.getBody().asString()).get("id");

        when().
                get("/api/books/{id}",id).
                then().
                statusCode(200).
                body("title", equalTo("Clean Code")).
                body("description", equalTo("Libro para los que se aburren"));

        Response responseDelete = given().auth().basic("admin","pass").contentType(ContentType.JSON).body(new ObjectMapper().writeValueAsString(book)).
                when().
                delete("/api/books/{id}",id).andReturn();
        responseDelete.then().
                statusCode(200);

        when().
                get("/api/books/{id}",id).
                then().
                statusCode(404);
    }



}