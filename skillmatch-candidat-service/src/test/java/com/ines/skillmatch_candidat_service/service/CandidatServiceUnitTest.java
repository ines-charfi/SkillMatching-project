package com.ines.skillmatch_candidat_service.service;

import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.repository.CandidatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CandidatServiceUnitTest {

    @Mock
    private CandidatRepository candidatRepository;

    @InjectMocks
    private CandidatService candidatService;

    private Candidat candidatExemple;

    @BeforeEach
    void setUp() {
        candidatExemple = new Candidat();
        candidatExemple.setId(1L);
        candidatExemple.setUserId(100L);
        candidatExemple.setNom("Ines");
        candidatExemple.setPrenom("Dev");
        candidatExemple.setTelephone("0600000000");
        candidatExemple.setValidationStatut(Candidat.ValidationStatut.EN_ATTENTE);
    }

    @Test
    void testGetCandidatById_Success() {
        // Given
        Mockito.when(candidatRepository.findById(1L)).thenReturn(Optional.of(candidatExemple));

        // When
        Candidat result = candidatService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Ines", result.getNom());
        Mockito.verify(candidatRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void testInitCandidat_Success() {
        // Given
        Long userId = 100L;
        String nom = "Ines";
        String prenom = "Dev";

        Mockito.when(candidatRepository.findByUserId(userId)).thenReturn(Optional.empty());
        Mockito.when(candidatRepository.save(any(Candidat.class))).thenReturn(candidatExemple);

        // When
        candidatService.initCandidat(userId, nom, prenom);

        // Then
        Mockito.verify(candidatRepository, Mockito.times(1)).findByUserId(userId);
        Mockito.verify(candidatRepository, Mockito.times(1)).save(any(Candidat.class));
    }

    @Test
    void testGetByUserId_WhenCandidatExists() {
        // Given
        Long userId = 100L;
        Mockito.when(candidatRepository.findByUserId(userId)).thenReturn(Optional.of(candidatExemple));

        // When
        Candidat result = candidatService.getByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        Mockito.verify(candidatRepository, Mockito.times(1)).findByUserId(userId);
        Mockito.verify(candidatRepository, Mockito.never()).save(any(Candidat.class));
    }

    @Test
    void testGetByUserId_WhenCandidatDoesNotExist_ShouldCreateDefault() {
        // Given
        Long userId = 200L;
        Mockito.when(candidatRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Candidat defaultCandidat = Candidat.builder()
                .userId(userId)
                .nom("Candidat")
                .prenom("Nouveau")
                .bio("Complétez votre bio pour attirer les recruteurs.")
                .validationStatut(Candidat.ValidationStatut.EN_ATTENTE)
                .build();

        Mockito.when(candidatRepository.save(any(Candidat.class))).thenReturn(defaultCandidat);

        // When
        Candidat result = candidatService.getByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Candidat", result.getNom());
        Mockito.verify(candidatRepository, Mockito.times(1)).findByUserId(userId);
        Mockito.verify(candidatRepository, Mockito.times(1)).save(any(Candidat.class));
    }
}