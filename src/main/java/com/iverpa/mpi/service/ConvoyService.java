package com.iverpa.mpi.service;

import com.iverpa.mpi.controller.dto.responses.ConvoyResponse;
import com.iverpa.mpi.controller.dto.responses.WaitingRoomResponse;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.repository.ConvoyRepository;
import com.iverpa.mpi.model.Convoy;
import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConvoyService {

    private final ConvoyRepository convoyRepository;
    private final SummonService summonService;

    /**
     * Получить активный конвой конвоира
     */
    public Optional<ConvoyResponse> getActiveConvoy(User escort) {
        return convoyRepository.findByEscort(escort)
                .map(convoy -> {
                    List<WaitingRoomResponse> recruits = summonService.findAllByConvoy(convoy).stream()
                            .map(summon -> new WaitingRoomResponse(
                                    summon.getId(),
                                    summon.getUser().getUsername(),
                                    summon.getMilitaryBranch()
                            ))
                            .toList();
                    return new ConvoyResponse(convoy.getId(), recruits);
                });
    }

    /**
     * Проверить, есть ли у конвоира активный конвой
     */
    public boolean hasActiveConvoy(User escort) {
        return convoyRepository.existsByEscort(escort);
    }

    /**
     * Создать конвой из выбранных призывников
     * Транзакционно проверяет статусы и создаёт конвой
     */
    @Transactional
    public ConvoyResponse createConvoy(User escort, List<Long> summonIds) {
        // Проверяем, что у конвоира нет активного конвоя
        if (hasActiveConvoy(escort)) {
            throw new IllegalStateException("У вас уже есть активный конвой");
        }

        if (summonIds == null || summonIds.isEmpty()) {
            throw new IllegalArgumentException("Необходимо выбрать хотя бы одного призывника");
        }

        // Получаем призывников и проверяем их статусы
        List<Summon> summons = summonService.findAllByIdsAndStatus(summonIds, RecruitStatus.WAITING_ESCORT);

        if (summons.size() != summonIds.size()) {
            throw new IllegalStateException("Некоторые призывники уже недоступны для конвоирования");
        }

        // Создаём конвой
        Convoy convoy = new Convoy();
        convoy.setEscort(escort);
        convoy = convoyRepository.save(convoy);

        // Привязываем призывников к конвою и меняем статус
        for (Summon summon : summons) {
            summon.setConvoy(convoy);
            summon.setStatus(RecruitStatus.IN_CONVOY);
        }
        summonService.saveAll(summons);

        // Формируем ответ
        List<WaitingRoomResponse> recruits = summons.stream()
                .map(summon -> new WaitingRoomResponse(
                        summon.getId(),
                        summon.getUser().getUsername(),
                        summon.getMilitaryBranch()
                ))
                .toList();

        return new ConvoyResponse(convoy.getId(), recruits);
    }

    /**
     * Распустить конвой (призывники доставлены)
     */
    @Transactional
    public void dismissConvoy(User escort) {
        Convoy convoy = convoyRepository.findByEscort(escort)
                .orElseThrow(() -> new IllegalStateException("У вас нет активного конвоя"));

        // Меняем статус всех призывников на DONE
        List<Summon> summons = summonService.findAllByConvoy(convoy);
        for (Summon summon : summons) {
            summon.setConvoy(null);
            summon.setStatus(RecruitStatus.DONE);
        }
        summonService.saveAll(summons);

        // Удаляем конвой
        convoyRepository.delete(convoy);
    }
}
