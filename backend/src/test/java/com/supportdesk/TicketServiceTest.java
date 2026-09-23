package com.supportdesk;

import com.supportdesk.dto.ticket.CreateTicketRequest;
import com.supportdesk.dto.ticket.TicketDetailResponse;
import com.supportdesk.dto.ticket.UpdateTicketStatusRequest;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.UserPrincipal;
import com.supportdesk.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User customerUser;
    private User otherCustomer;
    private User agentUser;
    private UserPrincipal customerPrincipal;
    private UserPrincipal otherCustomerPrincipal;
    private UserPrincipal agentPrincipal;
    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .id(1L)
                .fullName("Alice Customer")
                .email("alice@example.com")
                .role(Role.ROLE_CUSTOMER)
                .build();

        otherCustomer = User.builder()
                .id(2L)
                .fullName("Bob Customer")
                .email("bob@example.com")
                .role(Role.ROLE_CUSTOMER)
                .build();

        agentUser = User.builder()
                .id(3L)
                .fullName("Agent Smith")
                .email("smith@supportdesk.com")
                .role(Role.ROLE_AGENT)
                .build();

        customerPrincipal = new UserPrincipal(1L, "Alice Customer", "alice@example.com", "pass", Role.ROLE_CUSTOMER, Collections.emptyList());
        otherCustomerPrincipal = new UserPrincipal(2L, "Bob Customer", "bob@example.com", "pass", Role.ROLE_CUSTOMER, Collections.emptyList());
        agentPrincipal = new UserPrincipal(3L, "Agent Smith", "smith@supportdesk.com", "pass", Role.ROLE_AGENT, Collections.emptyList());

        sampleTicket = Ticket.builder()
                .id(100L)
                .ticketNumber("TCK-TEST-01")
                .title("Database connection issue")
                .description("Cannot establish JDBC connection")
                .category(TicketCategory.TECHNICAL)
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.OPEN)
                .customer(customerUser)
                .build();
    }

    @Test
    @DisplayName("Customer creates ticket successfully with default OPEN status")
    void createTicket_Success() {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .title("Database connection issue")
                .description("Cannot establish JDBC connection")
                .category(TicketCategory.TECHNICAL)
                .priority(TicketPriority.HIGH)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(sampleTicket);

        TicketDetailResponse response = ticketService.createTicket(request, customerPrincipal);

        assertNotNull(response);
        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertEquals("Database connection issue", response.getTitle());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("BOLA Defense: Bob cannot access Alice's ticket and receives AccessDeniedException")
    void getTicketById_BOLA_Protection_ThrowsAccessDenied() {
        when(ticketRepository.findWithDetailsById(100L)).thenReturn(Optional.of(sampleTicket));

        assertThrows(AccessDeniedException.class, () ->
                ticketService.getTicketById(100L, otherCustomerPrincipal)
        );
    }

    @Test
    @DisplayName("Agent can access any ticket successfully")
    void getTicketById_Agent_Success() {
        when(ticketRepository.findWithDetailsById(100L)).thenReturn(Optional.of(sampleTicket));

        TicketDetailResponse response = ticketService.getTicketById(100L, agentPrincipal);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    @DisplayName("Customer cannot close ticket that is still OPEN (must be RESOLVED first)")
    void updateTicketStatus_Customer_CannotCloseOpenTicket() {
        when(ticketRepository.findWithDetailsById(100L)).thenReturn(Optional.of(sampleTicket));
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(TicketStatus.CLOSED);

        assertThrows(BadRequestException.class, () ->
                ticketService.updateTicketStatus(100L, request, customerPrincipal)
        );
    }
}
