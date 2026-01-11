package com.iverpa.mpi.service;

import com.iverpa.mpi.dao.repository.ComplaintRepository;
import com.iverpa.mpi.dao.repository.ConvoyRepository;
import com.iverpa.mpi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ConvoyRepository convoyRepository;

    @InjectMocks
    private ComplaintService complaintService;

    private User user;
    private Convoy convoy;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        User escort = new User();
        escort.setId(2L);
        escort.setUsername("escortUser");

        convoy = new Convoy();
        convoy.setId(1L);
        convoy.setEscort(escort);
    }

    @Test
    void submitComplaint_ShouldCreateNewComplaint_WhenConvoyExists() {
        // Arrange
        Long convoyId = 1L;
        when(convoyRepository.findById(convoyId)).thenReturn(Optional.of(convoy));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        complaintService.submitComplaint(convoyId);

        // Assert
        verify(convoyRepository).findById(convoyId);
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void submitComplaint_ShouldThrowException_WhenConvoyDoesNotExist() {
        // Arrange
        Long convoyId = 1L;
        when(convoyRepository.findById(convoyId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> complaintService.submitComplaint(convoyId)
        );
        assertEquals("Конвой не найден", exception.getMessage());
        verify(convoyRepository).findById(convoyId);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void getActiveConvoys_ShouldReturnListOfConvoys() {
        // Arrange
        Convoy convoy1 = new Convoy();
        convoy1.setId(1L);
        
        Convoy convoy2 = new Convoy();
        convoy2.setId(2L);
        
        List<Convoy> expectedConvoys = List.of(convoy1, convoy2);
        when(convoyRepository.findAll()).thenReturn(expectedConvoys);

        // Act
        List<Convoy> result = complaintService.getActiveConvoys();

        // Assert
        assertEquals(expectedConvoys, result);
        verify(convoyRepository).findAll();
    }

    @Test
    void getComplaintsForPolice_ShouldReturnComplaintsWithNewAndInProgressStatus() {
        // Arrange
        Complaint complaint1 = new Complaint();
        complaint1.setStatus(ComplaintStatus.NEW);
        
        Complaint complaint2 = new Complaint();
        complaint2.setStatus(ComplaintStatus.IN_PROGRESS);
        
        List<Complaint> expectedComplaints = List.of(complaint1, complaint2);
        when(complaintRepository.findAllByStatusIn(List.of(ComplaintStatus.NEW, ComplaintStatus.IN_PROGRESS)))
                .thenReturn(expectedComplaints);

        // Act
        List<Complaint> result = complaintService.getComplaintsForPolice();

        // Assert
        assertEquals(expectedComplaints, result);
        verify(complaintRepository).findAllByStatusIn(List.of(ComplaintStatus.NEW, ComplaintStatus.IN_PROGRESS));
    }

    @Test
    void getActiveComplaint_ShouldReturnComplaint_WhenUserHasActiveComplaint() {
        // Arrange
        Complaint complaint = new Complaint();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setAssignedTo(user);
        
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.of(complaint));

        // Act
        Optional<Complaint> result = complaintService.getActiveComplaint(user);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(complaint, result.get());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
    }

    @Test
    void getActiveComplaint_ShouldReturnEmpty_WhenUserHasNoActiveComplaint() {
        // Arrange
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        // Act
        Optional<Complaint> result = complaintService.getActiveComplaint(user);

        // Assert
        assertFalse(result.isPresent());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
    }

    @Test
    void takeComplaint_ShouldAssignComplaintToUser_WhenUserHasNoActiveComplaintAndComplaintExists() {
        // Arrange
        Long convoyId = 1L;
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(convoyRepository.findById(convoyId)).thenReturn(Optional.of(convoy));
        
        Complaint complaint = new Complaint();
        complaint.setStatus(ComplaintStatus.NEW);
        complaint.setConvoy(convoy);
        when(complaintRepository.findAllByConvoy(convoy)).thenReturn(List.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        Complaint result = complaintService.takeComplaint(convoyId, user);

        // Assert
        assertNotNull(result);
        assertEquals(ComplaintStatus.IN_PROGRESS, result.getStatus());
        assertEquals(user, result.getAssignedTo());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(convoyRepository).findById(convoyId);
        verify(complaintRepository).findAllByConvoy(convoy);
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void takeComplaint_ShouldThrowException_WhenUserAlreadyHasActiveComplaint() {
        // Arrange
        Long convoyId = 1L;
        Complaint existingComplaint = new Complaint();
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.of(existingComplaint));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> complaintService.takeComplaint(convoyId, user)
        );
        assertEquals("У вас уже есть активная жалоба в работе", exception.getMessage());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(convoyRepository, never()).findById(anyLong());
    }

    @Test
    void takeComplaint_ShouldThrowException_WhenConvoyDoesNotExist() {
        // Arrange
        Long convoyId = 1L;
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(convoyRepository.findById(convoyId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> complaintService.takeComplaint(convoyId, user)
        );
        assertEquals("Конвой не найден", exception.getMessage());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(convoyRepository).findById(convoyId);
        verify(complaintRepository, never()).findAllByConvoy(any(Convoy.class));
    }

    @Test
    void takeComplaint_ShouldThrowException_WhenNoNewComplaintsAvailable() {
        // Arrange
        Long convoyId = 1L;
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(convoyRepository.findById(convoyId)).thenReturn(Optional.of(convoy));
        
        Complaint complaint = new Complaint();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setConvoy(convoy);
        when(complaintRepository.findAllByConvoy(convoy)).thenReturn(List.of(complaint));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> complaintService.takeComplaint(convoyId, user)
        );
        assertEquals("Нет доступных жалоб на этот конвой", exception.getMessage());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(convoyRepository).findById(convoyId);
        verify(complaintRepository).findAllByConvoy(convoy);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void completeComplaint_ShouldSetStatusToCompleted_WhenUserHasActiveComplaint() {
        // Arrange
        Long convoyId = 1L;
        Complaint complaint = new Complaint();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setAssignedTo(user);
        complaint.setConvoy(convoy);
        
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        complaintService.completeComplaint(convoyId, user);

        // Assert
        assertEquals(ComplaintStatus.COMPLETED, complaint.getStatus());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository).save(complaint);
    }

    @Test
    void completeComplaint_ShouldThrowException_WhenUserHasNoActiveComplaint() {
        // Arrange
        Long convoyId = 1L;
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> complaintService.completeComplaint(convoyId, user)
        );
        assertEquals("У вас нет активной жалобы", exception.getMessage());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void completeComplaint_ShouldThrowException_WhenConvoyIdMismatch() {
        // Arrange
        Long convoyId = 2L; // Different from complaint's convoy
        Convoy differentConvoy = new Convoy();
        differentConvoy.setId(2L);
        
        Complaint complaint = new Complaint();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setAssignedTo(user);
        complaint.setConvoy(convoy); // Different convoy
        
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.of(complaint));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> complaintService.completeComplaint(convoyId, user)
        );
        assertEquals("Жалоба не соответствует указанному конвою", exception.getMessage());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void cancelComplaint_ShouldResetComplaint_WhenUserHasActiveComplaint() {
        // Arrange
        Long convoyId = 1L;
        Complaint complaint = new Complaint();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setAssignedTo(user);
        complaint.setConvoy(convoy);
        
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        complaintService.cancelComplaint(convoyId, user);

        // Assert
        assertEquals(ComplaintStatus.NEW, complaint.getStatus());
        assertNull(complaint.getAssignedTo());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository).save(complaint);
    }

    @Test
    void cancelComplaint_ShouldThrowException_WhenUserHasNoActiveComplaint() {
        // Arrange
        Long convoyId = 1L;
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> complaintService.cancelComplaint(convoyId, user)
        );
        assertEquals("У вас нет активной жалобы", exception.getMessage());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void cancelComplaint_ShouldThrowException_WhenConvoyIdMismatch() {
        // Arrange
        Long convoyId = 2L; // Different from complaint's convoy
        Convoy differentConvoy = new Convoy();
        differentConvoy.setId(2L);
        
        Complaint complaint = new Complaint();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setAssignedTo(user);
        complaint.setConvoy(convoy); // Different convoy
        
        when(complaintRepository.findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS))
                .thenReturn(Optional.of(complaint));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> complaintService.cancelComplaint(convoyId, user)
        );
        assertEquals("Жалоба не соответствует указанному конвою", exception.getMessage());
        verify(complaintRepository).findByAssignedToAndStatus(user, ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void getComplaintsCount_ShouldReturnCorrectCount() {
        // Arrange
        int expectedCount = 3;
        when(complaintRepository.countByConvoy(convoy)).thenReturn(expectedCount);

        // Act
        int result = complaintService.getComplaintsCount(convoy);

        // Assert
        assertEquals(expectedCount, result);
        verify(complaintRepository).countByConvoy(convoy);
    }

    @Test
    void deleteAllByConvoy_ShouldDeleteAllComplaintsForConvoy() {
        // Act
        complaintService.deleteAllByConvoy(convoy);

        // Assert
        verify(complaintRepository).deleteAllByConvoy(convoy);
    }
}
