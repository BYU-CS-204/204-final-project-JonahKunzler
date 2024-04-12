package app.view;

import app.model.Book;
import app.model.requestresponse.Request;
import app.model.requestresponse.Response;
import app.service.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.IOException;

class MainPresenterTest {

  private MainPresenter classUnderTest;
  private I_MainView mockView;
  private Service mockTitleService;
  private Service mockAuthorService;
  private Service mockTopicService;

  @BeforeEach
  void setUp() {
    mockView = Mockito.mock(I_MainView.class);
    mockTitleService = Mockito.mock(Service.class);
    mockAuthorService = Mockito.mock(Service.class);
    mockTopicService = Mockito.mock(Service.class);

    classUnderTest = new MainPresenter(mockView, mockTitleService, mockAuthorService, mockTopicService);

    try {
      Mockito.when(mockTitleService.run(new Request("Exception"))).thenThrow(new IOException("Network error"));
      Mockito.when(mockAuthorService.run(new Request("Exception"))).thenThrow(new IOException("Network error"));
      Mockito.when(mockTopicService.run(new Request("Exception"))).thenThrow(new IOException("Network error"));
    } catch (IOException e) {
      //
    }
  }

  @Test
  void titleSearch_ShouldDisplayResults_WhenInputIsValid() throws IOException {
    String validInput = "Valid Title";
    Book[] expectedBooks = new Book[]{ new Book(validInput, "Author Name", new String[]{"Topic"}) };
    Response mockResponse = new Response(expectedBooks);
    Mockito.when(mockTitleService.run(new Request(validInput))).thenReturn(mockResponse);
    classUnderTest.titleSearch(validInput);
    Mockito.verify(mockTitleService).run(new Request(validInput));
    Mockito.verify(mockView).displayTitleSearchResults(expectedBooks);
  }

  @Test
  void titleSearch_ShouldDisplayErrorMessage_WhenInputIsInvalid() {
    String invalidInput = "";
    classUnderTest.titleSearch(invalidInput);
    Mockito.verify(mockView).displayErrorMessage("Invalid input.  Please enter a valid title.");
  }


  @Test
  void titleSearch_ShouldDisplayErrorMessage_OnException() throws IOException {
    String input = "Exception";
    classUnderTest.titleSearch(input);
    Mockito.verify(mockTitleService).run(new Request(input));
    Mockito.verify(mockView).displayErrorMessage("An exception occurred: Network error");
  }

  @Test
  void authorSearch_ShouldDisplayResults_WhenInputIsValid() throws IOException {
    String validInput = "Valid Author";
    Book[] expectedBooks = new Book[]{ new Book("Title", validInput, new String[]{"Topic"}) };
    Response mockResponse = new Response(expectedBooks);
    Mockito.when(mockAuthorService.run(new Request(validInput))).thenReturn(mockResponse);
    classUnderTest.authorSearch(validInput);
    Mockito.verify(mockAuthorService).run(new Request(validInput));
    Mockito.verify(mockView).displayAuthorSearchResults(expectedBooks);
  }

  @Test
  void authorSearch_ShouldDisplayErrorMessage_WhenInputIsInvalid() {
    String invalidInput = "";

    classUnderTest.authorSearch(invalidInput);

    Mockito.verify(mockView).displayErrorMessage("Invalid input.  Please enter a valid author name.");
  }


  @Test
  void authorSearch_ShouldDisplayErrorMessage_OnException() throws IOException {
    String input = "Exception";

    classUnderTest.authorSearch(input);

    Mockito.verify(mockAuthorService).run(new Request(input));
    Mockito.verify(mockView).displayErrorMessage("An exception occurred: Network error");
  }



  @Test
  void topicSearch_ShouldDisplayResults_WhenInputIsValid() throws IOException {
    String validInput = "Valid Topic";
    Book[] expectedBooks = new Book[]{
            new Book("Book Title 1", "Author Name 1", new String[]{validInput}),
            new Book("Book Title 2", "Author Name 2", new String[]{validInput})
    };
    Response mockResponse = new Response(expectedBooks);
    Mockito.when(mockTopicService.run(new Request(validInput))).thenReturn(mockResponse);

    classUnderTest.topicSearch(validInput);

    Mockito.verify(mockTopicService).run(new Request(validInput));
    Mockito.verify(mockView).displayTopicSearchResults(expectedBooks);
  }



  @Test
  void topicSearch_ShouldDisplayErrorMessage_OnException() throws IOException {
    String input = "Exception";

    classUnderTest.topicSearch(input);

    Mockito.verify(mockTopicService).run(new Request(input));
    Mockito.verify(mockView).displayErrorMessage("An exception occurred: Network error");
  }
  @Test
  void topicSearch_ShouldDisplayErrorMessage_WhenInputIsInvalid() {
    String invalidInput = "";

    classUnderTest.topicSearch(invalidInput);

    Mockito.verify(mockView).displayErrorMessage("Invalid input.  Please enter a valid topic.");
  }


}
