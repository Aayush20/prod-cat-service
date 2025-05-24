package org.example.prodcatservice.services;

import com.sendgrid.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.*;

class EmailAlertServiceTest {

    @InjectMocks
    private EmailAlertService emailAlertService;

    @Mock
    private SendGrid sendGrid;

    @Mock
    private Response response;

    @Captor
    private ArgumentCaptor<Request> requestCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Inject mock SendGrid API key and emails
        ReflectionTestUtils.setField(emailAlertService, "sendGridApiKey", "DUMMY_KEY");
        ReflectionTestUtils.setField(emailAlertService, "fromEmail", "alert@yourapp.com");
        ReflectionTestUtils.setField(emailAlertService, "toEmail", "admin@yourapp.com");
    }

    @Test
    void testSendLowStockAlertSuccess() throws IOException {
        // Arrange
        SendGrid mockSendGrid = mock(SendGrid.class);
        when(mockSendGrid.api(any())).thenReturn(new Response(202, "Accepted", null));
        ReflectionTestUtils.setField(emailAlertService, "sendGridApiKey", "dummy-api-key");

        // Replace internal SendGrid instance
        EmailAlertService spyService = spy(emailAlertService);
        doReturn(mockSendGrid).when(spyService).getSendGridInstance();

        // Act
        spyService.sendLowStockAlert("TestProduct", 100L, 2);

        // Verify send method called
        verify(mockSendGrid, times(1)).api(any());
    }

    @Test
    void testSendLowStockAlertFailureHandledGracefully() throws IOException {
        SendGrid mockSendGrid = mock(SendGrid.class);
        when(mockSendGrid.api(any())).thenThrow(new IOException("Send failed"));

        EmailAlertService spyService = spy(emailAlertService);
        doReturn(mockSendGrid).when(spyService).getSendGridInstance();

        spyService.sendLowStockAlert("FailProduct", 200L, 1);

        verify(mockSendGrid).api(any());
        // no exception should be thrown
    }
}
