package com.iverpa.mpi.model;

public enum RecruitStatus {
    NOT_STARTED,      // Зарегистрирован, не встал в очередь
    IN_QUEUE,         // В очереди к комиссару
    SUMMONED,         // Вызван комиссаром
    WAITING_ESCORT,   // В зале ожидания конвоирования
    IN_CONVOY,        // В конвое
    DONE              // Доставлен в часть
}
