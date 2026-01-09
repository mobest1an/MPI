package com.iverpa.mpi.service;

import com.iverpa.mpi.dao.repository.ComplaintRepository;
import com.iverpa.mpi.dao.repository.ConvoyRepository;
import com.iverpa.mpi.model.Complaint;
import com.iverpa.mpi.model.ComplaintStatus;
import com.iverpa.mpi.model.Convoy;
import com.iverpa.mpi.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ConvoyRepository convoyRepository;

    /**
     * Подать жалобу на конвой (анонимно)
     */
    @Transactional
    public void submitComplaint(Long convoyId) {
        Convoy convoy = convoyRepository.findById(convoyId)
                .orElseThrow(() -> new IllegalArgumentException("Конвой не найден"));

        Complaint complaint = new Complaint();
        complaint.setConvoy(convoy);
        complaint.setStatus(ComplaintStatus.NEW);
        complaint.setCreatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
    }

    /**
     * Получить список всех активных конвоев
     */
    public List<Convoy> getActiveConvoys() {
        return convoyRepository.findAll();
    }

    /**
     * Получить список жалоб, сгруппированных по конвоям (для военной полиции)
     * Возвращает жалобы со статусом NEW или IN_PROGRESS
     */
    public List<Complaint> getComplaintsForPolice() {
        return complaintRepository.findAllByStatusIn(List.of(ComplaintStatus.NEW, ComplaintStatus.IN_PROGRESS));
    }

    /**
     * Получить активную жалобу текущего пользователя военной полиции
     */
    public Optional<Complaint> getActiveComplaint(User policeUser) {
        return complaintRepository.findByAssignedToAndStatus(policeUser, ComplaintStatus.IN_PROGRESS);
    }

    /**
     * Взять жалобу на конвой в работу
     */
    @Transactional
    public Complaint takeComplaint(Long convoyId, User policeUser) {
        // Проверяем, что у пользователя нет активной жалобы
        if (getActiveComplaint(policeUser).isPresent()) {
            throw new IllegalStateException("У вас уже есть активная жалоба в работе");
        }

        Convoy convoy = convoyRepository.findById(convoyId)
                .orElseThrow(() -> new IllegalArgumentException("Конвой не найден"));

        // Находим любую NEW жалобу на этот конвой
        List<Complaint> complaints = complaintRepository.findAllByConvoy(convoy);
        Complaint complaintToTake = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.NEW)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Нет доступных жалоб на этот конвой"));

        complaintToTake.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintToTake.setAssignedTo(policeUser);
        return complaintRepository.save(complaintToTake);
    }

    /**
     * Завершить жалобу
     */
    @Transactional
    public void completeComplaint(Long convoyId, User policeUser) {
        Complaint complaint = getActiveComplaint(policeUser)
                .orElseThrow(() -> new IllegalStateException("У вас нет активной жалобы"));

        if (!complaint.getConvoy().getId().equals(convoyId)) {
            throw new IllegalStateException("Жалоба не соответствует указанному конвою");
        }

        complaint.setStatus(ComplaintStatus.COMPLETED);
        complaintRepository.save(complaint);
    }

    /**
     * Отменить жалобу (вернуть в NEW)
     */
    @Transactional
    public void cancelComplaint(Long convoyId, User policeUser) {
        Complaint complaint = getActiveComplaint(policeUser)
                .orElseThrow(() -> new IllegalStateException("У вас нет активной жалобы"));

        if (!complaint.getConvoy().getId().equals(convoyId)) {
            throw new IllegalStateException("Жалоба не соответствует указанному конвою");
        }

        complaint.setStatus(ComplaintStatus.NEW);
        complaint.setAssignedTo(null);
        complaintRepository.save(complaint);
    }

    /**
     * Получить количество жалоб на конвой
     */
    public int getComplaintsCount(Convoy convoy) {
        return complaintRepository.countByConvoy(convoy);
    }

    /**
     * Удалить все жалобы на конвой (при роспуске)
     */
    @Transactional
    public void deleteAllByConvoy(Convoy convoy) {
        complaintRepository.deleteAllByConvoy(convoy);
    }
}
