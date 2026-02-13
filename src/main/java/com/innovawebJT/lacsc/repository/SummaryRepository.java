package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.dto.SummaryCounterDTO;
import com.innovawebJT.lacsc.enums.SpecialSessions;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    //Todos los resúmenes de un usuario
    Page<Summary> findByPresenter(User presenter, Pageable pageable);

    //Resumen por ID validando ownership
    Optional<Summary> findByIdAndPresenter(Long id, User presenter);

    //Filtrar por estatus de pago
    Page<Summary> findBySummaryPayment(Status status, Pageable pageable);

    //Usuario + estatus
    Page<Summary> findByPresenterAndSummaryPayment(
        User presenter,
        Status status,
        Pageable pageable
    );

    //Buscar por título
    Page<Summary> findByTitleContainingIgnoreCase(
        String title,
        Pageable pageable
    );

    //Buscar por título (usuario)
    Page<Summary> findByPresenterAndTitleContainingIgnoreCase(
        User presenter,
        String title,
        Pageable pageable
    );

    Optional<List<Summary>> getAllByPresenter_Id(Long id);

    Optional<List<Summary>> getAllByPresenter_IdAndSummaryPayment(Long id, Status status);

    int countAllByPresenter_IdAndSummaryPayment(Long presenterId, Status summaryPayment);

    int countAllByPresenter_Id(Long presenterId);

    Page<Summary> findBySpecialSessionIn(List<SpecialSessions> specialSessions, Pageable pageable);

    List<Summary> findAllByPresenter_IdAndSpecialSessionIn(Long userId, List<SpecialSessions> sessions);
}
